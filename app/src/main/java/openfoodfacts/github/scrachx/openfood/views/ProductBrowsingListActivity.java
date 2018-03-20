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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsRecyclerViewAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.EndlessRecyclerViewScrollListener;
import openfoodfacts.github.scrachx.openfood.views.listeners.RecyclerItemClickListener;

public class ProductBrowsingListActivity extends BaseActivity {

    private static String SEARCH_TYPE = "search_type";
    private static String SEARCH_TITLE = "search_title";

    private String searchType;

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
    private EndlessRecyclerViewScrollListener scrollListener;
    private List<Product> mProducts;
    private OpenFoodAPIClient api;
    private OpenFoodAPIClient apiClient;
    private int mCountProducts = 0;
    private int pageAddress = 1;
    String title;

    public static void startActivity(Context context, String title, @SearchType String type) {
        Intent intent = new Intent(context, ProductBrowsingListActivity.class);
        intent.putExtra(SEARCH_TITLE, title);
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
                
                key = query;
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
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        countProductsView.setVisibility(View.INVISIBLE);
        
        Bundle extras = getIntent().getExtras();
        searchType = extras.getString(SEARCH_TYPE);
        title = extras.getString(SEARCH_TITLE);
        newSearchQuery();
    }

    protected void newSearchQuery(){
        getSupportActionBar().setTitle(title);
      
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
        }

        apiClient = new OpenFoodAPIClient(ProductBrowsingListActivity.this, BuildConfig.OFWEBSITE);
        api = new OpenFoodAPIClient(ProductBrowsingListActivity.this);
        productsRecyclerView = (RecyclerView) findViewById(R.id.products_recycler_view);
        progressBar.setVisibility(View.VISIBLE);
        setup();
    }

    @OnClick(R.id.buttonTryAgain)
    public void setup() {
        offlineCloudLayout.setVisibility(View.INVISIBLE);
        countProductsView.setVisibility(View.INVISIBLE);
        getDataFromAPI();
    }

    public void getDataFromAPI() {


        switch (searchType) {
            case SearchType.BRAND: {
                apiClient.getProductsByBrand(title, pageAddress, new OpenFoodAPIClient.OnBrandCallback() {
                    @Override
                    public void onBrandResponse(boolean value, Search brandObject) {
                        loadData(value, brandObject);
                    }
                });
                break;
            }
            case SearchType.COUNTRY: {
                apiClient.getProductsByCountry(title, pageAddress, new OpenFoodAPIClient.onCountryCallback() {
                    @Override
                    public void onCountryResponse(boolean value, Search country) {
                        loadData(value, country);
                    }
                });
                break;
            }
            case SearchType.ADDITIVE: {
                apiClient.getProductsByAdditive(title, pageAddress, new OpenFoodAPIClient.OnAdditiveCallback() {
                    @Override
                    public void onAdditiveResponse(boolean value, Search country) {
                        loadData(value, country);
                    }
                });
                break;
            }

            case SearchType.STORE: {
                apiClient.getProductsByStore(title, pageAddress, new OpenFoodAPIClient.OnStoreCallback() {
                    @Override
                    public void onStoreResponse(boolean value, Search storeObject) {
                        loadData(value, storeObject);
                    }
                });
                break;
            }

            case SearchType.PACKAGING: {
                apiClient.getProductsByPackaging(title, pageAddress, new OpenFoodAPIClient.OnPackagingCallback() {
                    @Override
                    public void onPackagingResponse(boolean value, Search packagingObject) {
                        loadData(value, packagingObject);
                    }
                });
                break;
            }
            case SearchType.SEARCH: {
                api.searchProduct(title, pageAddress, ProductBrowsingListActivity.this, new OpenFoodAPIClient.OnProductsCallback() {
                    @Override
                    public void onProductsResponse(boolean isOk, Search searchResponse, int countProducts) {
                        loadData(isOk, searchResponse);
                    }
                });
                break;
            }

            case SearchType.LABEL: {
                api.getProductsByLabel(title, pageAddress, new OpenFoodAPIClient.onLabelCallback() {
                    @Override
                    public void onLabelResponse(boolean value, Search label) {
                        loadData(value, label);
                    }
                });
                break;
            }

            case SearchType.CATEGORY: {
                api.getProductsByCategory(title, pageAddress, new OpenFoodAPIClient.onCategoryCallback() {
                    @Override
                    public void onCategoryResponse(boolean value, Search category) {
                        loadData(value, category);
                    }
                });
                break;
            }

            case SearchType.CONTRIBUTOR: {
                api.getProductsByContributor(title, pageAddress, new OpenFoodAPIClient.onContributorCallback() {
                    @Override
                    public void onContributorResponse(boolean value, Search contributor) {
                        loadData(value, contributor);
                    }
                });
                break;
            }
        }
    }


    private void loadData(boolean isResponseOk, Search response) {

        if (isResponseOk) {
            mCountProducts = Integer.parseInt(response.getCount());
            if (pageAddress == 1) {
                countProductsView.append(" " + NumberFormat.getInstance(getResources().getConfiguration().locale).format(Long.parseLong(response.getCount()
                )));
                mProducts = new ArrayList<>();
                mProducts.addAll(response.getProducts());
                if (mProducts.size() < mCountProducts) {
                    mProducts.add(null);
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
                if (mProducts.size() < mCountProducts && !swipeRefreshLayout.isRefreshing()) {
                    pageAddress = page;
                    getDataFromAPI();
                }
            }
        };
        // Adds the scroll listener to RecyclerView
        productsRecyclerView.addOnScrollListener(scrollListener);


        productsRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(ProductBrowsingListActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Product p = ((ProductsRecyclerViewAdapter) productsRecyclerView.getAdapter()).getProduct(position);
                        if (p != null) {
                            String barcode = p.getCode();
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
                        }
                    }
                })
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                countProductsView.setText(getResources().getString(R.string.number_of_results));
                pageAddress = 1;
                setup();
            }
        });

    }
}
