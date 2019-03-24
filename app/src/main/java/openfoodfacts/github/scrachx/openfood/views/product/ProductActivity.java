package openfoodfacts.github.scrachx.openfood.views.product;

import android.Manifest;
import android.app.Activity;
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
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;


import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.ContributorsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.ProductPhotosFragment;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.BottomNavigationBehavior;
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;
import openfoodfacts.github.scrachx.openfood.views.HistoryScanActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsRecyclerViewAdapter;
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
                case R.id.scan_bottom_nav:
                    onScanButtonClicked(this);
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

    public static void onScanButtonClicked(Activity activity) {
        if (Utils.isHardwareCameraInstalled(activity)) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                    new MaterialDialog.Builder(activity)
                            .title(R.string.action_about)
                            .content(R.string.permission_camera)
                            .neutralText(R.string.txtOk)
                            .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                            .show();
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                Intent intent = new Intent(activity, ContinuousScanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
            }
        }
    }

	private void setupViewPager( ViewPager viewPager )
	{
        adapterResult = new ProductFragmentPagerAdapter(getSupportFragmentManager());
		adapterResult = setupViewPager(viewPager, adapterResult, mState, this);
    }

    /**
     * CAREFUL ! YOU MUST INSTANTIATE YOUR OWN ADAPTERRESULT BEFORE CALLING THIS METHOD
     * @param viewPager
     * @param adapterResult
     * @param mState
     * @param activity
     * @return
     */
    public static ProductFragmentPagerAdapter setupViewPager (ViewPager viewPager, ProductFragmentPagerAdapter adapterResult, State mState, Activity activity) {
        String[] menuTitles = activity.getResources().getStringArray( R.array.nav_drawer_items_product );
        String[] newMenuTitles=activity.getResources().getStringArray(R.array.nav_drawer_new_items_product);

        adapterResult.addFragment( new SummaryProductFragment(), menuTitles[0] );
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( activity );
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
            if( PreferenceManager.getDefaultSharedPreferences( activity ).getBoolean( "photoMode", false ) )
            {
                adapterResult.addFragment( new ProductPhotosFragment(), newMenuTitles[0] );
            }
        }
        if( BuildConfig.FLAVOR.equals( "opff" ) )
        {
            adapterResult.addFragment( new NutritionProductFragment(), menuTitles[2] );
            if( PreferenceManager.getDefaultSharedPreferences( activity ).getBoolean( "photoMode", false ) )
            {
                adapterResult.addFragment( new ProductPhotosFragment(), newMenuTitles[0] );
            }
        }

        if( BuildConfig.FLAVOR.equals( "obf" ) )
        {
            if( PreferenceManager.getDefaultSharedPreferences( activity ).getBoolean( "photoMode", false ) )
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
            adapterResult.addFragment( new ContributorsFragment(), activity.getString( R.string.contribution_tab ) );
        }

        viewPager.setAdapter(adapterResult);
        return adapterResult;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onOptionsItemSelected(item, mState, this);
    }

    public static boolean onOptionsItemSelected(MenuItem item, State mState, Activity activity) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                activity.finish();
                return true;

            default:
                return true;
        }
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
