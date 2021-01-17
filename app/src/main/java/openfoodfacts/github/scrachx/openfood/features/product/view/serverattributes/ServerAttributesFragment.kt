package openfoodfacts.github.scrachx.openfood.features.product.view.serverattributes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import openfoodfacts.github.scrachx.openfood.databinding.FragmentServerAttributesBinding
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.utils.requireProductState

class ServerAttributesFragment : BaseFragment() {
    private lateinit var productState: ProductState
    private lateinit var binding: FragmentServerAttributesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productState = requireProductState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentServerAttributesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val attributeGroups = productState.product!!.getLocalAttributeGroups(requireContext())
        Log.i("ServerAttributes", attributeGroups.toString())

        binding.attrsList.setAdapter(AttributeGroupsAdapter(attributeGroups, requireActivity()))
    }

    companion object {
        fun newInstance(productState: ProductState) = ServerAttributesFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_STATE, productState)
            }
        }
    }
}