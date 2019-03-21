package openfoodfacts.github.scrachx.openfood.views.product;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.HistoryScanActivity;
import openfoodfacts.github.scrachx.openfood.views.LoginActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsRecyclerViewAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class ProductFragment extends Fragment implements OnRefreshListener {

    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    public static State mState;
    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.buttonScan)
    FloatingActionButton mButtonScan;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    RecyclerView productBrowsingRecyclerView;
    ProductFragmentPagerAdapter adapterResult;
    ProductsRecyclerViewAdapter productsRecyclerViewAdapter;
    private OpenFoodAPIClient api;
    private ShareActionProvider mShareActionProvider;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // boolean to determine if scan on shake feature should be enabled
    private boolean scanOnShake;

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_product, container, false);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        ButterKnife.bind(this, view);
        toolbar.setVisibility(View.GONE);
        mButtonScan.setVisibility(View.GONE);

        mState = (State) getArguments().getSerializable("state");

        setupViewPager(viewPager);

        viewPager.setNestedScrollingEnabled(true);

        tabLayout.setupWithViewPager(viewPager);

        api = new OpenFoodAPIClient(getActivity());

        if (!Utils.isHardwareCameraInstalled(getContext())) {
            mButtonScan.setVisibility(View.GONE);
        }

        // Get the user preference for scan on shake feature and open ContinuousScanActivity if the user has enabled the feature
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);

        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeDetected() {
            @Override
            public void onShake(int count) {

                if (scanOnShake) {
                    Utils.scan(getActivity());
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
                    final SharedPreferences settings = getActivity().getSharedPreferences("login", 0);
                    final String login = settings.getString("user", "");
                    if (login.isEmpty()) {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.sign_in_to_edit)
                                .positiveText(R.string.txtSignIn)
                                .negativeText(R.string.dialog_cancel)
                                .onPositive((dialog, which) -> {
                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                    startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
                                    dialog.dismiss();
                                })
                                .onNegative((dialog, which) -> dialog.dismiss())
                                .build().show();
                    } else {
                        Intent intent = new Intent(getActivity(), AddProductActivity.class);
                        intent.putExtra("edit_product", mState.getProduct());
                        startActivity(intent);
                    }
                    break;

                case R.id.history_bottom_nav:
                    startActivity(new Intent(getActivity(), HistoryScanActivity.class));
                    break;

                case R.id.search_product:
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.putExtra("product_search", true);
                    startActivity(intent);
                    break;

                case R.id.home:
                    getActivity().onBackPressed();
                    break;

                default:
                    return true;
            }
            return true;
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(getActivity(), AddProductActivity.class);
            intent.putExtra("edit_product", mState.getProduct());
            startActivity(intent);
        }
    }

    @OnClick(R.id.buttonScan)
    protected void OnScan() {
        ProductActivity.onScanButtonClicked(getActivity());
    }

    private void setupViewPager(ViewPager viewPager) {
        adapterResult = new ProductFragmentPagerAdapter(getChildFragmentManager());
        adapterResult = ProductActivity.setupViewPager(viewPager, adapterResult, mState, getActivity());
    }

    /**
     * This method is used to hide share_item and edit_product in App Bar
     */

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem share_item = menu.findItem(R.id.menu_item_share);
        share_item.setVisible(false);
        MenuItem edit_product = menu.findItem(R.id.action_edit_product);
        edit_product.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return ProductActivity.onOptionsItemSelected(item, mState, getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_product, menu);

/*
Hide the 'Calculate Calories' option from the overflow menu if the product
doesn't have calories information in nutrition facts.
*/
        if (mState.getProduct().getNutriments() == null || mState.getProduct().getNutriments().get(Nutriments.ENERGY) == null) {
            menu.findItem(R.id.action_facts).setVisible(false);
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
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
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
        api.getAPIService().getFullProductByBarcode(mState.getProduct().getCode(), Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)).enqueue(new Callback<State>() {
            @Override
            public void onResponse(@NonNull Call<State> call, @NonNull Response<State> response) {
                final State s = response.body();
                mState = s;
                adapterResult.refresh(s);
            }

            @Override
            public void onFailure(@NonNull Call<State> call, @NonNull Throwable t) {
                adapterResult.refresh(mState);
            }
        });
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

    public void bottomSheetWillGrow() {
        if (adapterResult == null || adapterResult.getCount() == 0) {
            return;
        }
        // without this, the view can be centered vertically on initial show. we force the scroll to top !
        if (adapterResult.getItem(0) instanceof SummaryProductFragment) {
            SummaryProductFragment productFragment = (SummaryProductFragment) adapterResult.getItem(0);
            if(productFragment.scrollView!=null) {
                productFragment.scrollView.scrollTo(0, 0);
            }
        }
    }
}
