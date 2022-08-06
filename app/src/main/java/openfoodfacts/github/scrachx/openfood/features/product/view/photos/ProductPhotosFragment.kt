package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.databinding.FragmentProductPhotosBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.requireProductState
import javax.inject.Inject

/**
 * A fragment to display all the product images in a grid.
 *
 * It allows the user to select the images as front/ingredients/... images
 * of the product.
 *
 * @author prajwalm
 */
@AndroidEntryPoint
class ProductPhotosFragment : BaseFragment() {
    private var _binding: FragmentProductPhotosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductPhotosViewModel by viewModels()

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var picasso: Picasso

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val product = requireProductState().product!!

        viewModel.imageNames.flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { loadImages(product, it) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

    }

    private fun loadImages(product: Product, imageNames: List<String>) {
        val adapter = ProductPhotosAdapter(
            context = requireContext(),
            lifecycleOwner = this,
            picasso = picasso,
            productRepository = productRepository,
            product = product,
            imageNames = imageNames,
            snackView = binding.root,
            onImageTap = { index -> openImage(product, imageNames[index]) }
        )

        binding.progress.hide()
        // Check if user is logged in
        binding.imagesRecycler.adapter = adapter
        binding.imagesRecycler.layoutManager = GridLayoutManager(context, 3)
    }

    private fun openImage(product: Product, image: String) {
        val barcode = getBarcodeUrl(product)
        val imageUrl = "${BuildConfig.STATICURL}/images/products/$barcode/$image.jpg"
        return FullScreenActivityOpener.openZoom(
            requireActivity(),
            imageUrl,
            null
        )
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance(productState: ProductState) = ProductPhotosFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_STATE, productState)
            }
        }

        private fun getBarcodeUrl(product: Product): String {
            return if (product.code.length <= 8) {
                product.code
            } else {
                StringBuilder(product.code)
                    .insert(3, "/")
                    .insert(6 + 1, "/")
                    .insert(9 + 2, "/")
                    .toString()
            }
        }
    }
}
