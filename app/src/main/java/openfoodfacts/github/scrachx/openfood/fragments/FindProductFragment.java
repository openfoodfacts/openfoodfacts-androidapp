package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import org.apache.commons.lang.StringUtils;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_SEARCH_BY_CODE;

public class FindProductFragment extends NavigationBaseFragment {
    public static final String BARCODE = "barcode";
    @BindView(R.id.editTextBarcode)
    EditText mBarCodeText;
    @BindView(R.id.buttonBarcode)
    Button mLaunchButton;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
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
        if (getActivity().getIntent() != null) {
            String barCode = getActivity().getIntent().getStringExtra(BARCODE);
            if (StringUtils.isNotEmpty(barCode)) {
                searchBarcode(barCode);
            }
        }
        BottomNavigationListenerInstaller.install(bottomNavigationView, getActivity(), getContext());
    }

    @OnClick(R.id.buttonBarcode)
    protected void onSearchBarcodeProduct() {
        Utils.hideKeyboard(getActivity());
        if (mBarCodeText.getText().toString().isEmpty()) {
            displayToast(getResources().getString(R.string.txtBarcodeRequire));
        } else {
            String barcodeText = mBarCodeText.getText().toString();
            if (barcodeText.length() <= 2 && !ProductUtils.DEBUG_BARCODE.equals(barcodeText)) {
                displayToast(getResources().getString(R.string.txtBarcodeNotValid));
            } else {
                if (ProductUtils.isBarcodeValid(barcodeText)) {
                    api.getProduct(mBarCodeText.getText().toString(), getActivity());
                } else {
                    displayToast(getResources().getString(R.string.txtBarcodeNotValid));
                }
            }
        }
    }

    private void searchBarcode(String code) {
        mBarCodeText.setText(code, TextView.BufferType.EDITABLE);
        onSearchBarcodeProduct();
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_SEARCH_BY_CODE;
    }

    public void displayToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void onResume() {
        super.onResume();
        final ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.search_by_barcode_drawer));
        }
    }

    @Override
    public void onPause() {
        if (mToast != null) {
            mToast.cancel();
        }

        super.onPause();
    }
}
