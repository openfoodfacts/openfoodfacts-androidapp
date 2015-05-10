package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;

/**
 * Created by scotscriven on 03/05/15.
 */
public class FindProductFragment extends Fragment {

    EditText barCode_text;
    Button launch_button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_product,container,false);
        barCode_text = (EditText) rootView.findViewById(R.id.editTextBarcode);
        launch_button = (Button) rootView.findViewById(R.id.buttonBarcode);
        launch_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToProduct();
            }
        });
        barCode_text.setSelected(false);
        return rootView;
    }

    private void goToProduct(){
        FoodAPIRestClientUsage api = new FoodAPIRestClientUsage();
        api.getProduct(barCode_text.getText().toString(), getActivity());
    }

}
