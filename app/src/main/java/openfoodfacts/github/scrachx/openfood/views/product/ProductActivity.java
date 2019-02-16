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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.ContributorsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.ProductPhotosFragment;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.BottomNavigationBehavior;
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;
import openfoodfacts.github.scrachx.openfood.views.HistoryScanActivity;
import openfoodfacts.github.scrachx.openfood.views.LoginActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsRecyclerViewAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.product.environment.EnvironmentProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients.IngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.IngredientsAnalysisProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.nutrition.NutritionProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductFragment;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class ProductActivity extends BaseActivity implements OnRefreshListener {

	private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
	private static final int EDIT_REQUEST_CODE = 2;
	@BindView( R.id.pager )
	ViewPager viewPager;
	@BindView( R.id.toolbar )
	Toolbar toolbar;
	@BindView( R.id.tabs )
	TabLayout tabLayout;
	@BindView( R.id.buttonScan )
	FloatingActionButton mButtonScan;
	@BindView( R.id.bottom_navigation )
	BottomNavigationView bottomNavigationView;

    RecyclerView productBrowsingRecyclerView;
    ProductFragmentPagerAdapter adapterResult;
    ProductsRecyclerViewAdapter productsRecyclerViewAdapter;

    private OpenFoodAPIClient api;
    private ShareActionProvider mShareActionProvider;
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

		api = new OpenFoodAPIClient( this );

		mState = (State) getIntent().getExtras().getSerializable( "state" );

		setupViewPager( viewPager );

		tabLayout.setupWithViewPager( viewPager );

		if( !Utils.isHardwareCameraInstalled( this ) )
		{
			mButtonScan.setVisibility( View.GONE );
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
					final SharedPreferences settings = getSharedPreferences( "login", 0 );
					final String login = settings.getString( "user", "" );
					if( login.isEmpty() )
					{
						new MaterialDialog.Builder( ProductActivity.this )
								.title( R.string.sign_in_to_edit )
								.positiveText( R.string.txtSignIn )
								.negativeText( R.string.dialog_cancel )
								.onPositive( ( dialog, which ) -> {
									Intent intent = new Intent( ProductActivity.this, LoginActivity.class );
									startActivityForResult( intent, LOGIN_ACTIVITY_REQUEST_CODE );
									dialog.dismiss();
								} )
								.onNegative( ( dialog, which ) -> dialog.dismiss() )
								.build().show();
					}
					else
					{
						mState = (State) getIntent().getExtras().getSerializable( "state" );
						Intent intent = new Intent( ProductActivity.this, AddProductActivity.class );
						intent.putExtra( "edit_product", mState.getProduct() );
						startActivityForResult( intent, EDIT_REQUEST_CODE );
					}
					break;

                case R.id.history_bottom_nav:
                    startActivity(new Intent(this, HistoryScanActivity.class));
                    break;

                case R.id.search_product:
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("product_search", true);
                    startActivity(intent);
                    break;

				default:
					return true;
			}
			return true;
		} );
		CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
		layoutParams.setBehavior( new BottomNavigationBehavior() );

		//To update the product details
		onRefresh();
	}

	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		super.onActivityResult( requestCode, resultCode, data );
		if( requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK )
		{
			Intent intent = new Intent( ProductActivity.this, AddProductActivity.class );
			intent.putExtra( "edit_product", mState.getProduct() );
			startActivity( intent );
		}
		if( requestCode == EDIT_REQUEST_CODE && resultCode == RESULT_OK)
		{
			onRefresh();
		}
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

	private void setupViewPager( ViewPager viewPager )
	{
		String[] menuTitles = getResources().getStringArray( R.array.nav_drawer_items_product );
		String[] newMenuTitles=getResources().getStringArray(R.array.nav_drawer_new_items_product);

		adapterResult = new ProductFragmentPagerAdapter( getSupportFragmentManager() );
		adapterResult.addFragment( new SummaryProductFragment(), menuTitles[0] );
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( this );
        if( BuildConfig.FLAVOR.equals( "off" ) || BuildConfig.FLAVOR.equals( "obf" ) || BuildConfig.FLAVOR.equals( "opff" ) )
        {
            adapterResult.addFragment( new IngredientsProductFragment(), menuTitles[1] );
        }
		if( BuildConfig.FLAVOR.equals( "off" ) )
		{
			adapterResult.addFragment( new NutritionProductFragment(), menuTitles[2] );
            if( (mState.getProduct().getNutriments() != null &&
                    mState.getProduct().getNutriments().contains(Nutriments.CARBON_FOOTPRINT)) ||
                    (mState.getProduct().getEnvironmentInfocard() != null && !mState.getProduct().getEnvironmentInfocard().isEmpty()))
            {
                adapterResult.addFragment(new EnvironmentProductFragment(), "Environment");
            }
			if( PreferenceManager.getDefaultSharedPreferences( this ).getBoolean( "photoMode", false ) )
			{
				adapterResult.addFragment( new ProductPhotosFragment(), newMenuTitles[0] );
			}
		}
		if( BuildConfig.FLAVOR.equals( "opff" ) )
		{
			adapterResult.addFragment( new NutritionProductFragment(), menuTitles[2] );
			if( PreferenceManager.getDefaultSharedPreferences( this ).getBoolean( "photoMode", false ) )
			{
				adapterResult.addFragment( new ProductPhotosFragment(), newMenuTitles[0] );
			}
		}

		if( BuildConfig.FLAVOR.equals( "obf" ) )
		{
			if( PreferenceManager.getDefaultSharedPreferences( this ).getBoolean( "photoMode", false ) )
			{
				adapterResult.addFragment( new ProductPhotosFragment(), newMenuTitles[0] );
			}
			adapterResult.addFragment( new IngredientsAnalysisProductFragment(), newMenuTitles[1] );
		}

		if( BuildConfig.FLAVOR.equals( "opf" ) )
		{
			adapterResult.addFragment( new ProductPhotosFragment(), newMenuTitles[0] );
		}
        if( preferences.getBoolean( "contributionTab", false ) )
        {
            adapterResult.addFragment( new ContributorsFragment(), getString( R.string.contribution_tab ) );
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

            case R.id.action_facts:

                MaterialDialog.Builder builder = new MaterialDialog.Builder(this)
                        .title(R.string.calculate_nutrition_facts)
                        .customView(R.layout.dialog_calculate_calories, false)
                        .dismissListener(dialogInterface -> Utils.hideKeyboard(ProductActivity.this));
                MaterialDialog dialog = builder.build();
                dialog.show();
                View view = dialog.getCustomView();
                if (view != null) {
                    EditText etWeight = view.findViewById(R.id.edit_text_weight);
                    Spinner spinner = view.findViewById(R.id.spinner_weight);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            Button btn = (Button) dialog.findViewById(R.id.txt_calories_result);
                            btn.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    if (!TextUtils.isEmpty(etWeight.getText().toString())) {

                                        String SpinnerValue = (String) spinner.getSelectedItem();
                                        String weight = etWeight.getText().toString();
                                        Product p = mState.getProduct();
                                        Intent intent = new Intent(getApplicationContext(), CalculateDetails.class);
                                        intent.putExtra("sampleObject", p);
                                        intent.putExtra("spinnervalue", SpinnerValue);
                                        intent.putExtra("weight", weight);
                                        startActivity(intent);
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_enter_weight), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product, menu);

/*
Hide the 'Calculate Calories' option from the overflow menu if the product
doesn't have calories information in nutrition facts.
*/
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.ENERGY) == null) {
            menu.findItem(R.id.action_facts).setVisible(false);
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

    public void showBottomScreen(JSONObject result, AdditiveName additive) {
        showBottomSheet(result, additive.getId(), additive.getName(), additive.getWikiDataId(),
                SearchType.ADDITIVE, "additive_details_fragment");
    }

    public void showBottomScreen(JSONObject result, LabelName label) {
        showBottomSheet(result, label.getId(), label.getName(), label.getWikiDataId(),
                SearchType.LABEL, "label_details_fragment");
    }

    public void showBottomScreen(JSONObject result, CategoryName category) {
        showBottomSheet(result, category.getId(), category.getName(), category.getWikiDataId(),
                SearchType.CATEGORY, "category_details_fragment");
    }

    public void showBottomScreen(JSONObject result, AllergenName allergen) {
        showBottomSheet(result, allergen.getId(), allergen.getName(), allergen.getWikiDataId(),
                SearchType.ALLERGEN, "allergen_details_fragment");
    }

    private void showBottomSheet(JSONObject result, Long id, String name,
                                 String wikidataId, String searchType, String fragmentTag) {
        try {
            String jsonObjectStr = (result != null) ? result.getJSONObject("entities")
                    .getJSONObject(wikidataId).toString() : null;
            ProductAttributeDetailsFragment fragment =
                    ProductAttributeDetailsFragment.newInstance(jsonObjectStr, id, searchType, name);
            fragment.show(getSupportFragmentManager(), fragmentTag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        api.getProduct(mState.getProduct().getCode(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        mState = (State) intent.getExtras().getSerializable("state");
        adapterResult.refresh(mState);
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
