package openfoodfacts.github.scrachx.openfood.views.product;

import android.Manifest;
import android.annotation.SuppressLint;
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
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;
import openfoodfacts.github.scrachx.openfood.views.HistoryScanActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;
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

        // Get the user preference for scan on shake feature and open ContinuousScanActivity if the user has enabled the feature
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
                Intent intent = new Intent(this, ContinuousScanActivity.class);
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
            case R.id.fat:
        /*
        creates dialog for calculating total calories for the entered weight.
        Result is displayed instantaneously by listening to the changes in input
        as well as the spinner.
         */
                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_fat)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView fatResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showfat(etWeight, spinner, fatResult);
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
                            showfat(etWeight, spinner, fatResult);
                        }
                    });
                }
                return true;
            case R.id.carbohydrate:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_carbohydrate)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView carbohydrateResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showcarbohydrate(etWeight, spinner, carbohydrateResult);
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
                            showcarbohydrate(etWeight, spinner, carbohydrateResult);
                        }
                    });
                }
                return true;
            case R.id.cholesterol:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_cholesterol)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView cholesterolResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showcholesterol(etWeight, spinner, cholesterolResult);
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
                            showcholesterol(etWeight, spinner, cholesterolResult);
                        }
                    });
                }
                return true;
            case R.id.df:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_fiber)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView fiberResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showfiber(etWeight, spinner, fiberResult);
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
                            showfiber(etWeight, spinner, fiberResult);
                        }
                    });
                }
                return true;
            case R.id.protein:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_protein)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView proteinResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showprotein(etWeight, spinner, proteinResult);
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
                            showprotein(etWeight, spinner, proteinResult);
                        }
                    });
                }
                return true;
            case R.id.salt:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_salt)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView saltResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showsalt(etWeight, spinner, saltResult);
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
                            showsalt(etWeight, spinner, saltResult);
                        }
                    });
                }
                return true;
            case R.id.satfat:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_satfat)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView satfatResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showsatfat(etWeight, spinner, satfatResult);
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
                            showsatfat(etWeight, spinner, satfatResult);
                        }
                    });
                }
                return true;
            case R.id.sodium:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_sodium)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView sodiumResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showsodium(etWeight, spinner, sodiumResult);
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
                            showsodium(etWeight, spinner, sodiumResult);
                        }
                    });
                }
                return true;
            case R.id.sugar:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_sugar)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView sugarResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showsugar(etWeight, spinner, sugarResult);
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
                            showsugar(etWeight, spinner, sugarResult);
                        }
                    });
                }
                return true;
            case R.id.transfat:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_trans_fat)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView transfatResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showtransfat(etWeight, spinner, transfatResult);
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
                            showtransfat(etWeight, spinner, transfatResult);
                        }
                    });
                }
                return true;
            case R.id.lactose:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_lactose)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView lactoseResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showlactose(etWeight, spinner, lactoseResult);
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
                            showlactose(etWeight, spinner, lactoseResult);
                        }
                    });
                }
                return true;
            case R.id.casein:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_casein)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView caseinResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showcasein(etWeight, spinner, caseinResult);
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
                            showcasein(etWeight, spinner, caseinResult);
                        }
                    });
                }
                return true;
            case R.id.maltodextrins:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_maltodextrins)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView maltodextrinsResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showmaltodextrins(etWeight, spinner, maltodextrinsResult);
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
                            showmaltodextrins(etWeight, spinner, maltodextrinsResult);
                        }
                    });
                }
                return true;
            case R.id.sp:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_serum_proteins)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView spResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showsp(etWeight, spinner, spResult);
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
                            showsp(etWeight, spinner, spResult);
                        }
                    });
                }
                return true;
            case R.id.nucleotides:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_nucleotides)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView nucleotidesResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            shownucleotides(etWeight, spinner, nucleotidesResult);
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
                            shownucleotides(etWeight, spinner, nucleotidesResult);
                        }
                    });
                }
                return true;
            case R.id.calcium:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_calcium)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView calciumResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showcalcium(etWeight, spinner, calciumResult);
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
                            showcalcium(etWeight, spinner, calciumResult);
                        }
                    });
                }
                return true;
            case R.id.potassium:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_potassium)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView potassiumResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showpotassium(etWeight, spinner, potassiumResult);
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
                            showpotassium(etWeight, spinner, potassiumResult);
                        }
                    });
                }
                return true;
            case R.id.chlorine:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_chlorine)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView chlorineResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showchlorine(etWeight, spinner, chlorineResult);
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
                            showchlorine(etWeight, spinner, chlorineResult);
                        }
                    });
                }
                return true;
            case R.id.magnesium:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_magnesium)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView magnesiumResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showmagnesium(etWeight, spinner, magnesiumResult);
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
                            showmagnesium(etWeight, spinner, magnesiumResult);
                        }
                    });
                }
                return true;
            case R.id.taurine:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_taurine)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView taurineResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showtaurine(etWeight, spinner, taurineResult);
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
                            showtaurine(etWeight, spinner, taurineResult);
                        }
                    });
                }
                return true;
            case R.id.muf:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_muf)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView mufResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showmuf(etWeight, spinner, mufResult);
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
                            showmuf(etWeight, spinner, mufResult);
                        }
                    });
                }
                return true;
            case R.id.puf:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_puf)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView pufResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showpuf(etWeight, spinner, pufResult);
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
                            showpuf(etWeight, spinner, pufResult);
                        }
                    });
                }
                return true;
            case R.id.omega3:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_omega3)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView omega3Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showomega3(etWeight, spinner, omega3Result);
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
                            showomega3(etWeight, spinner, omega3Result);
                        }
                    });
                }
                return true;
            case R.id.omega6:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_omega6)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView omega6Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showomega6(etWeight, spinner, omega6Result);
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
                            showomega6(etWeight, spinner, omega6Result);
                        }
                    });
                }
                return true;
            case R.id.omega9:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_omega9)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView omega9Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showomega9(etWeight, spinner, omega9Result);
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
                            showomega9(etWeight, spinner, omega9Result);
                        }
                    });
                }
                return true;
            case R.id.sucrose:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_sucrose)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView sucroseResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showsucrose(etWeight, spinner, sucroseResult);
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
                            showsucrose(etWeight, spinner, sucroseResult);
                        }
                    });
                }
                return true;
            case R.id.glucose:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_glucose)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView glucoseResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showglucose(etWeight, spinner, glucoseResult);
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
                            showglucose(etWeight, spinner, glucoseResult);
                        }
                    });
                }
                return true;
            case R.id.fructose:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_fructose)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView fructoseResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showfructose(etWeight, spinner, fructoseResult);
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
                            showfructose(etWeight, spinner, fructoseResult);
                        }
                    });
                }
                return true;
            case R.id.maltose:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_maltose)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView maltoseResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showmaltose(etWeight, spinner, maltoseResult);
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
                            showmaltose(etWeight, spinner, maltoseResult);
                        }
                    });
                }
                return true;
            case R.id.alcohol:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_alcohol)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView alcoholResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showalcohol(etWeight, spinner, alcoholResult);
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
                            showalcohol(etWeight, spinner, alcoholResult);
                        }
                    });
                }
                return true;
            case R.id.vitA:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_a)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitaResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvita(etWeight, spinner, vitaResult);
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
                            showvita(etWeight, spinner, vitaResult);
                        }
                    });
                }
                return true;
            case R.id.vitD:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_d)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitdResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvitd(etWeight, spinner, vitdResult);
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
                            showvitd(etWeight, spinner, vitdResult);
                        }
                    });
                }
                return true;
            case R.id.vitE:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_e)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView viteResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvite(etWeight, spinner, viteResult);
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
                            showvite(etWeight, spinner, viteResult);
                        }
                    });
                }
                return true;
            case R.id.vitK:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_k)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitkResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvitk(etWeight, spinner, vitkResult);
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
                            showvitk(etWeight, spinner, vitkResult);
                        }
                    });
                }
                return true;
            case R.id.vitC:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_c)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitcResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvitc(etWeight, spinner, vitcResult);
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
                            showvitc(etWeight, spinner, vitcResult);
                        }
                    });
                }
                return true;
            case R.id.vitPP:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_pp)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitppResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvitpp(etWeight, spinner, vitppResult);
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
                            showvitpp(etWeight, spinner, vitppResult);
                        }
                    });
                }
                return true;
            case R.id.vitB1:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_b1)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitb1Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvitb1(etWeight, spinner, vitb1Result);
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
                            showvitb1(etWeight, spinner, vitb1Result);
                        }
                    });
                }
                return true;
            case R.id.vitB2:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_b2)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitb2Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvitb2(etWeight, spinner, vitb2Result);
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
                            showvitb2(etWeight, spinner, vitb2Result);
                        }
                    });
                }
                return true;
            case R.id.vitB6:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_b6)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitb6Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvitb6(etWeight, spinner, vitb6Result);
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
                            showvitb6(etWeight, spinner, vitb6Result);
                        }
                    });
                }
                return true;
            case R.id.vitB9:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_b9)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitb9Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvitb9(etWeight, spinner, vitb9Result);
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
                            showvitb9(etWeight, spinner, vitb9Result);
                        }
                    });
                }
                return true;
            case R.id.vitB12:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_vitamin_b12)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView vitb12Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showvitb12(etWeight, spinner, vitb12Result);
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
                            showvitb12(etWeight, spinner, vitb12Result);
                        }
                    });
                }
                return true;
            case R.id.biotin:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_biotin)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView biotinResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showbiotin(etWeight, spinner, biotinResult);
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
                            showbiotin(etWeight, spinner, biotinResult);
                        }
                    });
                }
                return true;
            case R.id.pa:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_pa)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView paResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showpa(etWeight, spinner, paResult);
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
                            showpa(etWeight, spinner, paResult);
                        }
                    });
                }
                return true;
            case R.id.silica:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_silica)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView silicaResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showsilica(etWeight, spinner, silicaResult);
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
                            showsilica(etWeight, spinner, silicaResult);
                        }
                    });
                }
                return true;
            case R.id.bicarb:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_bicarbonate)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView bicarbResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showbicarbonate(etWeight, spinner, bicarbResult);
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
                            showbicarbonate(etWeight, spinner, bicarbResult);
                        }
                    });
                }
                return true;
            case R.id.phos:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_phosphorus)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView phosResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showphosphorus(etWeight, spinner, phosResult);
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
                            showphosphorus(etWeight, spinner, phosResult);
                        }
                    });
                }
                return true;
            case R.id.zinc:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_zinc)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView zincResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showzinc(etWeight, spinner, zincResult);
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
                            showzinc(etWeight, spinner, zincResult);
                        }
                    });
                }
                return true;
            case R.id.iron:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_iron)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView ironResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showiron(etWeight, spinner, ironResult);
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
                            showiron(etWeight, spinner, ironResult);
                        }
                    });
                }
                return true;
            case R.id.copper:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_copper)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView copResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showcopper(etWeight, spinner, copResult);
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
                            showcopper(etWeight, spinner, copResult);
                        }
                    });
                }
                return true;
            case R.id.manganese:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_manganese)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView mangResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showmanganese(etWeight, spinner, mangResult);
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
                            showmanganese(etWeight, spinner, mangResult);
                        }
                    });
                }
                return true;
            case R.id.fluoride:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_fluoride)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView fluoResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showfluoride(etWeight, spinner, fluoResult);
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
                            showfluoride(etWeight, spinner, fluoResult);
                        }
                    });
                }
                return true;
            case R.id.selenium:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_selenium)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView selResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showselenium(etWeight, spinner, selResult);
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
                            showselenium(etWeight, spinner, selResult);
                        }
                    });
                }
                return true;
            case R.id.chromium:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_chromium)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView chroResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showchromium(etWeight, spinner, chroResult);
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
                            showchromium(etWeight, spinner, chroResult);
                        }
                    });
                }
                return true;
            case R.id.molybdenum:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_molybdenum)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView molyResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showmolybdenum(etWeight, spinner, molyResult);
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
                            showmolybdenum(etWeight, spinner, molyResult);
                        }
                    });
                }
                return true;
            case R.id.iodine:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_iodine)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView iodResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showiodine(etWeight, spinner, iodResult);
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
                            showiodine(etWeight, spinner, iodResult);
                        }
                    });
                }
                return true;
            case R.id.caffeine:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_caffeine)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView caffResult = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showcaffeine(etWeight, spinner, caffResult);
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
                            showcaffeine(etWeight, spinner, caffResult);
                        }
                    });
                }
                return true;
            case R.id.chlorophyl:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_chlorophyl)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showchlorophyl(etWeight, spinner, Result);
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
                            showchlorophyl(etWeight, spinner, Result);
                        }
                    });
                }
                return true;
            case R.id.cocoa:

                builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_cocoa)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                dialog = builder.build();
                dialog.show();
                view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    TextView Result = view.findViewById(R.id.txt_calories_result);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            showcocoa(etWeight, spinner, Result);
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
                            showcocoa(etWeight, spinner, Result);
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
            caloriesResult.setText(getString(R.string.txt_calories_result,Utils.getRoundNumber(String.valueOf(calories)), Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showfat(EditText etWeight, Spinner spinner, TextView fatResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.FAT).getUnit();
            float fat = calculatefat(weight, unit);
            fatResult.setText(getString(R.string.txt_fat_result, Utils.getRoundNumber(String.valueOf(fat)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showcarbohydrate(EditText etWeight, Spinner spinner, TextView carbohydrateResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.CARBOHYDRATES).getUnit();
            float carbohydrate = calculatecarbohydrate(weight, unit);
            carbohydrateResult.setText(getString(R.string.txt_carbohydrate_result, Utils.getRoundNumber(String.valueOf(carbohydrate)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showcholesterol(EditText etWeight, Spinner spinner, TextView cholesterolResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.CHOLESTEROL).getUnit();
            float cholesterol = calculateCholesterol(weight, unit);
            cholesterolResult.setText(getString(R.string.txt_cholesterol_result, Utils.getRoundNumber(String.valueOf(cholesterol)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showfiber(EditText etWeight, Spinner spinner, TextView fiberResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.FIBER).getUnit();
            float fiber = calculatefiber(weight, unit);
            fiberResult.setText(getString(R.string.txt_fiber_result, Utils.getRoundNumber(String.valueOf(fiber)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showprotein(EditText etWeight, Spinner spinner, TextView proteinResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.PROTEINS).getUnit();
            float protein = calculateprotein(weight, unit);
            proteinResult.setText(getString(R.string.txt_protein_result, Utils.getRoundNumber(String.valueOf(protein)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showsalt(EditText etWeight, Spinner spinner, TextView saltResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.SALT).getUnit();
            float salt = calculatesalt(weight, unit);
            saltResult.setText(getString(R.string.txt_salt_result, Utils.getRoundNumber(String.valueOf(salt)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showsatfat(EditText etWeight, Spinner spinner, TextView satfatResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.SATURATED_FAT).getUnit();
            float satfat = calculatesatfat(weight, unit);
            satfatResult.setText(getString(R.string.txt_satfat_result, Utils.getRoundNumber(String.valueOf(satfat)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showsodium(EditText etWeight, Spinner spinner, TextView sodiumResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.SODIUM).getUnit();
            float sodium = calculateSodium(weight, unit);
            sodiumResult.setText(getString(R.string.txt_sodium_result, Utils.getRoundNumber(String.valueOf(sodium)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showsugar(EditText etWeight, Spinner spinner, TextView sugarResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.SUGARS).getUnit();
            float sugar = calculateSugar(weight, unit);
            sugarResult.setText(getString(R.string.txt_sugar_result, Utils.getRoundNumber(String.valueOf(sugar)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showtransfat(EditText etWeight, Spinner spinner, TextView transfatResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.TRANS_FAT).getUnit();
            float transfat = calculatetransfat(weight, unit);
            transfatResult.setText(getString(R.string.txt_transfat_result, Utils.getRoundNumber(String.valueOf(transfat)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showlactose(EditText etWeight, Spinner spinner, TextView lactoseResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.LACTOSE).getUnit();
            float lactose = calculatelactose(weight, unit);
            lactoseResult.setText(getString(R.string.txt_lactose_result, Utils.getRoundNumber(String.valueOf(lactose)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showcasein(EditText etWeight, Spinner spinner, TextView caseinResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.CASEIN).getUnit();
            float casein = calculatecasein(weight, unit);
            caseinResult.setText(getString(R.string.txt_casein_result, Utils.getRoundNumber(String.valueOf(casein)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showmaltodextrins(EditText etWeight, Spinner spinner, TextView maltodextrinsResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            float maltodextrins = calculatemaltodextrins(weight, unit);
            String s = mState.getProduct().getNutriments().get(Nutriments.MALTODEXTRINS).getUnit();
            maltodextrinsResult.setText(getString(R.string.txt_maltodextrins_result, Utils.getRoundNumber(String.valueOf(maltodextrins)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showsp(EditText etWeight, Spinner spinner, TextView spResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.SERUM_PROTEINS).getUnit();
            float sp = calculatesp(weight, unit);
            spResult.setText(getString(R.string.txt_sp_result, Utils.getRoundNumber(String.valueOf(sp)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void shownucleotides(EditText etWeight, Spinner spinner, TextView nucleotidesResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.NUCLEOTIDES).getUnit();
            float nucleotides = calculatenucleotides(weight, unit);
            nucleotidesResult.setText(getString(R.string.txt_nucleotides_result, Utils.getRoundNumber(String.valueOf(nucleotides)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showcalcium(EditText etWeight, Spinner spinner, TextView calciumResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.CALCIUM).getUnit();
            float calcium = calculatecalcium(weight, unit);
            calciumResult.setText(getString(R.string.txt_calcium_result, Utils.getRoundNumber(String.valueOf(calcium)), s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showpotassium(EditText etWeight, Spinner spinner, TextView potassiumResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.POTASSIUM).getUnit();
            float potassium = calculatepotassium(weight, unit);
            potassiumResult.setText(getString(R.string.txt_potassium_result, Utils.getRoundNumber(String.valueOf(potassium)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showchlorine(EditText etWeight, Spinner spinner, TextView chlorineResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.CHLORIDE).getUnit();
            float chlorine = calculatechlorine(weight, unit);
            chlorineResult.setText(getString(R.string.txt_chlorine_result, Utils.getRoundNumber(String.valueOf(chlorine)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showmagnesium(EditText etWeight, Spinner spinner, TextView magnesiumResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.MAGNESIUM).getUnit();
            float magnesium = calculatemagnesium(weight, unit);
            magnesiumResult.setText(getString(R.string.txt_magnesium_result, Utils.getRoundNumber(String.valueOf(magnesium)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showtaurine(EditText etWeight, Spinner spinner, TextView taurineResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.TAURINE).getUnit();
            float taurine = calculatetaurine(weight, unit);
            taurineResult.setText(getString(R.string.txt_taurine_result, Utils.getRoundNumber(String.valueOf(taurine)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showmuf(EditText etWeight, Spinner spinner, TextView mufResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.MONOUNSATURATED_FAT).getUnit();
            float muf = calculatemuf(weight, unit);
            mufResult.setText(getString(R.string.txt_muf_result, Utils.getRoundNumber(String.valueOf(muf)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showpuf(EditText etWeight, Spinner spinner, TextView pufResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.POLYUNSATURATED_FAT).getUnit();
            float puf = calculatepuf(weight, unit);
            pufResult.setText(getString(R.string.txt_puf_result, Utils.getRoundNumber(String.valueOf(puf)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showsucrose(EditText etWeight, Spinner spinner, TextView sucroseResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.SUCROSE).getUnit();
            float sucrose = calculatesucrose(weight, unit);
            sucroseResult.setText(getString(R.string.txt_sucrose_result, Utils.getRoundNumber(String.valueOf(sucrose)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showglucose(EditText etWeight, Spinner spinner, TextView glucoseResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.GLUCOSE).getUnit();
            float glucose = calculateglucose(weight, unit);
            glucoseResult.setText(getString(R.string.txt_glucose_result, Utils.getRoundNumber(String.valueOf(glucose)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showfructose(EditText etWeight, Spinner spinner, TextView fructoseResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.FRUCTOSE).getUnit();
            float fructose = calculatefructose(weight, unit);
            fructoseResult.setText(getString(R.string.txt_fructose_result, Utils.getRoundNumber(String.valueOf(fructose)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showomega3(EditText etWeight, Spinner spinner, TextView omega3Result) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.OMEGA_3_FAT).getUnit();
            float omega3 = calculateomega3(weight, unit);
            omega3Result.setText(getString(R.string.txt_omega3_result, Utils.getRoundNumber(String.valueOf(omega3)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showomega6(EditText etWeight, Spinner spinner, TextView omega6Result) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.OMEGA_9_FAT).getUnit();
            float omega6 = calculateomega6(weight, unit);
            omega6Result.setText(getString(R.string.txt_omega6_result, Utils.getRoundNumber(String.valueOf(omega6)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showomega9(EditText etWeight, Spinner spinner, TextView omega9Result) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.OMEGA_9_FAT).getUnit();
            float omega9 = calculateomega9(weight, unit);
            omega9Result.setText(getString(R.string.txt_omega9_result, Utils.getRoundNumber(String.valueOf(omega9)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showmaltose(EditText etWeight, Spinner spinner, TextView maltoseResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.MALTOSE).getUnit();
            float maltose = calculatemaltose(weight, unit);
            maltoseResult.setText(getString(R.string.txt_maltose_result, Utils.getRoundNumber(String.valueOf(maltose)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showalcohol(EditText etWeight, Spinner spinner, TextView alcoholResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.ALCOHOL).getUnit();
            float alcohol = calculatealcohol(weight, unit);
            alcoholResult.setText(getString(R.string.txt_alcohol_result, Utils.getRoundNumber(String.valueOf(alcohol)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvita(EditText etWeight, Spinner spinner, TextView vitaResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_A).getUnit();
            float vita = calculatevita(weight, unit);
            vitaResult.setText(getString(R.string.txt_vitamin_a_result, Utils.getRoundNumber(String.valueOf(vita)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvitd(EditText etWeight, Spinner spinner, TextView vitdResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_D).getUnit();
            float vitd = calculatevitd(weight, unit);
            vitdResult.setText(getString(R.string.txt_vitamin_d_result, Utils.getRoundNumber(String.valueOf(vitd)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvite(EditText etWeight, Spinner spinner, TextView viteResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_E).getUnit();
            float vite = calculatevite(weight, unit);
            viteResult.setText(getString(R.string.txt_vitamin_e_result, Utils.getRoundNumber(String.valueOf(vite)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvitk(EditText etWeight, Spinner spinner, TextView vitkResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_K).getUnit();
            float vitk = calculatevitk(weight, unit);
            vitkResult.setText(getString(R.string.txt_vitamin_k_result, Utils.getRoundNumber(String.valueOf(vitk)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvitc(EditText etWeight, Spinner spinner, TextView vitcResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_C).getUnit();
            float vitc = calculatevitc(weight, unit);
            vitcResult.setText(getString(R.string.txt_vitamin_c_result, Utils.getRoundNumber(String.valueOf(vitc)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvitpp(EditText etWeight, Spinner spinner, TextView vitppResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_PP).getUnit();
            float vitpp = calculatevitpp(weight, unit);
            vitppResult.setText(getString(R.string.txt_vitamin_pp_result, Utils.getRoundNumber(String.valueOf(vitpp)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvitb1(EditText etWeight, Spinner spinner, TextView vitb1Result) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B1).getUnit();
            float vitb1 = calculatevitb1(weight, unit);
            vitb1Result.setText(getString(R.string.txt_vitamin_b1_result, Utils.getRoundNumber(String.valueOf(vitb1)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvitb2(EditText etWeight, Spinner spinner, TextView vitb2Result) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B2).getUnit();
            float vitb2 = calculatevitb2(weight, unit);
            vitb2Result.setText(getString(R.string.txt_vitamin_b2_result, Utils.getRoundNumber(String.valueOf(vitb2)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvitb6(EditText etWeight, Spinner spinner, TextView vitb6Result) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B6).getUnit();
            float vitb6 = calculatevitb6(weight, unit);
            vitb6Result.setText(getString(R.string.txt_vitamin_b6_result, Utils.getRoundNumber(String.valueOf(vitb6)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvitb9(EditText etWeight, Spinner spinner, TextView vitb9Result) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B9).getUnit();
            float vitb9 = calculatevitb9(weight, unit);
            vitb9Result.setText(getString(R.string.txt_vitamin_b9_result, Utils.getRoundNumber(String.valueOf(vitb9)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showvitb12(EditText etWeight, Spinner spinner, TextView vitb12Result) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B12).getUnit();
            float vitb12 = calculatevitb12(weight, unit);
            vitb12Result.setText(getString(R.string.txt_vitamin_b12_result, Utils.getRoundNumber(String.valueOf(vitb12)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showbiotin(EditText etWeight, Spinner spinner, TextView biotinResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.BIOTIN).getUnit();
            float biotin = calculatebiotin(weight, unit);
            biotinResult.setText(getString(R.string.txt_biotin_result, Utils.getRoundNumber(String.valueOf(biotin)),s,
                    Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    // Here pa represents PANTOTHENIC ACID
    @SuppressLint("StringFormatMatches")
    private void showpa(EditText etWeight, Spinner spinner, TextView paResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.PANTOTHENIC_ACID).getUnit();
            float pa = calculatepa(weight, unit);
            paResult.setText(getString(R.string.txt_pa_result, Utils.getRoundNumber(String.valueOf(pa)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showsilica(EditText etWeight, Spinner spinner, TextView silicaResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.SILICA).getUnit();
            float silica = calculatesilica(weight, unit);
            silicaResult.setText(getString(R.string.txt_silica_result, Utils.getRoundNumber(String.valueOf(silica)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showbicarbonate(EditText etWeight, Spinner spinner, TextView bicarbResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.BICARBONATE).getUnit();
            float bicarb = calculatebicarbonate(weight, unit);
            bicarbResult.setText(getString(R.string.txt_bicarbonate_result, Utils.getRoundNumber(String.valueOf(bicarb)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showphosphorus(EditText etWeight, Spinner spinner, TextView phosResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.PHOSPHORUS).getUnit();
            float phos = calculatephosphorus(weight, unit);
            phosResult.setText(getString(R.string.txt_phosphorus_result, Utils.getRoundNumber(String.valueOf(phos)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showzinc(EditText etWeight, Spinner spinner, TextView zincResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.ZINC).getUnit();
            float zinc = calculatezinc(weight, unit);
            zincResult.setText(getString(R.string.txt_zinc_result, Utils.getRoundNumber(String.valueOf(zinc)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showiron(EditText etWeight, Spinner spinner, TextView ironResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.IRON).getUnit();
            float iron = calculateiron(weight, unit);
            ironResult.setText(getString(R.string.txt_iron_result, Utils.getRoundNumber(String.valueOf(iron)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showcopper(EditText etWeight, Spinner spinner, TextView copResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.COPPER).getUnit();
            float cop = calculatecopper(weight, unit);
            copResult.setText(getString(R.string.txt_copper_result, Utils.getRoundNumber(String.valueOf(cop)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showmanganese(EditText etWeight, Spinner spinner, TextView mangResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.MANGANESE).getUnit();
            float mang = calculatemanganese(weight, unit);
            mangResult.setText(getString(R.string.txt_manganese_result, Utils.getRoundNumber(String.valueOf(mang)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showfluoride(EditText etWeight, Spinner spinner, TextView fluoResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.FAT).getUnit();
            float fluo = calculatefluoride(weight, unit);
            fluoResult.setText(getString(R.string.txt_fluoride_result, Utils.getRoundNumber(String.valueOf(fluo)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showselenium(EditText etWeight, Spinner spinner, TextView selResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.SELENIUM).getUnit();
            float sel = calculateselenium(weight, unit);
            selResult.setText(getString(R.string.txt_selenium_result, Utils.getRoundNumber(String.valueOf(sel)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showchromium(EditText etWeight, Spinner spinner, TextView chroResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.CHROMIUM).getUnit();
            float chro = calculatechromium(weight, unit);
            chroResult.setText(getString(R.string.txt_chromium_result, Utils.getRoundNumber(String.valueOf(chro)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showmolybdenum(EditText etWeight, Spinner spinner, TextView molyResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.MOLYBDENUM).getUnit();
            float moly = calculatemolybdenum(weight, unit);
            molyResult.setText(getString(R.string.txt_molybdenum_result, Utils.getRoundNumber(String.valueOf(moly)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showiodine(EditText etWeight, Spinner spinner, TextView iodResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.IODINE).getUnit();
            float iod = calculateiodine(weight, unit);
            iodResult.setText(getString(R.string.txt_iodine_result, Utils.getRoundNumber(String.valueOf(iod)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showcaffeine(EditText etWeight, Spinner spinner, TextView caffResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.CAFFEINE).getUnit();
            float caff = calculatecaffeine(weight, unit);
            caffResult.setText(getString(R.string.txt_caffeine_result, Utils.getRoundNumber(String.valueOf(caff)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showchlorophyl(EditText etWeight, Spinner spinner, TextView chlorophylResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.CHLOROPHYL).getUnit();
            float chlorophyl = calculatechlorophyl(weight, unit);
            chlorophylResult.setText(getString(R.string.txt_chlorophyl_result, Utils.getRoundNumber(String.valueOf(chlorophyl)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }
    @SuppressLint("StringFormatMatches")
    private void showcocoa(EditText etWeight, Spinner spinner, TextView cocoaResult) {
        if (!TextUtils.isEmpty(etWeight.getText())) {
            float weight;
            try {
                weight = Float.valueOf(etWeight.getText().toString());
            } catch (NumberFormatException e) {
                return;
            }
            String unit = spinner.getSelectedItem().toString();
            String s = mState.getProduct().getNutriments().get(Nutriments.COCOA).getUnit();
            float cocoa = calculatecocoa(weight, unit);
            cocoaResult.setText(getString(R.string.txt_cocoa_result, Utils.getRoundNumber(String.valueOf(cocoa)), s,Utils.getRoundNumber(String.valueOf(weight)), unit));
        }
    }


    private float calculateiron(float weight, String unit) {
        float irPer100g, weightInG;
        irPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.IRON).getFor100g());
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
        return ((irPer100g / 100) * weightInG);
    }
    private float calculatecopper(float weight, String unit) {
        float copPer100g, weightInG;
        copPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.COPPER).getFor100g());
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
        return ((copPer100g / 100) * weightInG);
    }
    private float calculatemanganese(float weight, String unit) {
        float mangPer100g, weightInG;
        mangPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.MANGANESE).getFor100g());
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
        return ((mangPer100g / 100) * weightInG);
    }
    private float calculatefluoride(float weight, String unit) {
        float fluoPer100g, weightInG;
        fluoPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.FLUORIDE).getFor100g());
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
        return ((fluoPer100g / 100) * weightInG);
    }
    private float calculatepa(float weight, String unit) {
        float paPer100g, weightInG;
        paPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.PANTOTHENIC_ACID).getFor100g());
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
        return ((paPer100g / 100) * weightInG);
    }
    private float calculatefat(float weight, String unit) {
        float fatPer100g, weightInG;
        fatPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.FAT).getFor100g());
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
        return ((fatPer100g / 100) * weightInG);
    }
    private float calculatecarbohydrate(float weight, String unit) {
        float carbohydratePer100g, weightInG;
        carbohydratePer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.CARBOHYDRATES).getFor100g());
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
        return ((carbohydratePer100g / 100) * weightInG);
    }
    private float calculateCholesterol(float weight, String unit) {
        float cholesterolPer100g, weightInG;
        cholesterolPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.CHOLESTEROL).getFor100g());
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
        return ((cholesterolPer100g / 100) * weightInG);
    }
    private float calculatefiber(float weight, String unit) {
        float fiberPer100g, weightInG;
        fiberPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.FIBER).getFor100g());
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
        return ((fiberPer100g / 100) * weightInG);
    }
    private float calculateprotein(float weight, String unit) {
        float proPer100g, weightInG;
        proPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.PROTEINS).getFor100g());
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
        return ((proPer100g / 100) * weightInG);
    }
    private float calculatesalt(float weight, String unit) {
        float saltPer100g, weightInG;
        saltPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.SALT).getFor100g());
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
        return ((saltPer100g / 100) * weightInG);
    }
    private float calculatesatfat(float weight, String unit) {
        float satfatPer100g, weightInG;
        satfatPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.SATURATED_FAT).getFor100g());
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
        return ((satfatPer100g / 100) * weightInG);
    }
    private float calculateSodium(float weight, String unit) {
        float sodiumPer100g, weightInG;
        sodiumPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.SODIUM).getFor100g());
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
        return ((sodiumPer100g / 100) * weightInG);
    }
    private float calculateSugar(float weight, String unit) {
        float sugarPer100g, weightInG;
        sugarPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.SUGARS).getFor100g());
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
        return ((sugarPer100g / 100) * weightInG);
    }

    private float calculatetransfat(float weight, String unit) {
        float transfatPer100g, weightInG;
        transfatPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.TRANS_FAT).getFor100g());
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
        return ((transfatPer100g / 100) * weightInG);
    }

    private float calculatelactose(float weight, String unit) {
        float lactosePer100g, weightInG;
        lactosePer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.LACTOSE).getFor100g());
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
        return ((lactosePer100g / 100) * weightInG);
    }
    private float calculatecasein(float weight, String unit) {
        float caseinPer100g, weightInG;
        caseinPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.CASEIN).getFor100g());
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
        return ((caseinPer100g / 100) * weightInG);
    }

    private float calculatemaltodextrins(float weight, String unit) {
        float maltodextrinsPer100g, weightInG;
        maltodextrinsPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.MALTODEXTRINS).getFor100g());
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
        return ((maltodextrinsPer100g / 100) * weightInG);
    }
    private float calculatesp(float weight, String unit) {
        float spPer100g, weightInG;
        spPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.SERUM_PROTEINS).getFor100g());
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
        return ((spPer100g / 100) * weightInG);
    }
    private float calculatenucleotides(float weight, String unit) {
        float nucPer100g, weightInG;
        nucPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.NUCLEOTIDES).getFor100g());
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
        return ((nucPer100g / 100) * weightInG);
    }
    private float calculatecalcium(float weight, String unit) {
        float calciumPer100g, weightInG;
        calciumPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.CALCIUM).getFor100g());
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
        return ((calciumPer100g / 100) * weightInG);
    }
    private float calculatepotassium(float weight, String unit) {
        float potassiumPer100g, weightInG;
        potassiumPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.POTASSIUM).getFor100g());
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
        return ((potassiumPer100g / 100) * weightInG);
    }
    private float calculatechlorine(float weight, String unit) {
        float chlorinePer100g, weightInG;
        chlorinePer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.CHLORIDE).getFor100g());
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
        return ((chlorinePer100g / 100) * weightInG);
    }
    private float calculatemagnesium(float weight, String unit) {
        float magPer100g, weightInG;
        magPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.MAGNESIUM).getFor100g());
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
        return ((magPer100g / 100) * weightInG);
    }
    private float calculatetaurine(float weight, String unit) {
        float tauPer100g, weightInG;
        tauPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.TAURINE).getFor100g());
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
        return ((tauPer100g / 100) * weightInG);
    }
    private float calculatemuf(float weight, String unit) {
        float mufPer100g, weightInG;
        mufPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.MONOUNSATURATED_FAT).getFor100g());
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
        return ((mufPer100g / 100) * weightInG);
    }
    private float calculatepuf(float weight, String unit) {
        float pufPer100g, weightInG;
        pufPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.POLYUNSATURATED_FAT).getFor100g());
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
        return ((pufPer100g / 100) * weightInG);
    }
    private float calculateomega3(float weight, String unit) {
        float omega3Per100g, weightInG;
        omega3Per100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.OMEGA_3_FAT).getFor100g());
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
        return ((omega3Per100g / 100) * weightInG);
    }
    private float calculateomega6(float weight, String unit) {
        float omega6Per100g, weightInG;
        omega6Per100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.OMEGA_6_FAT).getFor100g());
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
        return ((omega6Per100g / 100) * weightInG);
    }
    private float calculateomega9(float weight, String unit) {
        float omega9Per100g, weightInG;
        omega9Per100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.OMEGA_9_FAT).getFor100g());
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
        return ((omega9Per100g / 100) * weightInG);
    }
    private float calculatesucrose(float weight, String unit) {
        float sucrPer100g, weightInG;
        sucrPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.SUCROSE).getFor100g());
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
        return ((sucrPer100g / 100) * weightInG);
    }
    private float calculateglucose(float weight, String unit) {
        float glucPer100g, weightInG;
        glucPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.GLUCOSE).getFor100g());
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
        return ((glucPer100g / 100) * weightInG);
    }
    private float calculatefructose(float weight, String unit) {
        float frucPer100g, weightInG;
        frucPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.FRUCTOSE).getFor100g());
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
        return ((frucPer100g / 100) * weightInG);
    }
    private float calculatemaltose(float weight, String unit) {
        float maltPer100g, weightInG;
        maltPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.MALTOSE).getFor100g());
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
        return ((maltPer100g / 100) * weightInG);
    }
    private float calculatealcohol(float weight, String unit) {
        float alcoPer100g, weightInG;
        alcoPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.ALCOHOL).getFor100g());
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
        return ((alcoPer100g / 100) * weightInG);
    }
    private float calculatevita(float weight, String unit) {
        float vitaPer100g, weightInG;
        vitaPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_A).getFor100g());
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
        return ((vitaPer100g / 100) * weightInG);
    }
    private float calculatevitd(float weight, String unit) {
        float vitdPer100g, weightInG;
        vitdPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_D).getFor100g());
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
        return ((vitdPer100g / 100) * weightInG);
    }
    private float calculatevite(float weight, String unit) {
        float vitePer100g, weightInG;
        vitePer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_E).getFor100g());
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
        return ((vitePer100g / 100) * weightInG);
    }
    private float calculatevitk(float weight, String unit) {
        float vitkPer100g, weightInG;
        vitkPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_K).getFor100g());
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
        return ((vitkPer100g / 100) * weightInG);
    }
    private float calculatevitc(float weight, String unit) {
        float vitcPer100g, weightInG;
        vitcPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_C).getFor100g());
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
        return ((vitcPer100g / 100) * weightInG);
    }
    private float calculatevitpp(float weight, String unit) {
        float vitppPer100g, weightInG;
        vitppPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_PP).getFor100g());
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
        return ((vitppPer100g / 100) * weightInG);
    }
    private float calculatevitb1(float weight, String unit) {
        float vitb1Per100g, weightInG;
        vitb1Per100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B1).getFor100g());
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
        return ((vitb1Per100g / 100) * weightInG);
    }
    private float calculatevitb2(float weight, String unit) {
        float vitb2Per100g, weightInG;
        vitb2Per100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B2).getFor100g());
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
        return ((vitb2Per100g / 100) * weightInG);
    }
    private float calculatevitb6(float weight, String unit) {
        float vitb6Per100g, weightInG;
        vitb6Per100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B6).getFor100g());
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
        return ((vitb6Per100g / 100) * weightInG);
    }
    private float calculatevitb9(float weight, String unit) {
        float vitb9Per100g, weightInG;
        vitb9Per100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B9).getFor100g());
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
        return ((vitb9Per100g / 100) * weightInG);
    }
    private float calculatevitb12(float weight, String unit) {
        float vitb12Per100g, weightInG;
        vitb12Per100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B12).getFor100g());
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
        return ((vitb12Per100g / 100) * weightInG);
    }
    private float calculatebiotin(float weight, String unit) {
        float biotinPer100g, weightInG;
        biotinPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.BIOTIN).getFor100g());
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
        return ((biotinPer100g / 100) * weightInG);
    }
    private float calculatesilica(float weight, String unit) {
        float silicaPer100g, weightInG;
        silicaPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.SILICA).getFor100g());
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
        return ((silicaPer100g / 100) * weightInG);
    }
    private float calculatebicarbonate(float weight, String unit) {
        float bicarbPer100g, weightInG;
        bicarbPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.BICARBONATE).getFor100g());
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
        return ((bicarbPer100g / 100) * weightInG);
    }
    private float calculatephosphorus(float weight, String unit) {
        float phosPer100g, weightInG;
        phosPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.PHOSPHORUS).getFor100g());
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
        return ((phosPer100g / 100) * weightInG);
    }
    private float calculatezinc(float weight, String unit) {
        float zincPer100g, weightInG;
        zincPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.ZINC).getFor100g());
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
        return ((zincPer100g / 100) * weightInG);
    }
    private float calculateselenium(float weight, String unit) {
        float selPer100g, weightInG;
        selPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.SELENIUM).getFor100g());
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
        return ((selPer100g / 100) * weightInG);
    }
    private float calculatechromium(float weight, String unit) {
        float chroPer100g, weightInG;
        chroPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.CHROMIUM).getFor100g());
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
        return ((chroPer100g / 100) * weightInG);
    }
    private float calculatemolybdenum(float weight, String unit) {
        float molyPer100g, weightInG;
        molyPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.MOLYBDENUM).getFor100g());
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
        return ((molyPer100g / 100) * weightInG);
    }
    private float calculateiodine(float weight, String unit) {
        float iodinePer100g, weightInG;
        iodinePer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.IODINE).getFor100g());
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
        return ((iodinePer100g / 100) * weightInG);
    }
    private float calculatecaffeine(float weight, String unit) {
        float caffeinePer100g, weightInG;
        caffeinePer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.CAFFEINE).getFor100g());
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
        return ((caffeinePer100g / 100) * weightInG);
    }
    private float calculatechlorophyl(float weight, String unit) {
        float chlorophylPer100g, weightInG;
        chlorophylPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.CHLOROPHYL).getFor100g());
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
        return ((chlorophylPer100g / 100) * weightInG);
    }
    private float calculatecocoa(float weight, String unit) {
        float cocoaPer100g, weightInG;
        cocoaPer100g = Float.valueOf(mState.getProduct().getNutriments().get(Nutriments.COCOA).getFor100g());
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
        return ((cocoaPer100g / 100) * weightInG);
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
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.FAT) == null) {
            menu.findItem(R.id.fat).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.CARBOHYDRATES) == null) {
            menu.findItem(R.id.carbohydrate).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.CHOLESTEROL) == null) {
            menu.findItem(R.id.cholesterol).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.SATURATED_FAT) == null) {
            menu.findItem(R.id.satfat).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.SODIUM) == null) {
            menu.findItem(R.id.sodium).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.SALT) == null) {
            menu.findItem(R.id.salt).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.PROTEINS) == null) {
            menu.findItem(R.id.protein).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.FIBER) == null) {
            menu.findItem(R.id.df).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.SUGARS) == null) {
            menu.findItem(R.id.sugar).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.TRANS_FAT) == null) {
            menu.findItem(R.id.transfat).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.LACTOSE) == null) {
            menu.findItem(R.id.lactose).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.CASEIN) == null) {
            menu.findItem(R.id.casein).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.MALTODEXTRINS) == null) {
            menu.findItem(R.id.maltodextrins).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.SERUM_PROTEINS) == null) {
            menu.findItem(R.id.sp).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.NUCLEOTIDES) == null) {
            menu.findItem(R.id.nucleotides).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.CALCIUM) == null) {
            menu.findItem(R.id.calcium).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.POTASSIUM) == null) {
            menu.findItem(R.id.potassium).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.CHLORIDE) == null) {
            menu.findItem(R.id.chlorine).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.MAGNESIUM) == null) {
            menu.findItem(R.id.magnesium).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.TAURINE) == null) {
            menu.findItem(R.id.taurine).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.MONOUNSATURATED_FAT) == null) {
            menu.findItem(R.id.muf).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.POLYUNSATURATED_FAT) == null) {
            menu.findItem(R.id.puf).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.OMEGA_3_FAT) == null) {
            menu.findItem(R.id.omega3).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.OMEGA_6_FAT) == null) {
            menu.findItem(R.id.omega6).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.OMEGA_9_FAT) == null) {
            menu.findItem(R.id.omega9).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.SUCROSE) == null) {
            menu.findItem(R.id.sucrose).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.GLUCOSE) == null) {
            menu.findItem(R.id.glucose).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.FRUCTOSE) == null) {
            menu.findItem(R.id.fructose).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.MALTOSE) == null) {
            menu.findItem(R.id.maltose).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.ALCOHOL) == null) {
            menu.findItem(R.id.alcohol).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_A) == null) {
            menu.findItem(R.id.vitA).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_D) == null) {
            menu.findItem(R.id.vitD).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_E) == null) {
            menu.findItem(R.id.vitE).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_K) == null) {
            menu.findItem(R.id.vitK).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_C) == null) {
            menu.findItem(R.id.vitC).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B1) == null) {
            menu.findItem(R.id.vitB1).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B2) == null) {
            menu.findItem(R.id.vitB2).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B6) == null) {
            menu.findItem(R.id.vitB6).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B9) == null) {
            menu.findItem(R.id.vitB9).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_B12) == null) {
            menu.findItem(R.id.vitB12).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.VITAMIN_PP) == null) {
            menu.findItem(R.id.vitPP).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.BIOTIN) == null) {
            menu.findItem(R.id.biotin).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.PANTOTHENIC_ACID) == null) {
            menu.findItem(R.id.pa).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.SILICA) == null) {
            menu.findItem(R.id.silica).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.PHOSPHORUS) == null) {
            menu.findItem(R.id.phos).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.BICARBONATE) == null) {
            menu.findItem(R.id.bicarb).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.ZINC) == null) {
            menu.findItem(R.id.zinc).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.IRON) == null) {
            menu.findItem(R.id.iron).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.COPPER) == null) {
            menu.findItem(R.id.copper).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.MANGANESE) == null) {
            menu.findItem(R.id.manganese).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.FLUORIDE) == null) {
            menu.findItem(R.id.fluoride).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.SELENIUM) == null) {
            menu.findItem(R.id.selenium).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.CHROMIUM) == null) {
            menu.findItem(R.id.chromium).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.MOLYBDENUM) == null) {
            menu.findItem(R.id.molybdenum).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.IODINE) == null) {
            menu.findItem(R.id.iodine).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.CAFFEINE) == null) {
            menu.findItem(R.id.caffeine).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.COCOA) == null) {
            menu.findItem(R.id.cocoa).setVisible(false);
        }
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.CHLOROPHYL) == null) {
            menu.findItem(R.id.chlorophyl).setVisible(false);
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
