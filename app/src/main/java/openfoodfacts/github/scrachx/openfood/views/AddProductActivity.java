package openfoodfacts.github.scrachx.openfood.views;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductIngredientsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductNutritionFactsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.AddProductOverviewFragment;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;

public class AddProductActivity extends BaseActivity {

    @BindView(R.id.overview_indicator)
    View overviewIndicator;
    @BindView(R.id.ingredients_indicator)
    View ingredientsIndicator;
    @BindView(R.id.nutrition_facts_indicator)
    View nutritionFactsIndicator;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    Map<String, String> productDetails = new HashMap<>();
    AddProductOverviewFragment addProductOverviewFragment = new AddProductOverviewFragment();
    AddProductIngredientsFragment addProductIngredientsFragment = new AddProductIngredientsFragment();
    AddProductNutritionFactsFragment addProductNutritionFactsFragment = new AddProductNutritionFactsFragment();

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            switch (position) {
                case 0:
                    updateTimelineIndicator(1, 0, 0);
                    break;
                case 1:
                    updateTimelineIndicator(2, 1, 0);
                    break;
                case 2:
                    updateTimelineIndicator(2, 2, 1);
                    break;
                default:
                    updateTimelineIndicator(1, 0, 0);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * This method is used to update the timeline.
     * 0 means inactive stage, 1 means active stage and 2 means completed stage
     *
     * @param overviewStage       change the state of overview indicator
     * @param ingredientsStage    change the state of ingredients indicator
     * @param nutritionFactsStage change the state of nutrition facts indicator
     */

    private void updateTimelineIndicator(int overviewStage, int ingredientsStage, int nutritionFactsStage) {

        switch (overviewStage) {
            case 0:
                overviewIndicator.setBackgroundResource(R.drawable.stage_inactive);
                break;
            case 1:
                overviewIndicator.setBackgroundResource(R.drawable.stage_active);
                break;
            case 2:
                overviewIndicator.setBackgroundResource(R.drawable.stage_complete);
                break;
        }

        switch (ingredientsStage) {
            case 0:
                ingredientsIndicator.setBackgroundResource(R.drawable.stage_inactive);
                break;
            case 1:
                ingredientsIndicator.setBackgroundResource(R.drawable.stage_active);
                break;
            case 2:
                ingredientsIndicator.setBackgroundResource(R.drawable.stage_complete);
                break;
        }

        switch (nutritionFactsStage) {
            case 0:
                nutritionFactsIndicator.setBackgroundResource(R.drawable.stage_inactive);
                break;
            case 1:
                nutritionFactsIndicator.setBackgroundResource(R.drawable.stage_active);
                break;
            case 2:
                nutritionFactsIndicator.setBackgroundResource(R.drawable.stage_complete);
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        setTitle(R.string.offline_product_addition_title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ProductFragmentPagerAdapter adapterResult = new ProductFragmentPagerAdapter(getSupportFragmentManager());
        adapterResult.addFragment(addProductOverviewFragment, "Overview");
        adapterResult.addFragment(addProductIngredientsFragment, "Ingredients");
        adapterResult.addFragment(addProductNutritionFactsFragment, "Nutrition Facts");
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapterResult);

    }

    private void saveProduct() {
        addProductOverviewFragment.getDetails();
        addProductIngredientsFragment.getDetails();
        for (Map.Entry<String, String> entry : productDetails.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Log.e(key, value);
        }
    }

    public void proceed() {
        switch (viewPager.getCurrentItem()) {
            case 0:
                viewPager.setCurrentItem(1, true);
                break;
            case 1:
                viewPager.setCurrentItem(2, true);
                break;
            case 3:
                //saveProduct();
                break;
        }
    }

    @OnClick(R.id.overview_indicator)
    void switchToOverviewPage() {
        viewPager.setCurrentItem(0, true);
    }

    @OnClick(R.id.ingredients_indicator)
    void switchToIngredientsPage() {
        viewPager.setCurrentItem(1, true);
    }

    @OnClick(R.id.nutrition_facts_indicator)
    void switchToNutritionFactsPage() {
        viewPager.setCurrentItem(2, true);
    }

    public void addToMap(String key, String value) {
        productDetails.put(key, value);
    }
}
