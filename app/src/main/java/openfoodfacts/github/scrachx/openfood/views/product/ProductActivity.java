/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.views.product;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import openfoodfacts.github.scrachx.openfood.AppFlavors;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductBinding;
import openfoodfacts.github.scrachx.openfood.fragments.ContributorsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.ProductPhotosFragment;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.ProductState;
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.CommonBottomListenerInstaller;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.product.environment.EnvironmentProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients.IngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.IngredientsAnalysisProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.nutrition.NutritionProductFragment;
import openfoodfacts.github.scrachx.openfood.views.product.summary.SummaryProductFragment;

import static openfoodfacts.github.scrachx.openfood.views.product.ProductActivity.ShowIngredientsAction.PERFORM_OCR;
import static openfoodfacts.github.scrachx.openfood.views.product.ProductActivity.ShowIngredientsAction.SEND_UPDATED;

public class ProductActivity extends BaseActivity implements OnRefreshListener {
    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    public static final String STATE_KEY = "state";
    private ActivityProductBinding binding;
    private ProductFragmentPagerAdapter adapterResult;
    private OpenFoodAPIClient client;
    private CompositeDisposable disp = new CompositeDisposable();
    private ProductState productState;

    public static void start(Context context, @NonNull ProductState productState) {
        Intent starter = new Intent(context, ProductActivity.class);
        starter.putExtra(STATE_KEY, productState);
        context.startActivity(starter);
    }

    /**
     * CAREFUL ! YOU MUST INSTANTIATE YOUR OWN ADAPTERRESULT BEFORE CALLING THIS METHOD
     */
    @NonNull
    public static ProductFragmentPagerAdapter setupViewPager(@NonNull ViewPager2 viewPager,
                                                             @NonNull ProductFragmentPagerAdapter adapter,
                                                             @NonNull ProductState productState,
                                                             @NonNull Activity activity) {

        String[] menuTitles = activity.getResources().getStringArray(R.array.nav_drawer_items_product);
        String[] newMenuTitles = activity.getResources().getStringArray(R.array.nav_drawer_new_items_product);

        Bundle fBundle = new Bundle();
        fBundle.putSerializable(STATE_KEY, productState);

        adapter.addFragment(FragmentUtils.applyBundle(new SummaryProductFragment(), fBundle), menuTitles[0]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        // Add Ingredients fragment for off, obf and opff
        if (AppFlavors.isFlavors(AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF)) {
            adapter.addFragment(FragmentUtils.applyBundle(new IngredientsProductFragment(), fBundle), menuTitles[1]);
        }

        if (AppFlavors.isFlavors(AppFlavors.OFF)) {
            adapter.addFragment(FragmentUtils.applyBundle(new NutritionProductFragment(), fBundle), menuTitles[2]);
            if ((productState.getProduct().getNutriments() != null &&
                productState.getProduct().getNutriments().contains(Nutriments.CARBON_FOOTPRINT)) ||
                (productState.getProduct().getEnvironmentInfocard() != null && !productState.getProduct().getEnvironmentInfocard().isEmpty())) {
                adapter.addFragment(FragmentUtils.applyBundle(new EnvironmentProductFragment(), fBundle), "Environment");
            }
            if (isPhotoMode(activity)) {
                adapter.addFragment(FragmentUtils.applyBundle(new ProductPhotosFragment(), fBundle), newMenuTitles[0]);
            }
        } else if (AppFlavors.isFlavors(AppFlavors.OPFF)) {
            adapter.addFragment(FragmentUtils.applyBundle(new NutritionProductFragment(), fBundle), menuTitles[2]);
            if (isPhotoMode(activity)) {
                adapter.addFragment(FragmentUtils.applyBundle(new ProductPhotosFragment(), fBundle), newMenuTitles[0]);
            }
        } else if (AppFlavors.isFlavors(AppFlavors.OBF)) {
            if (isPhotoMode(activity)) {
                adapter.addFragment(FragmentUtils.applyBundle(new ProductPhotosFragment(), fBundle), newMenuTitles[0]);
            }
            adapter.addFragment(FragmentUtils.applyBundle(new IngredientsAnalysisProductFragment(), fBundle), newMenuTitles[1]);
        } else if (AppFlavors.isFlavors(AppFlavors.OPF)) {
            adapter.addFragment(FragmentUtils.applyBundle(new ProductPhotosFragment(), fBundle), newMenuTitles[0]);
        }

        if (preferences.getBoolean("contributionTab", false)) {
            adapter.addFragment(FragmentUtils.applyBundle(new ContributorsFragment(), fBundle), activity.getString(R.string.contribution_tab));
        }

        viewPager.setAdapter(adapter);
        return adapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        binding = ActivityProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.app_name_long));

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        client = new OpenFoodAPIClient(this);

        productState = (ProductState) getIntent().getSerializableExtra(STATE_KEY);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            // handle opening the app via product page url
            Uri data = getIntent().getData();
            String[] paths = data.toString().split("/"); // paths[4]
            productState = new ProductState();
            loadProductDataFromUrl(paths[4]);
        } else if (productState == null) {
            //no state-> we can't display anything. we go back to home.
            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else {
            initViews();
        }
    }

    /**
     * Get the product data from the barcode. This takes the barcode and retrieves the information.
     *
     * @param barcode from the URL.
     */
    private void loadProductDataFromUrl(String barcode) {

        disp.add(client.getProductStateFull(barcode, Utils.HEADER_USER_AGENT_SCAN)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(state -> {
                productState = state;
                getIntent().putExtra(STATE_KEY, state);
                if (productState != null) {
                    initViews();
                } else {
                    finish();
                }
            }, e -> {
                Log.i(getClass().getSimpleName(), "Failed to load product data", e);
                finish();
            }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Open product editing after successful login
            Intent intent = new Intent(ProductActivity.this, AddProductActivity.class);
            intent.putExtra(AddProductActivity.KEY_EDIT_PRODUCT, productState.getProduct());
            startActivity(intent);
        }
    }

    /**
     * Initialise the content that shows the content on the device.
     */
    private void initViews() {

        adapterResult = setupViewPager(binding.pager);

        new TabLayoutMediator(binding.tabs, binding.pager, (tab, position) -> {
            tab.setText(adapterResult.getPageTitle(position));
        }).attach();

        CommonBottomListenerInstaller.selectNavigationItem(binding.navigationBottomInclude.bottomNavigation, 0);
        CommonBottomListenerInstaller.install(this, binding.navigationBottomInclude.bottomNavigation);
    }

    private ProductFragmentPagerAdapter setupViewPager(ViewPager2 viewPager) {
        return setupViewPager(viewPager, new ProductFragmentPagerAdapter(this), productState, this);
    }

    private static boolean isPhotoMode(Activity activity) {
        return PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("photoMode", false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return onOptionsItemSelected(item, this);
    }

    public static boolean onOptionsItemSelected(MenuItem item, Activity activity) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            activity.finish();
        }
        return true;
    }

    @Subscribe
    public void onEventBusProductNeedsRefreshEvent(ProductNeedsRefreshEvent event) {
        if (event.getBarcode().equals(productState.getProduct().getCode())) {
            onRefresh();
        }
    }

    @Override
    public void onRefresh() {
        client.openProduct(productState.getProduct().getCode(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        productState = (ProductState) intent.getSerializableExtra(STATE_KEY);
        adapterResult.refresh(productState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        disp.dispose();
        super.onDestroy();
    }

    public void showIngredientsTab(ShowIngredientsAction action) {
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

    public enum ShowIngredientsAction {
        PERFORM_OCR, SEND_UPDATED,
    }
}
