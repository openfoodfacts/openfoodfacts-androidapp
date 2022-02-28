package openfoodfacts.github.scrachx.openfood.features.product.view.environment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentEnvironmentProductBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Nutriment
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class EnvironmentProductFragment : BaseFragment() {
    private lateinit var productState: ProductState

    @Inject
    lateinit var productRepo: ProductRepository

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    private var _binding: FragmentEnvironmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var product: Product

    /**
     * boolean to determine if image should be loaded or not
     */
    private var isLowBatteryMode = false
    private var mUrlImage: String? = null
    private val photoReceiverHandler by lazy {
        PhotoReceiverHandler(sharedPreferences) { loadPackagingPhoto(it) }
    }

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
        val langCode = localeManager.getLanguage()
        productState = requireProductState()
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
                picasso.load(imagePackagingUrl).into(binding.imageViewPackaging)
            } else {
                binding.imageViewPackaging.visibility = View.GONE
            }
            mUrlImage = imagePackagingUrl
        }

        val carbonFootprintNutriment = nutriments[Nutriment.CARBON_FOOTPRINT]
        if (carbonFootprintNutriment != null) {
            binding.textCarbonFootprint.text = buildSpannedString {
                bold { append(getString(R.string.textCarbonFootprint)) }
                append(getRoundNumber(carbonFootprintNutriment.per100gInUnit))
                append(carbonFootprintNutriment.unit.sym)
            }
        } else {
            binding.carbonFootprintCv.visibility = View.GONE
        }

        val environmentInfoCard = product.environmentInfoCard
        if (!environmentInfoCard.isNullOrEmpty()) {
            binding.environmentInfoText.append(HtmlCompat.fromHtml(environmentInfoCard, FROM_HTML_MODE_COMPACT))
            binding.environmentInfoText.movementMethod = LinkMovementMethod.getInstance()
        } else {
            binding.environmentInfoCv.visibility = View.GONE
        }

        val packaging = product.packaging
        if (!packaging.isNullOrEmpty()) {
            binding.packagingText.text = buildSpannedString {
                bold { append(getString(R.string.packaging_environmentTab)) }
                append(" ")
                append(packaging.replace(",", ", "))
            }
        } else {
            binding.packagingCv.visibility = View.GONE
        }

        val recyclingInstructionsToDiscard = product.recyclingInstructionsToDiscard
        if (!recyclingInstructionsToDiscard.isNullOrEmpty()) {
            // TODO: 02/03/2021 i18n
            binding.recyclingInstructionToDiscard.text = buildSpannedString {
                bold { append("Recycling instructions - To discard: ") }
                append(recyclingInstructionsToDiscard)
            }
        } else {
            binding.recyclingInstructionsDiscardCv.visibility = View.GONE
        }

        val recyclingInstructionsToRecycle = product.recyclingInstructionsToRecycle
        if (!recyclingInstructionsToRecycle.isNullOrEmpty()) {
            // TODO: 02/03/2021 i18n
            binding.recyclingInstructionToRecycle.text = buildSpannedString {
                bold { append("Recycling instructions - To recycle: ") }
                append(recyclingInstructionsToRecycle)
            }
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
        val imageUrl = mUrlImage
        val product = productState.product
        if (imageUrl != null && product != null) {
            lifecycleScope.launch {
                FullScreenActivityOpener.openForUrl(
                    this@EnvironmentProductFragment,
                    productRepo,
                    product,
                    ProductImageField.PACKAGING,
                    imageUrl,
                    binding.imageViewPackaging,
                    localeManager.getLanguage(),
                )
            }
        } else {
            newPackagingImage()
        }
    }

    private fun newPackagingImage() = doChooseOrTakePhotos()

    private fun loadPackagingPhoto(photoFile: File) {
        // Create a new instance of ProductImage so we can load to server
        val image = ProductImage(productState.product!!.code, ProductImageField.PACKAGING, photoFile, localeManager.getLanguage())
        image.filePath = photoFile.absolutePath

        // Load to server
        lifecycleScope.launch { productRepo.postImg(image) }

        // Load into view
        binding.addPhotoLabel.visibility = View.GONE
        mUrlImage = photoFile.absolutePath
        picasso
            .load(photoFile)
            .fit()
            .into(binding.imageViewPackaging)
    }

    //checks the product states_tags to determine which prompt to be shown
    private fun refreshTagsPrompt() {
        val statesTags = product.statesTags
        showLabelsPrompt = ApiFields.StateTags.LABELS_TO_BE_COMPLETED in statesTags
        showOriginsPrompt = ApiFields.StateTags.ORIGINS_TO_BE_COMPLETED in statesTags

        binding.addLabelOriginPrompt.visibility = View.VISIBLE
        when {
            showLabelsPrompt && showOriginsPrompt -> {
                binding.addLabelOriginPrompt.text = getString(R.string.add_labels_origins_prompt_text)
            }
            showLabelsPrompt -> {
                binding.addLabelOriginPrompt.text = getString(R.string.add_labels_prompt_text)
            }
            showOriginsPrompt -> {
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
            putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, productState.product)
        })
    }

    companion object {
        fun newInstance(productState: ProductState) = EnvironmentProductFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_STATE, productState)
            }
        }

        private const val EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE = 1
    }
}
