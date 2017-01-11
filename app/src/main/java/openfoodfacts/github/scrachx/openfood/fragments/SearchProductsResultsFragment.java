package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsRecyclerViewAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.RecyclerItemClickListener;

public class SearchProductsResultsFragment extends BaseFragment {

    private OpenFoodAPIClient api;

    private RecyclerView productsRecyclerView;

    private View progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        api = new OpenFoodAPIClient(getActivity());

        return createView(inflater, container, R.layout.fragment_search_products_results);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);

        productsRecyclerView = (RecyclerView) view.findViewById(R.id.products_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        productsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        productsRecyclerView.setLayoutManager(mLayoutManager);

        // use VERTICAL divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(productsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        productsRecyclerView.addItemDecoration(dividerItemDecoration);

        // Click listener on a product
        productsRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(view.getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Product p = ((ProductsRecyclerViewAdapter) productsRecyclerView.getAdapter()).getProduct(position);
                        String barcode = p.getCode();
                        api.getProduct(barcode, getActivity());
                    }
                })
        );

        api.searchProduct(getArguments().getString("query"), getActivity(),
                new OpenFoodAPIClient.OnProductsCallback() {

                    @Override
                    public void onProductsResponse(boolean isResponseOk, List<Product> products) {
                        hideProgressBar();
                        if (isResponseOk) {
                            RecyclerView.Adapter adapter = new ProductsRecyclerViewAdapter(products);
                            productsRecyclerView.setAdapter(adapter);
                        }
                    }
                }
        );

        progressBar = view.findViewById(R.id.progressBar);
        showProgressBar();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate().setDuration(200).alpha(1).start();
    }

    private void hideProgressBar() {
        progressBar.animate().setDuration(200).alpha(0).start();
        progressBar.setVisibility(View.GONE);
    }
}