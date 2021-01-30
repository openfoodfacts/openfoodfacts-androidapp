package openfoodfacts.github.scrachx.openfood.features.product.view.environment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentEnvironmentProductBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Nutriments
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.File

class EnvironmentProductFragment : BaseFragment() {
    private lateinit var productState: ProductState
    private val api: OpenFoodAPIClient by lazy { OpenFoodAPIClient(requireActivity()) }
    private var _binding: FragmentEnvironmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var product: Product

    /**
     * boolean to determine if image should be loaded or not
     */
    private var isLowBatteryMode = false
    private var mUrlImage: String? = null
    private lateinit var photoReceiverHandler: PhotoReceiverHandler

    /**boolean to determine if labels prompt should be shown*/
    private var showLabelsPrompt = false

    /**boolean to determine if origins prompt should be shown*/
    private var showOriginsPrompt = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEnvironmentProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoReceiverHandler = PhotoReceiverHandler(this::loadPackagingPhoto)
        val langCode = LocaleHelper.getLanguage(context)
        productState = this.requireProductState()
        binding.imageViewPackaging.setOnClickListener { openFullScreen() }

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (requireContext().isDisableImageLoad() && requireContext().isBatteryLevelLow()) {
            isLowBatteryMode = true
        }

        product = productState.product!!
        val nutriments = product.nutriments

        val imagePackagingUrl = product.getImagePackagingUrl(langCode)
        if (!imagePackagingUrl.isNullOrBlank()) {
            binding.packagingImagetipBox.setTipMessage(getString(R.string.onboarding_hint_msg, getString(R.string.image_edit_tip)))
            binding.packagingImagetipBox.loadToolTip()
            binding.addPhotoLabel.visibility = View.GONE

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Utils.picassoBuilder(requireContext())
                        .load(imagePackagingUrl)
                        .into(binding.imageViewPackaging)
            } else {
                binding.imageViewPackaging.visibility = View.GONE
            }
            mUrlImage = imagePackagingUrl
        }

        val carbonFootprintNutriment = nutriments[Nutriments.CARBON_FOOTPRINT]
        if (carbonFootprintNutriment != null) {
            binding.textCarbonFootprint.text = bold(getString(R.string.textCarbonFootprint))
            binding.textCarbonFootprint.append(carbonFootprintNutriment.for100gInUnits)
            binding.textCarbonFootprint.append(carbonFootprintNutriment.unit)
        } else {
            binding.carbonFootprintCv.visibility = View.GONE
        }

        val environmentInfocard = product.environmentInfoCard
        if (!environmentInfocard.isNullOrEmpty()) {
            binding.environmentInfoText.append(HtmlCompat.fromHtml(environmentInfocard, HtmlCompat.FROM_HTML_MODE_COMPACT))
            binding.environmentInfoText.movementMethod = LinkMovementMethod.getInstance()
        } else {
            binding.environmentInfoCv.visibility = View.GONE
        }

        val packaging = product.packaging
        if (!packaging.isNullOrEmpty()) {
            binding.packagingText.text = bold(getString(R.string.packaging_environmentTab))
            binding.packagingText.append(" ")
            binding.packagingText.append(packaging.split(',').toString().removeSurrounding("[", "]"))
        } else {
            binding.packagingCv.visibility = View.GONE
        }

        val recyclingInstructionsToDiscard = product.recyclingInstructionsToDiscard
        if (!recyclingInstructionsToDiscard.isNullOrEmpty()) {
            binding.recyclingInstructionToDiscard.text = bold("Recycling instructions - To discard: ")
            binding.recyclingInstructionToDiscard.append(recyclingInstructionsToDiscard)
        } else {
            binding.recyclingInstructionsDiscardCv.visibility = View.GONE
        }

        val recyclingInstructionsToRecycle = product.recyclingInstructionsToRecycle
        if (!recyclingInstructionsToRecycle.isNullOrEmpty()) {
            binding.recyclingInstructionToRecycle.text = bold("Recycling instructions - To recycle:")
            binding.recyclingInstructionToRecycle.append(recyclingInstructionsToRecycle)
        } else {
            binding.recyclingInstructionsRecycleCv.visibility = View.GONE
        }

        refreshView(productState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun refreshView(productState: ProductState) {
        super.refreshView(productState)
        this.productState = productState

        refreshTagsPrompt()

    }

    private fun openFullScreen() {
        if (mUrlImage != null && productState.product != null) {
            FullScreenActivityOpener.openForUrl(
                    this,
                    productState.product!!,
                    ProductImageField.PACKAGING,
                    mUrlImage,
                    binding.imageViewPackaging,
            )
        } else {
            newPackagingImage()
        }
    }

    private fun newPackagingImage() {
        doChooseOrTakePhotos(getString(R.string.recycling_picture))
    }

    private fun loadPackagingPhoto(photoFile: File) {
        // Create a new instance of ProductImage so we can load to server
        val image = ProductImage(productState.product!!.code, ProductImageField.PACKAGING, photoFile)
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

    //checks the product states_tags to determine which prompt to be shown
    private fun refreshTagsPrompt() {
        val statesTags = product.statesTags
        showLabelsPrompt = statesTags.contains("en:labels-to-be-completed")
        showOriginsPrompt = statesTags.contains("en:origins-to-be-completed")

        binding.addLabelOriginPrompt.visibility = View.VISIBLE
        when {
            showLabelsPrompt && showOriginsPrompt -> {
                // showLabelsPrompt and showOriginsPrompt true
                binding.addLabelOriginPrompt.text = getString(R.string.add_labels_origins_prompt_text)
            }
            showLabelsPrompt -> {
                // showLabelsPrompt true
                binding.addLabelOriginPrompt.text = getString(R.string.add_labels_prompt_text)
            }
            showOriginsPrompt -> {
                // showOriginsPrompt true
                binding.addLabelOriginPrompt.text = getString(R.string.add_origins_prompt_text)
            }
            else -> binding.addLabelOriginPrompt.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // TODO: 15/11/2020 find a way to use ActivityResultApi
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data)
        if (requestCode == EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK && requireActivity().isUserSet()) {
            startEditProduct()
        }
        if (ImagesManageActivity.isImageModified(requestCode, resultCode)) {
            (activity as? ProductViewActivity)?.onRefresh()
        }
    }

    private fun startEditProduct() {
        startActivity(Intent(activity, ProductEditActivity::class.java).apply {
            putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, this@EnvironmentProductFragment.productState.product)
        })
    }

    companion object {
        private const val EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE = 1
    }
}