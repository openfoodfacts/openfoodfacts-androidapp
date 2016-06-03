package openbeautyfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemClick;
import openbeautyfacts.github.scrachx.openfood.R;
import openbeautyfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openbeautyfacts.github.scrachx.openfood.models.Product;
import openbeautyfacts.github.scrachx.openfood.utils.Utils;
import openbeautyfacts.github.scrachx.openfood.views.adapters.ProductsListAdapter;

public class SearchProductsFragment extends BaseFragment {

    private ArrayList<Product> productItems;
    private ProductsListAdapter adapter;
    private FoodAPIRestClientUsage api;

    @Bind(R.id.listProducts) ListView listView;
    @Bind(R.id.buttonSearchProducts) Button buttonSearch;
    @Bind(R.id.editTextName) EditText nameSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_search_products);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productItems = new ArrayList<>();
        api = new FoodAPIRestClientUsage();
    }

    @OnItemClick(R.id.listProducts)
    protected void onProductClicked(int position) {
        Product p = (Product) listView.getItemAtPosition(position);
        String barcode = p.getCode();
        api.getProduct(barcode, getActivity());
    }

    @OnClick(R.id.buttonSearchProducts)
    protected void onSearchProduct() {
        Utils.hideKeyboard(getActivity());
        if (!nameSearch.getText().toString().isEmpty()) {
            buttonSearch.setEnabled(false);
            api.searchProduct(nameSearch.getText().toString(), getActivity(),
                    new FoodAPIRestClientUsage.OnProductsCallback() {
                        @Override
                        public void onProductsResponse(boolean value, List<Product> lproducts) {
                            if (value) {
                                productItems = (ArrayList) lproducts;
                                adapter = new ProductsListAdapter(getActivity(), productItems);
                                listView.setAdapter(adapter);
                            }
                            buttonSearch.setEnabled(true);
                        }
                    }
            );
        }
    }
}