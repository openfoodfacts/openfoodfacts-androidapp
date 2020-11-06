package openfoodfacts.github.scrachx.openfood.features.searchbycode;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang.StringUtils;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentFindProductBinding;
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_SEARCH_BY_CODE;

/**
 * @see R.layout#fragment_find_product
 */
public class SearchByCodeFragment extends NavigationBaseFragment {
    public static final String INTENT_KEY_BARCODE = "barcode";
    private FragmentFindProductBinding binding;
    private OpenFoodAPIClient api;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        api = new OpenFoodAPIClient(requireActivity());
        binding = FragmentFindProductBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.editTextBarcode.setSelected(false);
        binding.buttonBarcode.setOnClickListener(v -> checkBarcodeThenSearch());

        Intent intent = requireActivity().getIntent();
        if (intent != null) {
            String barCode = intent.getStringExtra(INTENT_KEY_BARCODE);
            if (StringUtils.isNotEmpty(barCode)) {
                setBarcodeThenSearch(barCode);
            }
        }
    }

    private void setBarcodeThenSearch(String code) {
        binding.editTextBarcode.setText(code, TextView.BufferType.EDITABLE);
        checkBarcodeThenSearch();
    }

    private void checkBarcodeThenSearch() {
        Utils.hideKeyboard(requireActivity());

        final String barCodeTxt = binding.editTextBarcode.getText().toString();
        if (barCodeTxt.isEmpty()) {
            binding.editTextBarcode.setError(getResources().getString(R.string.txtBarcodeRequire));
        } else if (!ProductUtils.isBarcodeValid(barCodeTxt)) {
            binding.editTextBarcode.setError(getResources().getString(R.string.txtBarcodeNotValid));
        } else {
            api.openProduct(barCodeTxt, getActivity());
        }
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_SEARCH_BY_CODE;
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar supportActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.search_by_barcode_drawer));
        }
    }
}
