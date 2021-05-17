package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.databinding.FragmentProductPhotosBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.extractImagesNameSortedByUploadTimeDesc
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.requireProductState
import javax.inject.Inject

/**
 * @author prajwalm
 * @see R.layout.fragment_product_photos
 */
@AndroidEntryPoint
class ProductPhotosFragment : BaseFragment() {
    private var _binding: FragmentProductPhotosBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var client: OpenFoodAPIClient

    @Inject
    lateinit var productsApi: ProductsAPI

    @Inject
    lateinit var picasso: Picasso

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val product = requireProductState().product!!

        viewLifecycleOwner.lifecycleScope.launch { loadImages(product) }
    }

    private suspend fun loadImages(product: Product) {
        val adapter = withContext(Dispatchers.IO) {
            val node = productsApi.getProductImages(product.code)
                .observeOn(AndroidSchedulers.mainThread()).await()

            val imageNames = node.extractImagesNameSortedByUploadTimeDesc()
            return@withContext ProductPhotosAdapter(
                requireContext(),
                picasso,
                client,
                product,
                imageNames,
                binding.root
            ) { position ->
                // Retrieves url of the image clicked to open FullScreenActivity
                val barcode = if (product.code.length <= 8) product.code
                else StringBuilder(product.code)
                    .insert(3, "/")
                    .insert(6 + 1, "/")
                    .insert(9 + 2, "/")
                    .toString()
                val image = imageNames[position]
                openFullScreen("${BuildConfig.STATICURL}/images/products/$barcode/$image.jpg")
            }.apply { addTo(disp) }
        }
        withContext(Dispatchers.Main) {
            binding.progress.hide()
            // Check if user is logged in
            binding.imagesRecycler.adapter = adapter
            binding.imagesRecycler.layoutManager = GridLayoutManager(context, 3)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Call an intent to open full screen activity for a given image
     *
     * @param mUrlImage url of the image in FullScreenImage
     */
    private fun openFullScreen(mUrlImage: String?) {
        FullScreenActivityOpener.openZoom(
            requireActivity(),
            mUrlImage ?: return,
            null
        )
    }

    companion object {
        private val LOG_TAG = ProductPhotosFragment::class.simpleName
        fun newInstance(productState: ProductState) = ProductPhotosFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_STATE, productState)
            }
        }
    }
}
