package openfoodfacts.github.scrachx.openfood.features.product.view.serverattributes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.databinding.FragmentServerAttributesBinding
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.requireProductState
import javax.inject.Inject

@AndroidEntryPoint
class ServerAttributesFragment : BaseFragment() {
    private lateinit var productState: ProductState
    private lateinit var binding: FragmentServerAttributesBinding

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var localeManager: LocaleManager

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

        val attributeGroups = productState.product!!.getAttributeGroups(localeManager.getLanguage())
        binding.attrsList.setAdapter(AttributeGroupsAdapter(attributeGroups, requireActivity(), picasso))
    }

    companion object {
        fun newInstance(productState: ProductState) = ServerAttributesFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_STATE, productState)
            }
        }
    }
}
