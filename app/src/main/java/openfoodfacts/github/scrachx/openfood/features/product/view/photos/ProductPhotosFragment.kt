package openfoodfacts.github.scrachx.openfood.features.product.view.photos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.databinding.FragmentProductPhotosBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.ImageNameJsonParser
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.requireProductState

/**
 * @author prajwalm
 * @see R.layout.fragment_product_photos
 */
class ProductPhotosFragment : BaseFragment() {
    private var _binding: FragmentProductPhotosBinding? = null
    private val binding get() = _binding!!

    private lateinit var openFoodAPIClient: OpenFoodAPIClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openFoodAPIClient = OpenFoodAPIClient(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val product = requireProductState().product!!
        CommonApiManager.productsApi
                .getProductImages(product.code)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(LOG_TAG, "Cannot download images from server", it) }
                .subscribe { node ->
                    binding.progress.hide()
                    val imageNames = ImageNameJsonParser.extractImagesNameSortedByUploadTimeDesc(node!!)

                    //Check if user is logged in
                    val adapter = ProductPhotosAdapter(requireContext(), product, imageNames, binding.root)
                    { position ->
                        // Retrieves url of the image clicked to open FullScreenActivity
                        var barcodePattern = product.code
                        if (barcodePattern.length > 8) {
                            barcodePattern = StringBuilder(product.code).let {
                                it.insert(3, "/")
                                it.insert(7, "/")
                                it.insert(11, "/")
                                it.toString()
                            }
                        }
                        openFullScreen("${BuildConfig.STATICURL}/images/products/$barcodePattern/${imageNames[position]}.jpg")
                    }.apply { addTo(disp) }
                    binding.imagesRecycler.adapter = adapter
                    binding.imagesRecycler.layoutManager = GridLayoutManager(context, 3)
                }.addTo(disp)
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
                putSerializable(ProductEditActivity.KEY_STATE, productState)
            }
        }
    }
}
