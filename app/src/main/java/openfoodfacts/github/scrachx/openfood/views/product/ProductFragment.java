package openfoodfacts.github.scrachx.openfood.views.product;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class ProductFragment extends Fragment implements OnRefreshListener {

    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    public static State productState;//NOSONAR To be changed ASAP !
    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    ProductFragmentPagerAdapter adapterResult;
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
        productState = (State) getArguments().getSerializable("state");

        setupViewPager(viewPager);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            viewPager.setNestedScrollingEnabled(true);
        }

        tabLayout.setupWithViewPager(viewPager);

        api = new OpenFoodAPIClient(getActivity());

        // Get the user preference for scan on shake feature and open ContinuousScanActivity if the user has enabled the feature
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);

        mShakeDetector.setOnShakeListener(count -> {

            if (scanOnShake) {
                Utils.scan(getActivity());
            }
        });
        BottomNavigationListenerInstaller.install(bottomNavigationView,getActivity(),getContext());
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(getActivity(), AddProductActivity.class);
            intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, productState.getProduct());
            startActivity(intent);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        adapterResult = ProductActivity.setupViewPager(viewPager, new ProductFragmentPagerAdapter(getChildFragmentManager()), productState, getActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return ProductActivity.onOptionsItemSelected(item, getActivity());
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }


    @Override
    public void onRefresh() {
        api.getProductFull(productState.getProduct().getCode()).enqueue(new Callback<State>() {
            @Override
            public void onResponse(@NonNull Call<State> call, @NonNull Response<State> response) {
                final State s = response.body();
                productState = s;
                adapterResult.refresh(s);
            }

            @Override
            public void onFailure(@NonNull Call<State> call, @NonNull Throwable t) {
                adapterResult.refresh(productState);
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
