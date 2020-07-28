package openfoodfacts.github.scrachx.openfood.views.product;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductBinding;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients.IngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductFragment;

import static android.app.Activity.RESULT_OK;

public class ProductFragment extends Fragment implements OnRefreshListener {
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    private ActivityProductBinding binding;
    private State productState;
    private ProductFragmentPagerAdapter adapterResult;
    private OpenFoodAPIClient api;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private CompositeDisposable disp = new CompositeDisposable();

    @Override
    public void onDestroy() {
        disp.dispose();
        super.onDestroy();
    }

    // boolean to determine if scan on shake feature should be enabled
    private boolean scanOnShake;

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityProductBinding.inflate(inflater);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        binding.toolbar.setVisibility(View.GONE);
        productState = (State) getArguments().getSerializable("state");

        setupViewPager(binding.pager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.pager.setNestedScrollingEnabled(true);
        }

        new TabLayoutMediator(binding.tabs, binding.pager, (tab, position) -> {
            tab.setText(adapterResult.getPageTitle(position));
        }).attach();

        api = new OpenFoodAPIClient(getActivity());

        // Get the user preference for scan on shake feature and open ContinuousScanActivity if the user has enabled the feature
        mSensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);

        mShakeDetector.setOnShakeListener(count -> {

            if (scanOnShake) {
                Utils.scan(requireActivity());
            }
        });

        BottomNavigationListenerInstaller.selectNavigationItem(binding.navigationBottomInclude.bottomNavigation, 0);
        BottomNavigationListenerInstaller.install(binding.navigationBottomInclude.bottomNavigation, getActivity());
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(getActivity(), AddProductActivity.class);
            intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, getProductState().getProduct());
            startActivity(intent);
        }
    }

    private void setupViewPager(ViewPager2 viewPager) {
        adapterResult = ProductActivity.setupViewPager(viewPager, new ProductFragmentPagerAdapter(requireActivity()), getProductState(), requireActivity());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return ProductActivity.onOptionsItemSelected(item, getActivity());
    }

    @Override
    public void onRefresh() {
        disp.add(api.getProductStateFull(getProductState().getProduct().getCode())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(newState -> {
                productState = newState;
                adapterResult.refresh(newState);
            }, throwable ->
                adapterResult.refresh(getProductState())));
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
        if (adapterResult == null || adapterResult.getItemCount() == 0) {
            return;
        }
        // without this, the view can be centered vertically on initial show. we force the scroll to top !
        if (adapterResult.createFragment(0) instanceof SummaryProductFragment) {
            SummaryProductFragment productFragment = (SummaryProductFragment) adapterResult.createFragment(0);
            productFragment.resetScroll();
        }
    }

    public void goToIngredients(String action) {
        if (adapterResult == null || adapterResult.getItemCount() == 0) {
            return;
        }
        for (int i = 0; i < adapterResult.getItemCount(); ++i) {
            Fragment fragment = adapterResult.createFragment(i);
            if (fragment instanceof IngredientsProductFragment) {
                binding.pager.setCurrentItem(i);

                if ("perform_ocr".equals(action)) {
                    ((IngredientsProductFragment) fragment).extractIngredients();
                } else if ("send_updated".equals(action)) {
                    ((IngredientsProductFragment) fragment).changeIngImage();
                }
                return;
            }
        }
    }

    public State getProductState() {
        return productState;
    }
}
