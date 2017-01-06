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

public class SearchProductsResultsFragment extends BaseFragment {

    private OpenFoodAPIClient api;

    private RecyclerView productsRecyclerView;

    private RecyclerView.LayoutManager mLayoutManager;

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
        mLayoutManager = new LinearLayoutManager(getActivity());
        productsRecyclerView.setLayoutManager(mLayoutManager);

        // use VERTICAL divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(productsRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        productsRecyclerView.addItemDecoration(dividerItemDecoration);

        String query = this.getArguments().getString("query");

        api.searchProduct(query, getActivity(),
                new OpenFoodAPIClient.OnProductsCallback() {

                    @Override
                    public void onProductsResponse(boolean isResponseOk, List<Product> products) {
                        if (isResponseOk) {
                            RecyclerView.Adapter adapter = new ProductsRecyclerViewAdapter(products);
                            productsRecyclerView.setAdapter(adapter);
                        }
                    }
                }
        );
    }

//    @OnItemClick(R.id.listProducts)
    protected void onProductClicked(int position) {
        Product p = ((ProductsRecyclerViewAdapter) productsRecyclerView.getAdapter()).getProduct(position);
        String barcode = p.getCode();
        api.getProduct(barcode, getActivity());
    }


}