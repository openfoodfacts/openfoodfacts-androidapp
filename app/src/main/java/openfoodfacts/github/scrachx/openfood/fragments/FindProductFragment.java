package openfoodfacts.github.scrachx.openfood.fragments;


import android.app.Fragment;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

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
                Utils.hideKeyboard(getActivity());
                if(barCode_text.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), getResources().getString(R.string.txtBarcodeRequire), Toast.LENGTH_LONG).show();
                }else{
                    if(EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(barCode_text.getText().toString()) && (!barCode_text.getText().toString().substring(0,3).contains("977") || !barCode_text.getText().toString().substring(0,3).contains("978") || !barCode_text.getText().toString().substring(0,3).contains("979"))) {
                        goToProduct();
                    }else{
                        Toast.makeText(getActivity(), getResources().getString(R.string.txtBarcodeNotValid), Toast.LENGTH_LONG).show();
                    }
                }
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
