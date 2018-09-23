package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.CategoryNameDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.ShakeDetector;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsRecyclerViewAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.EndlessRecyclerViewScrollListener;
import openfoodfacts.github.scrachx.openfood.views.listeners.RecyclerItemClickListener;

public class ProductBrowsingListActivity extends BaseActivity {

    private static String SEARCH_TYPE = "search_type";

    private static String SEARCH_QUERY = "search_query";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.products_recycler_view)
    RecyclerView productsRecyclerView;
    @BindView(R.id.textCountProduct)
    TextView countProductsView;
    @BindView(R.id.offlineCloudLinearLayout)
    LinearLayout offlineCloudLayout;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.noResultsLayout)
    LinearLayout noResultsLayout;
    @BindView(R.id.textNoResults)
    TextView textNoResults;
    @BindView(R.id.textExtendSearch)
    TextView textExtendSearch;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    String searchQuery;
    private String searchType;
    private EndlessRecyclerViewScrollListener scrollListener;
    private List<Product> mProducts;
    private OpenFoodAPIClient api;
    private OpenFoodAPIClient apiClient;
    private int mCountProducts = 0;
    private int pageAddress = 1;
    private Boolean setupDone = false;
    //boolean to determine if image should be loaded or not
    private boolean isLowBatteryMode = false;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    // boolean to determine if scan on shake feature should be enabled
    private boolean scanOnShake;
    private int contributionType;
    private CategoryNameDao mCategoryNameDao;


    public static void startActivity(Context context, String searchQuery, @SearchType String type) {
        Intent intent = new Intent(context, ProductBrowsingListActivity.class);
        intent.putExtra(SEARCH_QUERY, searchQuery);
        intent.putExtra(SEARCH_TYPE, type);
        context.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                searchQuery = query;
                newSearchQuery();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat
                .OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getSupportActionBar().setTitle(null);
                finish();
                return true;
            }
        });

        if (SearchType.CONTRIBUTOR.equals(searchType)) {
            MenuItem contributionItem = menu.findItem(R.id.action_set_type);
            contributionItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        if (item.getItemId() == R.id.action_set_type) {

            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            builder.title(R.string.show_by);
            String[] contributionTypes = new String[]{getString(R.string.products_added),
                    getString(R.string.products_incomplete), getString(R.string.product_pictures_contributed),
                    getString(R.string.picture_contributed_incomplete), getString(R.string.product_info_added),
                    getString(R.string.product_info_tocomplete)};

            builder.items(contributionTypes);
            builder.itemsCallback(new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {

                    switch (position) {

                        case 0:
                            contributionType = 0;
                            newSearchQuery();
                            break;
                        case 1:
                            contributionType = 1;
                            newSearchQuery();
                            break;
                        case 2:
                            contributionType = 2;
                            newSearchQuery();
                            break;
                        case 3:
                            contributionType = 3;
                            newSearchQuery();
                            break;
                        case 4:
                            contributionType = 4;
                            newSearchQuery();
                            break;
                        case 5:
                            contributionType = 5;
                            newSearchQuery();
                            break;
                        default:
                            contributionType = 0;
                            newSearchQuery();
                            break;

                    }

                }
            });
            builder.show();


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        countProductsView.setVisibility(View.INVISIBLE);

        Bundle extras = getIntent().getExtras();
        searchType = extras.getString(SEARCH_TYPE);
        searchQuery = extras.getString(SEARCH_QUERY);
        mCategoryNameDao = Utils.getAppDaoSession(this).getCategoryNameDao();
        newSearchQuery();


        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Utils.DISABLE_IMAGE_LOAD = preferences.getBoolean("disableImageLoad", false);
        if (Utils.DISABLE_IMAGE_LOAD && Utils.getBatteryLevel(this)) {
            isLowBatteryMode = true;
        }

        SharedPreferences shakePreference = PreferenceManager.getDefaultSharedPreferences(this);
        scanOnShake = shakePreference.getBoolean("shakeScanMode", false);

        // Get the user preference for scan on shake feature and open ContinuousScanActivity if the user has enabled the feature
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();

        if (scanOnShake) {
            mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeDetected() {
                @Override
                public void onShake(int count) {
                    Utils.scan(ProductBrowsingListActivity.this);
                }
            });
        }

    }

    protected void newSearchQuery() {
        getSupportActionBar().setTitle(searchQuery);
        switch (searchType) {
            case SearchType.BRAND:
                toolbar.setSubtitle(R.string.brand_string);
                break;
            case SearchType.COUNTRY:
                toolbar.setSubtitle(R.string.country_string);
                break;
            case SearchType.ADDITIVE:
                toolbar.setSubtitle(R.string.additive_string);
                break;
            case SearchType.SEARCH:
                toolbar.setSubtitle(R.string.search_string);
                break;
            case SearchType.STORE:
                toolbar.setSubtitle(R.string.store_subtitle);
                break;
            case SearchType.PACKAGING:
                toolbar.setSubtitle(R.string.packaging_subtitle);
                break;
            case SearchType.LABEL:
                getSupportActionBar().setSubtitle(getString(R.string.label_string));
                break;
            case SearchType.CATEGORY:
                getSupportActionBar().setSubtitle(getString(R.string.category_string));
                break;
            case SearchType.CONTRIBUTOR:
                getSupportActionBar().setSubtitle(getString(R.string.contributor_string));
                break;
            case SearchType.INCOMPLETE_PRODUCT: {
                getSupportActionBar().setTitle(getString(R.string.products_to_be_completed));
                break;
            }

            case SearchType.STATE: {
                getSupportActionBar().setSubtitle("State");
                break;
            }

            default:

                Log.e("Products Browsing", "No math case found for " + searchType);


        }


        apiClient = new OpenFoodAPIClient(ProductBrowsingListActivity.this, BuildConfig.OFWEBSITE);
        api = new OpenFoodAPIClient(ProductBrowsingListActivity.this);

        progressBar.setVisibility(View.VISIBLE);

        setup();
    }


    @OnClick(R.id.buttonTryAgain)
    public void setup() {
        offlineCloudLayout.setVisibility(View.INVISIBLE);
        countProductsView.setVisibility(View.INVISIBLE);
        pageAddress = 1;
        noResultsLayout.setVisibility(View.INVISIBLE);
        getDataFromAPI();
    }

    /*
    when no matching products are found in the database then noResultsLayout is displayed.
    This method is called when the user clicks on the add photo button in the noResultsLayout.
     */
    @OnClick(R.id.addProduct)
    public void addProduct() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new MaterialDialog.Builder(this)
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                                .CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            Intent intent = new Intent(this, ContinuousScanActivity.class);
            startActivity(intent);
        }
    }

    public void getDataFromAPI() {

        switch (searchType) {
            case SearchType.BRAND:
                apiClient.getProductsByBrand(searchQuery, pageAddress, this::loadData);
                break;
            case SearchType.COUNTRY:
                apiClient.getProductsByCountry(searchQuery, pageAddress, this::loadData);
                break;
            case SearchType.ADDITIVE:
                apiClient.getProductsByAdditive(searchQuery, pageAddress, this::loadData);
                break;
            case SearchType.STORE:
                apiClient.getProductsByStore(searchQuery, pageAddress, new OpenFoodAPIClient.OnStoreCallback() {
                    @Override
                    public void onStoreResponse(boolean value, Search storeObject) {
                        loadData(value, storeObject);
                    }
                });
                break;
            case SearchType.PACKAGING:
                apiClient.getProductsByPackaging(searchQuery, pageAddress, new OpenFoodAPIClient.OnPackagingCallback() {
                    @Override
                    public void onPackagingResponse(boolean value, Search packagingObject) {
                        loadData(value, packagingObject);
                    }
                });
                break;
            case SearchType.SEARCH:
                api.searchProduct(searchQuery, pageAddress, ProductBrowsingListActivity.this, new OpenFoodAPIClient.OnProductsCallback() {
                    @Override
                    public void onProductsResponse(boolean isOk, Search searchResponse, int countProducts) {
                    /*
                    countProducts is checked, if it is -2 it means that there are no matching products in the
                    database for the query.
                     */
                        if (countProducts == -2) {
                            textNoResults.setText(R.string.txt_no_matching_products);
                            textExtendSearch.setText(R.string.txt_broaden_search);
                            noResultsLayout.setVisibility(View.VISIBLE);
                            noResultsLayout.bringToFront();
                            productsRecyclerView.setVisibility(View.INVISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            offlineCloudLayout.setVisibility(View.INVISIBLE);
                            countProductsView.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                        } else {
                            loadData(isOk, searchResponse);
                        }
                    }
                });
                break;
            case SearchType.LABEL:
                api.getProductsByLabel(searchQuery, pageAddress, new OpenFoodAPIClient.onLabelCallback() {
                    @Override
                    public void onLabelResponse(boolean value, Search label) {
                        loadData(value, label);
                    }
                });


                break;
            case SearchType.CATEGORY:
                String appLanguageCode = LocaleHelper.getLanguage(this);
                CategoryName categoryName = mCategoryNameDao.queryBuilder().where(CategoryNameDao.Properties.Name.eq(searchQuery), CategoryNameDao.Properties.LanguageCode.eq(appLanguageCode)).unique();
                String tag = categoryName != null ? categoryName.getCategoryTag() : searchQuery;
                api.getProductsByCategory(tag, pageAddress, (value, category) -> {
                    if (value && category != null && Integer.valueOf(category.getCount()) == 0) {
                        textNoResults.setText(R.string.txt_no_matching__category_products);
                        textExtendSearch.setText(null);
                        noResultsLayout.setVisibility(View.VISIBLE);
                        noResultsLayout.bringToFront();
                        productsRecyclerView.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        offlineCloudLayout.setVisibility(View.INVISIBLE);
                        countProductsView.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        loadData(value, category);
                    }
                });
                break;


            case SearchType.CONTRIBUTOR: {

                switch (contributionType) {
                    case 0:
                        api.getProductsByContributor(searchQuery, pageAddress, this::loadData);
                        break;

                    case 1:
                        api.getToBeCompletedProductsByContributor(searchQuery, pageAddress, this::loadData);
                        break;

                    case 2:
                        api.getPicturesContributedProducts(searchQuery, pageAddress, this::loadData);
                        break;

                    case 3:
                        api.getPicturesContributedIncompleteProducts(searchQuery, pageAddress, this::loadData);
                        break;

                    case 4:
                        api.getInfoAddedProducts(searchQuery, pageAddress, this::loadData);
                        break;

                    case 5:
                        api.getInfoAddedIncompleteProducts(searchQuery, pageAddress, this::loadData);
                        break;

                    default:
                        api.getProductsByContributor(searchQuery, pageAddress, this::loadData);
                        break;
                }
                break;
            }


            case SearchType.STATE:
                api.getProductsByStates(searchQuery, pageAddress, this::loadData);
                break;

            case SearchType.INCOMPLETE_PRODUCT:
                // Get Products to be completed data and input it to loadData function
                api.getIncompleteProducts(pageAddress, new OpenFoodAPIClient.OnIncompleteCallback() {
                    @Override
                    public void onIncompleteResponse(boolean value, Search incompleteProducts) {
                        loadData(value, incompleteProducts);
                    }
                });
                break;


            default:
                Log.e("Products Browsing", "No math case found for " + searchType);


        }
    }


    private void loadData(boolean isResponseOk, Search response) {

        if (isResponseOk) {
            mCountProducts = Integer.parseInt(response.getCount());
            if (pageAddress == 1) {
                countProductsView.setText(getResources().getString(R.string.number_of_results) + " " +
                        NumberFormat.getInstance(getResources().getConfiguration().locale).format(Long.parseLong(response.getCount())));
                mProducts = new ArrayList<>();
                mProducts.addAll(response.getProducts());
                if (mProducts.size() < mCountProducts) {
                    mProducts.add(null);
                }
                if (setupDone) {
                    productsRecyclerView.setAdapter(new ProductsRecyclerViewAdapter(mProducts, isLowBatteryMode));
                }
                setUpRecyclerView();
            } else {
                if (mProducts.size() - 1 < mCountProducts + 1) {
                    final int posStart = mProducts.size();
                    mProducts.remove(mProducts.size() - 1);
                    mProducts.addAll(response.getProducts());
                    if (mProducts.size() < mCountProducts) {
                        mProducts.add(null);
                    }
                    productsRecyclerView.getAdapter().notifyItemRangeChanged(posStart - 1, mProducts.size() - 1);
                }
            }
        } else {
            // productsRecyclerView.setVisibility(View.INVISIBLE);

            swipeRefreshLayout.setRefreshing(false);
            productsRecyclerView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            offlineCloudLayout.setVisibility(View.VISIBLE);
        }


    }

    private void setUpRecyclerView() {

        progressBar.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);
        countProductsView.setVisibility(View.VISIBLE);
        offlineCloudLayout.setVisibility(View.INVISIBLE);
        productsRecyclerView.setVisibility(View.VISIBLE);

        if (!setupDone) {
            productsRecyclerView.setHasFixedSize(true);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(ProductBrowsingListActivity.this, LinearLayoutManager.VERTICAL, false);
            productsRecyclerView.setLayoutManager(mLayoutManager);

            ProductsRecyclerViewAdapter adapter = new ProductsRecyclerViewAdapter(mProducts, isLowBatteryMode);
            productsRecyclerView.setAdapter(adapter);


            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(productsRecyclerView.getContext(),
                    DividerItemDecoration.VERTICAL);
            productsRecyclerView.addItemDecoration(dividerItemDecoration);

            // Retain an instance so that you can call `resetState()` for fresh searches
            scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    if (mProducts.size() < mCountProducts) {
                        pageAddress = page;
                        getDataFromAPI();
                    }
                }
            };
            // Adds the scroll listener to RecyclerView
            productsRecyclerView.addOnScrollListener(scrollListener);


            productsRecyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(ProductBrowsingListActivity.this, (view, position) -> {
                        Product p = ((ProductsRecyclerViewAdapter) productsRecyclerView.getAdapter()).getProduct(position);
                        if (p != null) {
                            String barcode = p.getCode();
                            if (Utils.isNetworkConnected(ProductBrowsingListActivity.this)) {
                                api.getProduct(barcode, ProductBrowsingListActivity.this);
                                try {
                                    View view1 = ProductBrowsingListActivity.this.getCurrentFocus();
                                    if (view != null) {
                                        InputMethodManager imm = (InputMethodManager) ProductBrowsingListActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                                    }
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                new MaterialDialog.Builder(ProductBrowsingListActivity.this)
                                        .title(R.string.device_offline_dialog_title)
                                        .content(R.string.connectivity_check)
                                        .positiveText(R.string.txt_try_again)
                                        .negativeText(R.string.dismiss)
                                        .onPositive((dialog, which) -> {
                                            if (Utils.isNetworkConnected(ProductBrowsingListActivity.this))
                                                api.getProduct(barcode, ProductBrowsingListActivity.this);
                                            else
                                                Toast.makeText(ProductBrowsingListActivity.this, R.string.device_offline_dialog_title, Toast.LENGTH_SHORT).show();
                                        })
                                        .show();
                            }
                        }
                    })
            );

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {

                    mProducts.clear();
                    adapter.notifyDataSetChanged();
                    countProductsView.setText(getResources().getString(R.string.number_of_results));
                    pageAddress = 1;
                    setup();
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }

        setupDone = true;
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            pageAddress = 1;
            setup();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (scanOnShake) {
            //register the listener
            mSensorManager.unregisterListener(mShakeDetector, mAccelerometer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scanOnShake) {
            //unregister the listener
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }
}
