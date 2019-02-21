package openfoodfacts.github.scrachx.openfood.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_SEARCH_BY_CODE;

public class FindProductFragment extends NavigationBaseFragment {

    @BindView(R.id.editTextBarcode) EditText mBarCodeText;
    @BindView(R.id.buttonBarcode) Button mLaunchButton;
    private OpenFoodAPIClient api;
    private Toast mToast;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_find_product);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBarCodeText.setSelected(false);
        api = new OpenFoodAPIClient(getActivity());
    }

    @OnClick(R.id.buttonBarcode)
    protected void onSearchBarcodeProduct() {
        Utils.hideKeyboard(getActivity());
        if (mBarCodeText.getText().toString().isEmpty()) {
            displayToast(getResources().getString(R.string.txtBarcodeRequire));
        } else {
            String barcodeText = mBarCodeText.getText().toString();
            if(barcodeText.length()<=2){
                displayToast(getResources().getString(R.string.txtBarcodeNotValid));
            }
            else {
                if (EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(barcodeText) && (!barcodeText.substring(0, 3).contains("977") ||!barcodeText.substring(0, 3).contains("978") || !barcodeText.substring(0, 3).contains("979"))) {
                    api.getProduct(mBarCodeText.getText().toString(), getActivity());
                } else {
                    displayToast(getResources().getString(R.string.txtBarcodeNotValid));
                }
            }
        }
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_SEARCH_BY_CODE;
    }

    public void displayToast(String message) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        mToast.show();
    }
    public void onResume() {

        super.onResume();

        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.search_by_barcode_drawer));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        if (mToast != null)
            mToast.cancel();

        super.onPause();
    }
}
