package openfoodfacts.github.scrachx.openfood.views.product;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.ContributorsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.ProductPhotosFragment;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.BottomNavigationBehavior;
import openfoodfacts.github.scrachx.openfood.views.HistoryScanActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
import openfoodfacts.github.scrachx.openfood.views.ScannerFragmentActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsRecyclerViewAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients.IngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.nutrition.NutritionProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.nutrition_details.NutritionInfoProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductFragment;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class ProductActivity extends BaseActivity implements CustomTabActivityHelper.ConnectionCallback, OnRefreshListener {

    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.buttonScan)
    FloatingActionButton mButtonScan;
    TextView bottomSheetDesciption;
    TextView bottomSheetTitle;
    Button buttonToBrowseProducts;
    Button wikipediaButton;
    RecyclerView productBrowsingRecyclerView;
    ProductFragmentPagerAdapter adapterResult;
    ProductsRecyclerViewAdapter productsRecyclerViewAdapter;

    private OpenFoodAPIClient api;
    private ShareActionProvider mShareActionProvider;
    private BottomSheetBehavior bottomSheetBehavior;
    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;
    private State mState;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // boolean to determine if scan on shake feature should be enabled
    private boolean scanOnShake;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_product);
        setTitle(getString(R.string.app_name_long));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);

        api = new OpenFoodAPIClient(this);
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getApplicationContext(), customTabActivityHelper.getSession());

        View v = findViewById(R.id.design_bottom_sheet_product_activity);
        bottomSheetTitle = v.findViewById(R.id.titleBottomSheet);
        bottomSheetDesciption = v.findViewById(R.id.description);
        buttonToBrowseProducts = v.findViewById(R.id.buttonToBrowseProducts);
        wikipediaButton = v.findViewById(R.id.wikipediaButton);

        bottomSheetBehavior = BottomSheetBehavior.from(v);

        mState = (State) getIntent().getExtras().getSerializable("state");
        if (!Utils.isHardwareCameraInstalled(this)) {
            mButtonScan.setVisibility(View.GONE);
        }

        // Get the user preference for scan on shake feature and open ScannerFragmentActivity if the user has enabled the feature
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(this);
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);


        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeDetected() {
            @Override
            public void onShake(int count) {

                if (scanOnShake) {
                    Utils.scan(ProductActivity.this);
                }
            }
        });


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
//                case R.id.bookmark:
//                     Implementation of bookmark will be here
//                    Toast.makeText(ProductActivity.this,"Bookmark",Toast.LENGTH_SHORT).show();
//                    break;
                case R.id.share:
                    String shareUrl = " " + getString(R.string.website_product) + mState.getProduct().getCode();
                    Intent sharingIntent = new Intent();
                    sharingIntent.setAction(Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = getResources().getString(R.string.msg_share) + shareUrl;
                    String shareSub = "\n\n";
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                    startActivity(Intent.createChooser(sharingIntent, "Share using"));
                    break;
//                case R.id.translation:
//                     Implementation of Translation will be here
//                    Toast.makeText(ProductActivity.this,"Translation",Toast.LENGTH_SHORT).show();
//                    break;
                case R.id.edit_product:
                    String url = getString(R.string.website) + "cgi/product.pl?type=edit&code=" + mState.getProduct().getCode();
                    if (mState.getProduct().getUrl() != null) {
                        url = " " + mState.getProduct().getUrl();
                    }

                    CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(), null);

                    CustomTabActivityHelper.openCustomTab(ProductActivity.this, customTabsIntent, Uri.parse(url), new WebViewFallback());
                    break;

                case R.id.history_bottom_nav:
                    startActivity(new Intent(this, HistoryScanActivity.class));
                    break;

                case R.id.search_product:
                    startActivity(new Intent(this, MainActivity.class));
                    break;

                case R.id.empty:
                    break;
                default:
                    return true;

            }
            return true;
        });
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());
    }

    public void expand() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        mButtonScan.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.buttonScan)
    protected void OnScan() {
        if (Utils.isHardwareCameraInstalled(this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.action_about)
                            .content(R.string.permission_camera)
                            .neutralText(R.string.txtOk)
                            .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                Intent intent = new Intent(this, ScannerFragmentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        String[] menuTitles = getResources().getStringArray(R.array.nav_drawer_items_product);

        adapterResult = new ProductFragmentPagerAdapter(getSupportFragmentManager());
        adapterResult.addFragment(new SummaryProductFragment(), menuTitles[0]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("contributionTab", false)) {
            adapterResult.addFragment(new ContributorsFragment(), getString(R.string.contribution_tab));
        }
        if (BuildConfig.FLAVOR.equals("off") || BuildConfig.FLAVOR.equals("obf") || BuildConfig.FLAVOR.equals("opff")) {
            adapterResult.addFragment(new IngredientsProductFragment(), menuTitles[1]);
        }
        if (BuildConfig.FLAVOR.equals("off")) {
            adapterResult.addFragment(new NutritionProductFragment(), menuTitles[2]);
            adapterResult.addFragment(new NutritionInfoProductFragment(), menuTitles[3]);
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("photoMode", false)) {
                adapterResult.addFragment(new ProductPhotosFragment(), "Product Photos");
            }
        }
        if (BuildConfig.FLAVOR.equals("opff")) {
            adapterResult.addFragment(new NutritionProductFragment(), menuTitles[2]);
            adapterResult.addFragment(new NutritionInfoProductFragment(), menuTitles[3]);
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("photoMode", false)) {
                adapterResult.addFragment(new ProductPhotosFragment(), "Product Photos");
            }
        }

        if (BuildConfig.FLAVOR.equals("obf")) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("photoMode", false)) {
                adapterResult.addFragment(new ProductPhotosFragment(), "Product Photos");
            }
        }

        if (BuildConfig.FLAVOR.equals("opf")) {
            adapterResult.addFragment(new ProductPhotosFragment(), "Product Photos");
        }

        viewPager.setAdapter(adapterResult);
    }

    /**
     * This method is used to hide share_item and edit_product in App Bar
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem share_item = menu.findItem(R.id.menu_item_share);
        share_item.setVisible(false);
        MenuItem edit_product = menu.findItem(R.id.action_edit_product);
        edit_product.setVisible(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;

            case R.id.menu_item_share:
                String shareUrl = " " + getString(R.string.website_product) + mState.getProduct().getCode();
                Intent sharingIntent = new Intent();
                sharingIntent.setAction(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = getResources().getString(R.string.msg_share) + shareUrl;
                String shareSub = "\n\n";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share using"));
                return true;

            case R.id.action_edit_product:
                String url = getString(R.string.website) + "cgi/product.pl?type=edit&code=" + mState.getProduct().getCode();
                if (mState.getProduct().getUrl() != null) {
                    url = " " + mState.getProduct().getUrl();
                }

                CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(), null);

                CustomTabActivityHelper.openCustomTab(ProductActivity.this, customTabsIntent, Uri.parse(url), new WebViewFallback());
                return true;

            case R.id.action_calculate_calories:
                /*
                creates dialog for calculating total calories for the entered weight.
                Result is displayed instantaneously by listening to the changes in input
                as well as the spinner.
                 */
                MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_calories)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                MaterialDialog dialog = builder.build();
                dialog.show();
                View view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView caloriesResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showCalories(etWeight, spinner, caloriesResult);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                    etWeight.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            showCalories(etWeight, spinner, caloriesResult);
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Displays the calculated calorie in the dialog.
     *
     * @param etWeight       editText for inputting weight.
     * @param spinner        indicating the unit (mg, g or kg).
     * @param caloriesResult textView in which the result is displayed.
     */
    private void showCalories(EditText etWeight, Spinner spinner, TextView caloriesResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            float calories = calculateCalories(weight, unit);
            caloriesResult.setText(getString(R.string.txt_calories_result, Utils.getRoundNumber(String.valueOf(calories)),
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }

    /**
     * Given a weight of food, calculate the number of calories for that portion.
     *
     * @param weight for which calories need to be calculated.
     * @param unit   from spinner either mg, g or kg.
     * @return total calories in the provided weight.
     */
    private float calculateCalories(float weight, String unit) {
        float caloriePer100g, weightInG;
        caloriePer100g = Float.valueOf(Utils.getEnergy(mState.getProduct().getNutriments().get(Nutriments.ENERGY).getFor100g()));
        switch (unit) {
            case "mg":
                weightInG = weight / 1000;
                break;
            case "kg":
                weightInG = weight * 1000;
                break;
            default:
                weightInG = weight;
                break;
        }
        return ((caloriePer100g / 100) * weightInG);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product, menu);

        /*
        Hide the 'Calculate Calories' option from the overflow menu if the product
        doesn't have calories information in nutrition facts.
        */
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.ENERGY) == null) {
            menu.findItem(R.id.action_calculate_calories).setVisible(false);
        }
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
            case Utils.MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length <= 0 || grantResults[0] != PERMISSION_GRANTED) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .show();
                }
            }
        }
    }

    public void showBottomScreen(JSONObject result, String code, int type, String title) {
        try {
            result = result.getJSONObject("entities").getJSONObject(code);
            JSONObject description = result.getJSONObject("descriptions");
            JSONObject sitelinks = result.getJSONObject("sitelinks");
            String descriptionString = getDescription(description);
            String wikiLink = getWikiLink(sitelinks);
            bottomSheetTitle.setText(title);
            bottomSheetDesciption.setText(descriptionString);
            buttonToBrowseProducts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(ProductActivity.this, ProductBrowsingListActivity.class);
                    switch (type) {
                        case 1: {
                            intent.putExtra("search_type", SearchType.CATEGORY);
                            break;
                        }
                        case 2: {
                            intent.putExtra("search_type", SearchType.LABEL);
                            break;
                        }
                        case 3: {
                            intent.putExtra("search_type", SearchType.ADDITIVE);
                            break;
                        }
                    }
                    intent.putExtra("search_query", title);
                    startActivity(intent);
                }
            });
            wikipediaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openInCustomTab(wikiLink);
                }
            });
            expand();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getWikiLink(JSONObject sitelinks) {
        String link = "";
        String languageCode = Locale.getDefault().getLanguage();
        languageCode = languageCode + "wiki";
        if (sitelinks.has(languageCode)) {
            try {
                sitelinks = sitelinks.getJSONObject(languageCode);
                link = sitelinks.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (sitelinks.has("enwiki")) {
            try {
                sitelinks = sitelinks.getJSONObject("enwiki");
                link = sitelinks.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("ProductActivity", "Result for wikilink is not found in native or english language.");
        }
        return link;
    }

    private String getDescription(JSONObject description) {
        String descriptionString = "";
        String languageCode = Locale.getDefault().getLanguage();
        if (description.has(languageCode)) {
            try {
                description = description.getJSONObject(languageCode);
                descriptionString = description.getString("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (description.has("en")) {
            try {
                description = description.getJSONObject("en");
                descriptionString = description.getString("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("ProductActivity", "Result for description is not found in native or english language.");
        }
        return descriptionString;
    }

    private void openInCustomTab(String url) {
        Uri wikipediaUri = Uri.parse(url);
        CustomTabActivityHelper.openCustomTab(ProductActivity.this, customTabsIntent, wikipediaUri, new WebViewFallback());

    }

    @Override
    public void onRefresh() {
        api.getProduct(mState.getProduct().getCode(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        adapterResult.refresh((State) intent.getExtras().getSerializable("state"));
    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    @Override
    public void onPause() {
        super.onPause();
        if (scanOnShake) {
            //unregister the listener
            mSensorManager.unregisterListener(mShakeDetector, mAccelerometer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scanOnShake) {
            //register the listener
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }
}
