package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsListAdapter;

public class SearchProductsFragment extends Fragment {

    private ArrayList<Product> productItems;
    private ProductsListAdapter adapter;
    private ListView listView;
    private Button buttonSearch;
    private EditText nameSearch;
    private FoodAPIRestClientUsage api;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_products,container,false);
        listView = (ListView) rootView.findViewById(R.id.listProducts);
        buttonSearch = (Button) rootView.findViewById(R.id.buttonSearchProducts);
        nameSearch = (EditText) rootView.findViewById(R.id.editTextName);
        productItems = new ArrayList<Product>();
        api = new FoodAPIRestClientUsage();

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideKeyboard(getActivity());
                if (!nameSearch.getText().toString().isEmpty()) {
                    buttonSearch.setEnabled(false);
                    api.searchProduct(nameSearch.getText().toString(), getActivity(),
                        new FoodAPIRestClientUsage.OnProductsCallback() {
                            @Override
                            public void onProductsResponse(boolean value, List<Product> lproducts) {
                                if(value) {
                                    productItems = (ArrayList) lproducts;
                                    adapter = new ProductsListAdapter(getActivity(),productItems);
                                    listView.setAdapter(adapter);
                                }
                                buttonSearch.setEnabled(true);
                            }
                        }
                    );
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product p = (Product) parent.getItemAtPosition(position);
                String barcode = p.getCode();
                api.getProduct(barcode, getActivity());
            }
        });

        return rootView;
    }

}