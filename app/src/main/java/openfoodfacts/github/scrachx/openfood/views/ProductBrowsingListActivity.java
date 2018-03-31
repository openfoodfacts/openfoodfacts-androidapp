package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
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
    ProgressBar progressBar;
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

        return true;
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
        setContentView(R.layout.activity_brand);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar = findViewById(R.id.progress_bar);
        countProductsView.setVisibility(View.INVISIBLE);

        Bundle extras = getIntent().getExtras();
        searchType = extras.getString(SEARCH_TYPE);
        searchQuery = extras.getString(SEARCH_QUERY);
        newSearchQuery();
    }

    protected void newSearchQuery() {
        getSupportActionBar().setTitle(searchQuery);
        switch (searchType) {
            case SearchType.BRAND: {
                toolbar.setSubtitle(R.string.brand_string);
                break;
            }
            case SearchType.COUNTRY: {
                toolbar.setSubtitle(R.string.country_string);
                break;
            }
            case SearchType.ADDITIVE: {
                toolbar.setSubtitle(R.string.additive_string);
                break;
            }
            case SearchType.SEARCH: {
                toolbar.setSubtitle(R.string.search_string);
                break;
            }
            case SearchType.STORE: {
                toolbar.setSubtitle(R.string.store_subtitle);
                break;
            }
            case SearchType.PACKAGING: {
                toolbar.setSubtitle(R.string.packaging_subtitle);
                break;
            }
            case SearchType.LABEL: {
                getSupportActionBar().setSubtitle(getString(R.string.label_string));
                break;
            }
            case SearchType.CATEGORY: {
                getSupportActionBar().setSubtitle(getString(R.string.category_string));
                break;
            }
            case SearchType.CONTRIBUTOR: {
                getSupportActionBar().setSubtitle(getString(R.string.contributor_string));
                break;
            }
            default : {
                Log.e("Products Browsing","No math case found for "+searchType);
            }

        }

        apiClient = new OpenFoodAPIClient(ProductBrowsingListActivity.this, BuildConfig.OFWEBSITE);
        api = new OpenFoodAPIClient(ProductBrowsingListActivity.this);

        productsRecyclerView = findViewById(R.id.products_recycler_view);
        progressBar.setVisibility(View.VISIBLE);

        setup();
    }

    @OnClick(R.id.buttonTryAgain)
    public void setup() {
        offlineCloudLayout.setVisibility(View.INVISIBLE);
        countProductsView.setVisibility(View.INVISIBLE);
        pageAddress = 1;
        getDataFromAPI();
    }

    public void getDataFromAPI() {


        switch (searchType) {
            case SearchType.BRAND: {

                apiClient.getProductsByBrand(searchQuery, pageAddress, this::loadData);
                break;
            }
            case SearchType.COUNTRY: {
                apiClient.getProductsByCountry(searchQuery, pageAddress, this::loadData);
                break;
            }
            case SearchType.ADDITIVE: {
                apiClient.getProductsByAdditive(searchQuery, pageAddress, this::loadData);

                break;
            }

            case SearchType.STORE: {
                apiClient.getProductsByStore(searchQuery, pageAddress, new OpenFoodAPIClient.OnStoreCallback() {

                    @Override
                    public void onStoreResponse(boolean value, Search storeObject) {
                        loadData(value, storeObject);
                    }
                });
                break;
            }

            case SearchType.PACKAGING: {

                apiClient.getProductsByPackaging(searchQuery, pageAddress, new OpenFoodAPIClient.OnPackagingCallback() {

                    @Override
                    public void onPackagingResponse(boolean value, Search packagingObject) {
                        loadData(value, packagingObject);
                    }
                });
                break;
            }
            case SearchType.SEARCH:

            {
                api.searchProduct(searchQuery, pageAddress, ProductBrowsingListActivity.this, new OpenFoodAPIClient.OnProductsCallback() {

                    @Override

                    public void onProductsResponse(boolean isOk, Search searchResponse, int countProducts) {
                        loadData(isOk, searchResponse);
                    }
                });
                break;
            }

            case SearchType.LABEL:

            {
                api.getProductsByLabel(searchQuery, pageAddress, new OpenFoodAPIClient.onLabelCallback() {

                    @Override

                    public void onLabelResponse(boolean value, Search label) {
                        loadData(value, label);
                    }
                });

                break;
            }
            case SearchType.CATEGORY: {

                api.getProductsByCategory(searchQuery, pageAddress, this::loadData);
                break;
            }

            case SearchType.CONTRIBUTOR: {


                api.getProductsByContributor(searchQuery, pageAddress, this::loadData);
                break;
            }
            default : {
                Log.e("Products Browsing","No math case found for "+searchType);
            }
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
                    productsRecyclerView.setAdapter(new ProductsRecyclerViewAdapter(mProducts));
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

            ProductsRecyclerViewAdapter adapter = new ProductsRecyclerViewAdapter(mProducts);
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
}
