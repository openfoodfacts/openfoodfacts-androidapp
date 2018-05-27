package openfoodfacts.github.scrachx.openfood.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public class FindProductFragment extends BaseFragment {

    @BindView(R.id.editTextBarcode) EditText mBarCodeText;
    @BindView(R.id.buttonBarcode) Button mLaunchButton;
    private OpenFoodAPIClient api;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_find_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBarCodeText.setSelected(false);
        api = new OpenFoodAPIClient(getActivity());
    }

    @OnClick(R.id.buttonBarcode)
    protected void onSearchBarcodeProduct() {
        Utils.hideKeyboard(getActivity());
        if (mBarCodeText.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.txtBarcodeRequire), Toast.LENGTH_LONG).show();
        } else {
            if (EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(mBarCodeText.getText().toString()) && (!mBarCodeText.getText().toString().substring(0, 3).contains("977") || !mBarCodeText.getText().toString().substring(0, 3).contains("978") || !mBarCodeText.getText().toString().substring(0, 3).contains("979"))) {
                api.getProduct(mBarCodeText.getText().toString(), getActivity());
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.txtBarcodeNotValid), Toast.LENGTH_LONG).show();
            }
        }
    }
}
