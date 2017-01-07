package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsListAdapter;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class SearchProductsFragment extends BaseFragment {

    private ProductsListAdapter adapter;
    private OpenFoodAPIClient api;

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

        api = new OpenFoodAPIClient(getActivity());
    }

    @OnItemClick(R.id.listProducts)
    protected void onProductClicked(int position) {
        Product p = (Product) listView.getItemAtPosition(position);
        String barcode = p.getCode();
        api.getProduct(barcode, getActivity(), null, null);
    }

    @OnClick(R.id.buttonSearchProducts)
    protected void onSearchProduct() {
        Utils.hideKeyboard(getActivity());
        String searchTerms = nameSearch.getText().toString();
        if (isNotEmpty(searchTerms)) {
            buttonSearch.setEnabled(false);
            api.searchProduct(searchTerms, getActivity(),
                    new OpenFoodAPIClient.OnProductsCallback() {
                        @Override
                        public void onProductsResponse(boolean isOk, List<Product> products) {
                            if (isOk) {
                                adapter = new ProductsListAdapter(getActivity(), products);
                                listView.setAdapter(adapter);
                            }
                            buttonSearch.setEnabled(true);
                        }
                    }
            );
        }
    }
}