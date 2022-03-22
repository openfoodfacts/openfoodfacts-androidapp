package openfoodfacts.github.scrachx.openfood.features.searchbycode

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentFindProductBinding
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import openfoodfacts.github.scrachx.openfood.utils.hideKeyboard
import openfoodfacts.github.scrachx.openfood.utils.isBarcodeValid
import openfoodfacts.github.scrachx.openfood.utils.isEmpty
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit

/**
 * @see R.layout.fragment_find_product
 */
@AndroidEntryPoint
class SearchByCodeFragment : NavigationBaseFragment() {
    private var _binding: FragmentFindProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchByCodeViewModel by viewModels()

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
        binding.editTextBarcode.let { e ->
            e.isSelected = false

            // Accept both the Enter key (from the keyboard) or the search virtual key
            e.setOnEditorActionListener { _, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    keyEvent.keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    checkBarcodeThenSearch()
                    true
                } else {
                    false
                }
            }
        }


        binding.buttonBarcode.setOnClickListener { checkBarcodeThenSearch() }

        // Get barcode from intent or saved instance or from arguments, in this order
        val barcode = requireActivity().intent.getStringExtra(INTENT_KEY_BARCODE).takeUnless { it.isNullOrBlank() }
            ?: savedInstanceState?.getString(INTENT_KEY_BARCODE).takeUnless { it.isNullOrBlank() }
            ?: arguments?.getString(INTENT_KEY_BARCODE).takeUnless { it.isNullOrBlank() }

        // If we have the barcode, set the textbox and start the search
        if (!barcode.isNullOrBlank()) {
            binding.editTextBarcode.setText(barcode, TextView.BufferType.EDITABLE)
            checkBarcodeThenSearch()
        }
    }

    private fun checkBarcodeThenSearch() {
        requireActivity().hideKeyboard()

        val code = binding.editTextBarcode.text.toString()
        when {
            code.isEmpty() -> {
                binding.editTextBarcode.error = resources.getString(R.string.txtBarcodeRequire)
            }
            !isBarcodeValid(code) -> {
                binding.editTextBarcode.error = resources.getString(R.string.txtBarcodeNotValid)
            }
            else -> {
                viewModel.openProduct(code, requireActivity())
            }
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
