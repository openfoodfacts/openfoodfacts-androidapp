package openfoodfacts.github.scrachx.openfood.features.product.view.environment

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentEnvironmentProductBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Nutriments
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import openfoodfacts.github.scrachx.openfood.utils.Utils
import java.io.File

class EnvironmentProductFragment : BaseFragment() {
    private lateinit var productState: ProductState
    private lateinit var api: OpenFoodAPIClient
    private var _binding: FragmentEnvironmentProductBinding? = null
    private val binding get() = _binding!!
    private val disp = CompositeDisposable()

    /**
     * boolean to determine if image should be loaded or not
     */
    private var isLowBatteryMode = false
    private var mUrlImage: String? = null
    private var photoReceiverHandler: PhotoReceiverHandler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = OpenFoodAPIClient(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEnvironmentProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        disp.dispose()
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoReceiverHandler = PhotoReceiverHandler(this::loadPackagingPhoto)
        val langCode = LocaleHelper.getLanguage(context)
        productState = FragmentUtils.requireStateFromArguments(this)
        binding.imageViewPackaging.setOnClickListener { openFullScreen() }

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (Utils.isDisableImageLoad(requireContext()) && Utils.isBatteryLevelLow(requireContext())) {
            isLowBatteryMode = true
        }
        val product = productState.product
        val nutriments = product.nutriments
        if (product.getImagePackagingUrl(langCode).isNotBlank()) {
            binding.packagingImagetipBox.setTipMessage(getString(R.string.onboarding_hint_msg, getString(R.string.image_edit_tip)))
            binding.packagingImagetipBox.loadToolTip()
            binding.addPhotoLabel.visibility = View.GONE

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Utils.picassoBuilder(context)
                        .load(product.getImagePackagingUrl(langCode))
                        .into(binding.imageViewPackaging)
            } else {
                binding.imageViewPackaging.visibility = View.GONE
            }
            mUrlImage = product.getImagePackagingUrl(langCode)
        }

        val carbonFootprintNutriment = nutriments[Nutriments.CARBON_FOOTPRINT]
        if (carbonFootprintNutriment != null) {
            binding.textCarbonFootprint.text = Utils.bold(getString(R.string.textCarbonFootprint))
            binding.textCarbonFootprint.append(carbonFootprintNutriment.for100gInUnits)
            binding.textCarbonFootprint.append(carbonFootprintNutriment.unit)
        } else {
            binding.carbonFootprintCv.visibility = View.GONE
        }

        if (product.environmentInfocard != null && product.environmentInfocard.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.environmentInfoText.append(Html.fromHtml(product.environmentInfocard, Html.FROM_HTML_MODE_COMPACT))
            } else {
                @Suppress("DEPRECATION")
                binding.environmentInfoText.append(Html.fromHtml(product.environmentInfocard))
            }
        } else {
            binding.environmentInfoCv.visibility = View.GONE
        }

        if (product.recyclingInstructionsToDiscard != null && product.recyclingInstructionsToDiscard.isNotEmpty()) {
            binding.recyclingInstructionToDiscard.text = Utils.bold("Recycling instructions - To discard: ")
            binding.recyclingInstructionToDiscard.append(product.recyclingInstructionsToDiscard)
        } else {
            binding.recyclingInstructionsDiscardCv.visibility = View.GONE
        }

        if (product.recyclingInstructionsToRecycle != null && product.recyclingInstructionsToRecycle.isNotEmpty()) {
            binding.recyclingInstructionToRecycle.text = Utils.bold("Recycling instructions - To recycle:")
            binding.recyclingInstructionToRecycle.append(product.recyclingInstructionsToRecycle)
        } else {
            binding.recyclingInstructionsRecycleCv.visibility = View.GONE
        }

        refreshView(productState)
    }

    override fun refreshView(productState: ProductState) {
        super.refreshView(productState)
        this.productState = productState
    }

    private fun openFullScreen() {
        if (mUrlImage != null && productState.product != null) {
            FullScreenActivityOpener.openForUrl(this, productState.product, ProductImageField.PACKAGING, mUrlImage, binding.imageViewPackaging)
        } else {
            newPackagingImage()
        }
    }

    private fun newPackagingImage() {
        doChooseOrTakePhotos(getString(R.string.recycling_picture))
    }

    private fun loadPackagingPhoto(photoFile: File) {
        // Create a new instance of ProductImage so we can load to server
        val image = ProductImage(productState.product.code, ProductImageField.PACKAGING, photoFile)
        image.filePath = photoFile.absolutePath

        // Load to server
        disp.add(api.postImg(image).subscribe())

        // Load into view
        binding.addPhotoLabel.visibility = View.GONE
        mUrlImage = photoFile.absolutePath
        Picasso.get()
                .load(photoFile)
                .fit()
                .into(binding.imageViewPackaging)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // TODO: 15/11/2020 find a way to use ActivityResultApi
        photoReceiverHandler!!.onActivityResult(this, requestCode, resultCode, data)
        if (requestCode == EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK && isUserLoggedIn) {
            startEditProduct()
        }
        if (ImagesManageActivity.isImageModified(requestCode, resultCode) && activity is ProductViewActivity) {
            (activity as ProductViewActivity).onRefresh()
        }
    }

    private fun startEditProduct() {
        val intent = Intent(activity, ProductEditActivity::class.java)
        intent.putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, productState.product)
        startActivity(intent)
    }

    companion object {
        private const val EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE = 1
    }
}