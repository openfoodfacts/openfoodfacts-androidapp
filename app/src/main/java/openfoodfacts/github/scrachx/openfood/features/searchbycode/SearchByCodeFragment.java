package openfoodfacts.github.scrachx.openfood.features.searchbycode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang.StringUtils;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentFindProductBinding;
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_SEARCH_BY_CODE;

/**
 * @see R.layout#fragment_find_product
 */
public class SearchByCodeFragment extends NavigationBaseFragment {
    public static final String BARCODE = "barcode";
    private FragmentFindProductBinding binding;
    private OpenFoodAPIClient api;
    private Toast mToast;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFindProductBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.editTextBarcode.setSelected(false);
        api = new OpenFoodAPIClient(getActivity());
        binding.buttonBarcode.setOnClickListener(v -> onSearchBarcodeProduct());

        if (requireActivity().getIntent() != null) {
            String barCode = requireActivity().getIntent().getStringExtra(BARCODE);
            if (StringUtils.isNotEmpty(barCode)) {
                searchBarcode(barCode);
            }
        }
    }

    private void onSearchBarcodeProduct() {
        Utils.hideKeyboard(requireActivity());

        final String barCodeTxt = binding.editTextBarcode.getText().toString();
        if (barCodeTxt.isEmpty()) {
            binding.editTextBarcode.setError(getResources().getString(R.string.txtBarcodeRequire));
            return;
        }

        if (barCodeTxt.length() <= 2 && !ApiFields.Defaults.DEBUG_BARCODE.equals(barCodeTxt)) {
            binding.editTextBarcode.setError(getResources().getString(R.string.txtBarcodeNotValid));
            return;
        }

        if (!ProductUtils.isBarcodeValid(barCodeTxt)) {
            binding.editTextBarcode.setError(getResources().getString(R.string.txtBarcodeNotValid));
        } else {

            api.openProduct(barCodeTxt, getActivity());
        }
    }

    private void searchBarcode(String code) {
        binding.editTextBarcode.setText(code, TextView.BufferType.EDITABLE);
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

    @Override
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
