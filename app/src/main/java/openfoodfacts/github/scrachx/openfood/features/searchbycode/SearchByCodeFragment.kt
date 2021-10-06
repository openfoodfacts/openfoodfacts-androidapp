package openfoodfacts.github.scrachx.openfood.features.searchbycode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentFindProductBinding
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivityStarter
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import openfoodfacts.github.scrachx.openfood.utils.hideKeyboard
import openfoodfacts.github.scrachx.openfood.utils.isBarcodeValid
import javax.inject.Inject

/**
 * @see R.layout.fragment_find_product
 */
@AndroidEntryPoint
class SearchByCodeFragment : NavigationBaseFragment() {
    private var _binding: FragmentFindProductBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var client: OpenFoodAPIClient

    @Inject
    lateinit var productViewActivityStarter: ProductViewActivityStarter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

        // Get barcode from intent or saved instance or from arguments, in this order
        var barCode = requireActivity().intent.getStringExtra(INTENT_KEY_BARCODE)
        if (barCode.isNullOrBlank()) {
            barCode = savedInstanceState?.getString(INTENT_KEY_BARCODE) ?: arguments?.getString(INTENT_KEY_BARCODE)
        }
        if (!barCode.isNullOrBlank()) {
            setBarcodeThenSearch(barCode)
        }

    }

    private fun setBarcodeThenSearch(code: String) {
        binding.editTextBarcode.setText(code, TextView.BufferType.EDITABLE)
        checkBarcodeThenSearch()
    }

    private fun checkBarcodeThenSearch() {
        requireActivity().hideKeyboard()
        val barCodeTxt = binding.editTextBarcode.text.toString()
        if (barCodeTxt.isEmpty()) {
            binding.editTextBarcode.error = resources.getString(R.string.txtBarcodeRequire)
        } else if (!isBarcodeValid(barCodeTxt)) {
            binding.editTextBarcode.error = resources.getString(R.string.txtBarcodeNotValid)
        } else {
            lifecycleScope.launch { productViewActivityStarter.openProduct(barCodeTxt, requireActivity()) }
        }
    }

    @NavigationDrawerType
    override fun getNavigationDrawerType() = NavigationDrawerListener.ITEM_SEARCH_BY_CODE

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.search_by_barcode_drawer)
    }

    companion object {
        const val INTENT_KEY_BARCODE = "barcode"
        fun newInstance(barcode: String? = null) = SearchByCodeFragment().apply {
            arguments = Bundle().apply {
                putString(INTENT_KEY_BARCODE, barcode)
            }
        }
    }
}
