package openfoodfacts.github.scrachx.openfood.features.product.view.environment

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentEnvironmentProductBinding
import openfoodfacts.github.scrachx.openfood.features.ImageOpenerUtil
import openfoodfacts.github.scrachx.openfood.features.images.manage.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class EnvironmentProductFragment : BaseFragment() {
    private var _binding: FragmentEnvironmentProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EnvironmentProductViewModel by viewModels()

    @Inject
    lateinit var productRepo: ProductRepository

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    @Inject
    lateinit var photoReceiverHandler: PhotoReceiverHandler

    /**
     * boolean to determine if image should be loaded or not
     */
    private val isLowBatteryMode by lazy { requireContext().isDisableImageLoad() && requireContext().isBatteryLevelLow() }

    private lateinit var productState: ProductState

    private var mUrlImage: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEnvironmentProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.imagePackagingUrl.observe(viewLifecycleOwner) { setPackagingImage(it) }
        viewModel.carbonFootprint.observe(viewLifecycleOwner) { setCarbonFootprint(it) }
        viewModel.environmentInfoCard.observe(viewLifecycleOwner) { setInfoCard(it) }
        viewModel.packaging.observe(viewLifecycleOwner) { setPackaging(it) }
        viewModel.recyclingInstructionsToDiscard.observe(viewLifecycleOwner) { setRecyclingInstructionsToDiscard(it) }
        viewModel.recyclingInstructionsToRecycle.observe(viewLifecycleOwner) { setRecyclingInstructionsToRecycle(it) }
        viewModel.statesTags.observe(viewLifecycleOwner) { refreshTagsPrompt(it) }

        refreshView(requireProductState())

        binding.imageViewPackaging.setOnClickListener { openFullScreenImage() }
    }

    private fun setRecyclingInstructionsToRecycle(recyclingInstructionsToRecycle: String?) {
        if (recyclingInstructionsToRecycle.isNullOrEmpty()) {
            binding.recyclingInstructionsRecycleCv.visibility = View.GONE
        } else {
            // TODO: 02/03/2021 i18n
            binding.recyclingInstructionToRecycle.text = buildSpannedString {
                bold { append("Recycling instructions - To recycle: ") }
                append(recyclingInstructionsToRecycle)
            }
        }
    }

    private fun setRecyclingInstructionsToDiscard(instructions: String?) {
        if (instructions == null) {
            binding.recyclingInstructionsDiscardCv.visibility = View.GONE
        } else {
            // TODO: 02/03/2021 i18n
            binding.recyclingInstructionToDiscard.text = buildSpannedString {
                bold { append("Recycling instructions - To discard: ") }
                append(instructions)
            }
        }
    }

    private fun setPackaging(packaging: String?) {
        if (packaging == null) {
            binding.packagingCv.visibility = View.GONE
        } else {
            binding.packagingText.text = buildSpannedString {
                bold { append(getString(R.string.packaging_environmentTab)) }
                append(" ")
                append(packaging)
            }
        }
    }

    private fun setPackagingImage(url: String?) {
        binding.packagingImagetipBox.setTipMessage(getString(R.string.onboarding_hint_msg, getString(R.string.image_edit_tip)))
        binding.packagingImagetipBox.loadToolTip()

        binding.addPhotoLabel.visibility = View.GONE

        // Load Image if isLowBatteryMode is false
        if (isLowBatteryMode) {
            binding.imageViewPackaging.visibility = View.GONE
        } else {
            picasso.load(url).into(binding.imageViewPackaging)
        }
        mUrlImage = url
    }

    private fun setCarbonFootprint(nutriment: ProductNutriments.ProductNutriment?) {
        if (nutriment == null) {
            binding.carbonFootprintCv.visibility = View.GONE
        } else {
            val carbonFootprintValue = nutriment.per100gInUnit?.getRoundNumber()
            val carbonFootprintSymbol = nutriment.unit.sym

            binding.textCarbonFootprint.text = buildSpannedString {
                bold { append(getString(R.string.textCarbonFootprint)) }
                append(carbonFootprintValue)
                append(carbonFootprintSymbol)
            }
        }
    }

    private fun setInfoCard(it: Spanned?) {
        if (it == null) {
            binding.environmentInfoCv.visibility = View.GONE
        } else {
            binding.environmentInfoText.append(it)
            binding.environmentInfoText.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun refreshView(productState: ProductState) {
        super.refreshView(productState)
        this.productState = productState

        viewModel.setProduct(productState.product!!)
    }

    private fun openFullScreenImage() {
        val imageUrl = mUrlImage
        val product = productState.product
        if (imageUrl != null && product != null) {
            lifecycleScope.launch {
                ImageOpenerUtil.startImageEditFromUrl(
                    requireActivity(),
                    productRepo,
                    product,
                    ProductImageField.PACKAGING,
                    imageUrl,
                    binding.imageViewPackaging,
                    localeManager.getLanguage()
                )
            }
        } else {
            newPackagingImage()
        }
    }

    private fun newPackagingImage() = doChooseOrTakePhotos()

    override fun doOnPhotosPermissionGranted() = doChooseOrTakePhotos()

    private fun loadPackagingPhoto(photoFile: File) {
        // Create a new instance of ProductImage so we can load to server
        val image = ProductImage(productState.product!!.code, ProductImageField.PACKAGING, photoFile, localeManager.getLanguage())
        image.filePath = photoFile.absolutePath

        // Load to server
        viewModel.postImg(image)

        // Load into view
        binding.addPhotoLabel.visibility = View.GONE
        mUrlImage = photoFile.absolutePath
        picasso
            .load(photoFile)
            .fit()
            .into(binding.imageViewPackaging)
    }

    //checks the product states_tags to determine which prompt to be shown
    private fun refreshTagsPrompt(stateTags: List<String>) {
        val showLabelsPrompt = ApiFields.StateTags.LABELS_TO_BE_COMPLETED in stateTags
        val showOriginsPrompt = ApiFields.StateTags.ORIGINS_TO_BE_COMPLETED in stateTags

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
            else -> {
                binding.addLabelOriginPrompt.visibility = View.GONE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // TODO: 15/11/2020 find a way to use ActivityResultApi
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data) { loadPackagingPhoto(it) }
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
