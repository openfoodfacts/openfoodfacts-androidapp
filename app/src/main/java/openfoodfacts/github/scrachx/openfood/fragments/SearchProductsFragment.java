package openfoodfacts.github.scrachx.openfood.fragments;

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

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsListAdapter;

public class SearchProductsFragment extends BaseFragment {

    private List<Product> productItems;
    private ProductsListAdapter adapter;
    private FoodAPIRestClientUsage api;

    @BindView(R.id.listProducts) ListView listView;
    @BindView(R.id.buttonSearchProducts) Button buttonSearch;
    @BindView(R.id.editTextName) EditText nameSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_search_products);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        productItems = new ArrayList<>();
        api = new FoodAPIRestClientUsage(getString(R.string.openfoodUrl));
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
                        public void onProductsResponse(boolean value, List<Product> products) {
                            if (value) {
                                productItems = products;
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