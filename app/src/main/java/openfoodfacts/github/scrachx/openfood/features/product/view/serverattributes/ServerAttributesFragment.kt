package openfoodfacts.github.scrachx.openfood.features.product.view.serverattributes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import openfoodfacts.github.scrachx.openfood.databinding.FragmentServerAttributesBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils

class ServerAttributesFragment : BaseFragment() {
    private lateinit var productState: ProductState
    private lateinit var binding: FragmentServerAttributesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productState = FragmentUtils.requireStateFromArguments(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentServerAttributesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val attributes = productState.product.getLocalAttributeGroups(requireContext())
                .mapNotNull { it.attributes }
                .flatMap { it.asList() }
        Log.i("ServerAttributes", attributes.toString())

        binding.attrsList.layoutManager = LinearLayoutManager(requireContext())
        binding.attrsList.adapter = AttributeAdapter(attributes)
    }
}