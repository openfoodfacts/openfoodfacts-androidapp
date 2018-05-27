package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsRecyclerViewAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.EndlessRecyclerViewScrollListener;
import openfoodfacts.github.scrachx.openfood.views.listeners.RecyclerItemClickListener;

public class SearchProductsResultsFragment extends BaseFragment {

    private OpenFoodAPIClient api;
    private RecyclerView productsRecyclerView;
    private View progressBar;
    private EndlessRecyclerViewScrollListener scrollListener;
    private List<Product> mProducts;
    private int mCountProducts = 0;

    @BindView(R.id.textCountProduct)
    TextView countProductsView;

    @BindView(R.id.noResultsLayout)
    LinearLayout noResultsLayout;

    @BindView(R.id.offlineCloudLinearLayout)
    LinearLayout offlineCloudLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        api = new OpenFoodAPIClient(getActivity());

        return createView(inflater, container, R.layout.fragment_search_products_results);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);

        productsRecyclerView = (RecyclerView) view.findViewById(R.id.products_recycler_view);

        mProducts = new ArrayList<>();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        productsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        productsRecyclerView.setLayoutManager(mLayoutManager);

        // use VERTICAL divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(productsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        productsRecyclerView.addItemDecoration(dividerItemDecoration);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if(mProducts.size() < mCountProducts) {
                    loadNextDataFromApi(page);
                }
            }
        };
        // Adds the scroll listener to RecyclerView
        productsRecyclerView.addOnScrollListener(scrollListener);

        // Click listener on a product
        productsRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(view.getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Product p = ((ProductsRecyclerViewAdapter) productsRecyclerView.getAdapter()).getProduct(position);
                        if(p != null) {
                            String barcode = p.getCode();
                            api.getProduct(barcode, getActivity());
                        }
                    }
                })
        );

        searchProduct(view);

        progressBar = view.findViewById(R.id.progressBar);
        showProgressBar();
    }

    @OnClick(R.id.buttonTryAgain)
    public void searchProduct(View view){
        api.searchProduct(getArguments().getString("query"), 1, getActivity(),
                new OpenFoodAPIClient.OnProductsCallback() {

                    @Override
                    public void onProductsResponse(boolean isResponseOk, List<Product> products, int countProducts) {
                        hideProgressBar();
                        if (isResponseOk) {
                            countProductsView.append(" " + String.valueOf(countProducts));
                            mCountProducts = countProducts;
                            mProducts.addAll(products);
                            if(mProducts.size() < mCountProducts) {
                                mProducts.add(null);
                            }
                            ProductsRecyclerViewAdapter adapter = new ProductsRecyclerViewAdapter(mProducts);
                            productsRecyclerView.setAdapter(adapter);
                            countProductsView.setVisibility(View.VISIBLE);
                            offlineCloudLayout.setVisibility(View.INVISIBLE);
                            noResultsLayout.setVisibility(View.INVISIBLE);
                        } else {
                            if(countProducts == -2) {
                                countProductsView.setVisibility(View.INVISIBLE);
                                offlineCloudLayout.setVisibility(View.INVISIBLE);
                                noResultsLayout.setVisibility(View.VISIBLE);
                            }
                            else{
                                countProductsView.setVisibility(View.INVISIBLE);
                                noResultsLayout.setVisibility(View.INVISIBLE);
                                offlineCloudLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
        );
    }

    private void showProgressBar() {
        countProductsView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate().setDuration(200).alpha(1).start();
    }

    private void hideProgressBar() {
        countProductsView.setVisibility(View.VISIBLE);
        progressBar.animate().setDuration(200).alpha(0).start();
        progressBar.setVisibility(View.GONE);
    }

    // Append the next page of data into the adapter
    public void loadNextDataFromApi(int offset) {
        api.searchProduct(getArguments().getString("query"), offset, getActivity(),
                new OpenFoodAPIClient.OnProductsCallback() {

                    @Override
                    public void onProductsResponse(boolean isResponseOk, List<Product> products, int countProducts) {
                        final int posStart = mProducts.size();

                        if (isResponseOk && mProducts.size() - 1 < mCountProducts + 1) {
                            mProducts.remove(mProducts.size() - 1);
                            mProducts.addAll(products);
                            if(mProducts.size() < mCountProducts) {
                                mProducts.add(null);
                            }
                            productsRecyclerView.getAdapter().notifyItemRangeChanged(posStart - 1, mProducts.size() - 1);
                        }

                    }
                }
        );

    }
}