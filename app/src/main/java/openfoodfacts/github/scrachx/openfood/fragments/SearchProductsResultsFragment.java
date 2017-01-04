package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsListAdapter;

public class SearchProductsResultsFragment extends BaseFragment {

    private OpenFoodAPIClient api;

    @BindView(R.id.listProducts) ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        api = new OpenFoodAPIClient(getActivity());

        return createView(inflater, container, R.layout.fragment_search_products_results);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);

        String query = this.getArguments().getString("query");

        api.searchProduct(query, getActivity(),
                new OpenFoodAPIClient.OnProductsCallback() {
                    @Override
                    public void onProductsResponse(boolean isResponseOk, List<Product> products) {
                        if (isResponseOk) {
                            ProductsListAdapter adapter = new ProductsListAdapter(getActivity(), products);
                            listView.setAdapter(adapter);
                        }
                    }
                }
        );
    }

    @OnItemClick(R.id.listProducts)
    protected void onProductClicked(int position) {
        Product p = (Product) listView.getItemAtPosition(position);
        String barcode = p.getCode();
        api.getProduct(barcode, getActivity());
    }


}