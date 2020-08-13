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
import static openfoodfacts.github.scrachx.openfood.views.product.ProductActivity.ShowIngredientsAction.PERFORM_OCR;
import static openfoodfacts.github.scrachx.openfood.views.product.ProductActivity.ShowIngredientsAction.SEND_UPDATED;

public class ProductFragment extends Fragment implements OnRefreshListener {
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    private ActivityProductBinding binding;
    private ProductFragmentPagerAdapter adapterResult;
    private OpenFoodAPIClient client;
    private CompositeDisposable disp = new CompositeDisposable();
    private ProductState productState;

    @NonNull
    public static ProductFragment newInstance(@NonNull ProductState productState) {

        Bundle args = new Bundle();
        args.putSerializable(ProductActivity.STATE_KEY, productState);

        ProductFragment fragment = new ProductFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroy() {
        disp.dispose();
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getResources().getBoolean(R.bool.portrait_only)) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        binding = ActivityProductBinding.inflate(inflater);
        binding.toolbar.setVisibility(View.GONE);

        client = new OpenFoodAPIClient(requireActivity());

        productState = (ProductState) requireArguments().getSerializable(ProductActivity.STATE_KEY);

        adapterResult = setupViewPager(binding.pager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.pager.setNestedScrollingEnabled(true);
        }

        new TabLayoutMediator(binding.tabs, binding.pager, (tab, position) ->
            tab.setText(adapterResult.getPageTitle(position)))
            .attach();

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

    private ProductFragmentPagerAdapter setupViewPager(ViewPager2 viewPager) {
        return ProductActivity.setupViewPager(viewPager, new ProductFragmentPagerAdapter(requireActivity()), getProductState(), requireActivity());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return ProductActivity.onOptionsItemSelected(item, getActivity());
    }

    @Override
    public void onRefresh() {
        disp.add(client.getProductStateFull(productState.getProduct().getCode())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(newState -> {
                productState = newState;
                adapterResult.refresh(newState);
            }, throwable ->
                adapterResult.refresh(productState))
        );
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

    public void showIngredientsTab(ProductActivity.ShowIngredientsAction action) {
        if (adapterResult == null || adapterResult.getItemCount() == 0) {
            return;
        }
        for (int i = 0; i < adapterResult.getItemCount(); ++i) {
            Fragment fragment = adapterResult.createFragment(i);
            if (fragment instanceof IngredientsProductFragment) {
                binding.pager.setCurrentItem(i);

                if (action == PERFORM_OCR) {
                    ((IngredientsProductFragment) fragment).extractIngredients();
                } else if (action == SEND_UPDATED) {
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
