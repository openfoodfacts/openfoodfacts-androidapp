package openfoodfacts.github.scrachx.openfood.fragments;


import android.app.Fragment;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public class FindProductFragment extends Fragment {

    EditText mBarCodeText;
    Button mLaunchButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_product,container,false);
        mBarCodeText = (EditText) rootView.findViewById(R.id.editTextBarcode);
        mLaunchButton = (Button) rootView.findViewById(R.id.buttonBarcode);
        mLaunchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.hideKeyboard(getActivity());
                if(mBarCodeText.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), getResources().getString(R.string.txtBarcodeRequire), Toast.LENGTH_LONG).show();
                }else{
                    if(EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(mBarCodeText.getText().toString()) && (!mBarCodeText.getText().toString().substring(0,3).contains("977") || !mBarCodeText.getText().toString().substring(0,3).contains("978") || !mBarCodeText.getText().toString().substring(0,3).contains("979"))) {
                        goToProduct();
                    }else{
                        Toast.makeText(getActivity(), getResources().getString(R.string.txtBarcodeNotValid), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        mBarCodeText.setSelected(false);
        return rootView;
    }

    private void goToProduct(){
        FoodAPIRestClientUsage api = new FoodAPIRestClientUsage();
        api.getProduct(mBarCodeText.getText().toString(), getActivity());
    }

}
