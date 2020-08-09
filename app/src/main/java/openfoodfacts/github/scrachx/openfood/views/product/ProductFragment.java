package openfoodfacts.github.scrachx.openfood.views.product;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductBinding;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients.IngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductFragment;

import static android.app.Activity.RESULT_OK;

public class ProductFragment extends Fragment implements OnRefreshListener {
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    private static final String STATE_ARG = "state";
    private ProductFragmentPagerAdapter adapterResult;
    private OpenFoodAPIClient client;
    private ActivityProductBinding binding;
    private CompositeDisposable disp = new CompositeDisposable();
    private ProductState productState;

    @NonNull
    public static ProductFragment newInstance(@NonNull ProductState productState) {

        Bundle args = new Bundle();
        args.putSerializable(STATE_ARG, productState);

        ProductFragment fragment = new ProductFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroy() {
        disp.dispose();
        super.onDestroy();
    }

    @Nullable
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityProductBinding.inflate(inflater);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        binding.toolbar.setVisibility(View.GONE);
        productState = (ProductState) requireArguments().getSerializable(STATE_ARG);

        setupViewPager(binding.pager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.pager.setNestedScrollingEnabled(true);
        }

        new TabLayoutMediator(binding.tabs, binding.pager, (tab, position) ->
            tab.setText(adapterResult.getPageTitle(position)))
            .attach();

        client = new OpenFoodAPIClient(requireActivity());

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
        disp.add(client.getProductStateFull(getProductState().getProduct().getCode())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(newState -> {
                productState = newState;
                adapterResult.refresh(newState);
            }, throwable ->
                adapterResult.refresh(getProductState())));
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

    public ProductState getProductState() {
        return productState;
    }
}
