package openfoodfacts.github.scrachx.openfood.features.searchbycode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentFindProductBinding
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils
import openfoodfacts.github.scrachx.openfood.utils.Utils

/**
 * @see R.layout.fragment_find_product
 */
class SearchByCodeFragment : NavigationBaseFragment() {
    private var _binding: FragmentFindProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var api: OpenFoodAPIClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        api = OpenFoodAPIClient(requireActivity())
        _binding = FragmentFindProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editTextBarcode.isSelected = false
        binding.buttonBarcode.setOnClickListener { checkBarcodeThenSearch() }


        val intent = requireActivity().intent
        val barCode = intent.getStringExtra(INTENT_KEY_BARCODE)
        if (!barCode.isNullOrEmpty()) {
            setBarcodeThenSearch(barCode)
        }

    }

    private fun setBarcodeThenSearch(code: String) {
        binding.editTextBarcode.setText(code, TextView.BufferType.EDITABLE)
        checkBarcodeThenSearch()
    }

    private fun checkBarcodeThenSearch() {
        Utils.hideKeyboard(requireActivity())
        val barCodeTxt = binding.editTextBarcode.text.toString()
        if (barCodeTxt.isEmpty()) {
            binding.editTextBarcode.error = resources.getString(R.string.txtBarcodeRequire)
        } else if (!ProductUtils.isBarcodeValid(barCodeTxt)) {
            binding.editTextBarcode.error = resources.getString(R.string.txtBarcodeNotValid)
        } else {
            api.openProduct(barCodeTxt, activity)
        }
    }

    @NavigationDrawerType
    override fun getNavigationDrawerType(): Int {
        return NavigationDrawerListener.ITEM_SEARCH_BY_CODE
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.search_by_barcode_drawer)

    }

    companion object {
        const val INTENT_KEY_BARCODE = "barcode"
    }
}