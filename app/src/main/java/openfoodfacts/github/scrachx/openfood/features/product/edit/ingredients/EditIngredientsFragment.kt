/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features.product.edit.ingredients

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.validator.ChipifyingNachoValidator
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsView
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductIngredientsBinding
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_PERFORM_OCR
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_SEND_UPDATED
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenNameDao
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Keys.lcIngredientsKey
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.File
import javax.inject.Inject

/**
 * Fragment for Add Product Ingredients
 *
 * @see R.layout.fragment_add_product_ingredients
 */
@AndroidEntryPoint
class EditIngredientsFragment : ProductEditFragment() {
    private var _binding: FragmentAddProductIngredientsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditIngredientsViewModel by viewModels()

    @Inject
    lateinit var daoSession: DaoSession

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var client: ProductRepository

    @Inject
    lateinit var fileDownloader: FileDownloader

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    /**
     * Executed when an image is returned from the camera
     */
    @Inject
    lateinit var photoReceiverHandler: PhotoReceiverHandler

    private var photoFile: File? = null
    private var code: String? = null

    private var offlineProduct: OfflineSavedProduct? = null
    private var product: Product? = null
    private var productDetails = mutableMapOf<String, String?>()

    private var imagePath: String? = null
    private var newImageSelected = false


    override fun allValid() = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductIngredientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIXME: DO NOT USE INTENTS IN FRAGMENTS
        val activityIntent = activity?.intent
        if (activityIntent != null
            && activityIntent.getBooleanExtra(ProductEditActivity.KEY_MODIFY_NUTRITION_PROMPT, false)
            && !activityIntent.getBooleanExtra(ProductEditActivity.KEY_MODIFY_CATEGORY_PROMPT, false)
        ) {
            (activity as ProductEditActivity).proceed()
        }

        binding.btnAddImageIngredients.setOnClickListener { addIngredientsImage() }
        binding.btnEditImageIngredients.setOnClickListener { editIngredientsImage() }
        binding.btnNext.setOnClickListener { next() }
        binding.btnLooksGood.setOnClickListener { verifyIngredients() }
        binding.btnSkipIngredients.setOnClickListener { skipIngredients() }
        binding.btnExtractIngredients.setOnClickListener { extractIngredients() }
        binding.ingredientsList.doAfterTextChanged { newText ->
            binding.btnExtractIngredients.isVisible = newText.isNullOrEmpty()
        }

        val bundle = arguments
        if (bundle == null) {
            Toast.makeText(activity, R.string.error_adding_ingredients, Toast.LENGTH_SHORT).show()

            requireActivity().finish()
            return
        }

        product = getProductFromArgs()
        offlineProduct = getEditOfflineProductFromArgs()

        if (product != null) {
            code = product!!.code
        }

        if (isEditingFromArgs && product != null) {
            code = product!!.code
            preFillProductValues(product!!)

        } else if (offlineProduct != null) {
            code = offlineProduct!!.barcode
            preFillValuesForOffline(offlineProduct!!)

        } else {
            // Fast addition
            val enabled = requireContext().isFastAdditionMode()
            setFastAdditionMode(enabled)
        }

        if (bundle.getBoolean(KEY_PERFORM_OCR)) extractIngredients()

        if (bundle.getBoolean(KEY_SEND_UPDATED)) editIngredientsImage()

        val ingredientsImg = getImageIngredients()
        if (binding.ingredientsList.isEmpty() && !ingredientsImg.isNullOrEmpty()) {
            binding.btnExtractIngredients.visibility = View.VISIBLE
            imagePath = ingredientsImg
        } else if (
            isEditingFromArgs
            && binding.ingredientsList.isEmpty()
            && !product!!.imageIngredientsUrl.isNullOrEmpty()
        ) {
            binding.btnExtractIngredients.visibility = View.VISIBLE
        }

        // Allergens autosuggestion
        viewModel.allergens.observe(viewLifecycleOwner) { loadAutoSuggestions(it) }

        (activity as? ProductEditActivity)?.let { getAllDetails(it) }
    }

    override fun onResume() {
        super.onResume()
        matomoAnalytics.trackView(AnalyticsView.ProductEditIngredients)
    }

    private fun getImageIngredients() = productDetails[ApiFields.Keys.IMAGE_INGREDIENTS]

    private fun getAddProductActivity() = activity as ProductEditActivity?

    private fun extractTracesChipValues(product: Product?): List<String> =
        product?.tracesTags
            ?.map { getTracesName(localeManager.getLanguage(), it) }
            ?: emptyList()

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private fun preFillProductValues(product: Product) {
        loadIngredientsImage()

        product.ingredientsText
            ?.takeUnless { it.isEmpty() }
            ?.let { binding.ingredientsList.setText(it) }

        product.takeUnless { it.tracesTags.isEmpty() }
            ?.let {
                binding.traces.setText(extractTracesChipValues(it))
            }
    }

    /**
     * Load ingredients image on the image view
     */
    fun loadIngredientsImage() {
        if (getAddProductActivity() == null) return

        val newImageIngredientsUrl = product!!.getImageIngredientsUrl(getAddProductActivity()!!.getProductLanguageForEdition())
        photoFile = null
        if (newImageIngredientsUrl != null && newImageIngredientsUrl.isNotEmpty()) {
            binding.imageProgress.visibility = View.VISIBLE
            imagePath = newImageIngredientsUrl
            picasso
                .load(newImageIngredientsUrl)
                .resize(dps50ToPixels, dps50ToPixels)
                .centerInside()
                .into(binding.btnAddImageIngredients, object : Callback {
                    override fun onSuccess() = imageLoaded()
                    override fun onError(ex: Exception) = imageLoaded()
                })
        }
    }

    /**
     * Set visibility parameters when image is loaded
     */
    private fun imageLoaded() {
        binding.btnEditImageIngredients.visibility = View.VISIBLE
        binding.imageProgress.visibility = View.GONE
    }

    /**
     * Returns allergen name or `tag` if nothing found.
     *
     * @param languageCode language in which additive name and tag are written
     * @param tag Tag associated with the allergen
     */
    private fun getTracesName(languageCode: String, tag: String): String {
        return daoSession.allergenNameDao.unique {
            where(AllergenNameDao.Properties.AllergenTag.eq(tag))
            where(AllergenNameDao.Properties.LanguageCode.eq(languageCode))
        }?.name ?: tag
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * To enable fast addition mode
     */
    private fun setFastAdditionMode(enabled: Boolean) {
        if (enabled) {
            binding.traces.visibility = View.GONE
            binding.sectionTraces.visibility = View.GONE
            binding.hintTraces.visibility = View.GONE
            binding.greyLine2.visibility = View.GONE
        } else {
            binding.traces.visibility = View.VISIBLE
            binding.sectionTraces.visibility = View.VISIBLE
            binding.hintTraces.visibility = View.VISIBLE
            binding.greyLine2.visibility = View.VISIBLE
        }
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private fun preFillValuesForOffline(prod: OfflineSavedProduct) {
        productDetails = prod.productDetails.toMutableMap()

        // Load ingredients image
        getImageIngredients()?.let {
            binding.imageProgress.visibility = View.VISIBLE
            picasso
                .load(LOCALE_FILE_SCHEME + it)
                .resize(dps50ToPixels, dps50ToPixels)
                .centerInside()
                .into(binding.btnAddImageIngredients, object : Callback {
                    override fun onSuccess() {
                        binding.imageProgress.visibility = View.GONE
                    }

                    override fun onError(ex: Exception) {
                        binding.imageProgress.visibility = View.GONE
                    }
                })
        }

        prod.ingredients
            ?.takeUnless { it.isEmpty() }
            ?.let { binding.ingredientsList.setText(it) }


        productDetails[ApiFields.Keys.ADD_TRACES]?.let {
            binding.traces.setText(it.split(Regex("\\s*,\\s*")))
        }
    }

    /**
     * Automatically load suggestions for allergen names
     */
    private fun loadAutoSuggestions(allergens: List<String>) {
        binding.traces.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN)
        binding.traces.setNachoValidator(ChipifyingNachoValidator())
        binding.traces.enableEditChipOnTouch(false, true)
        binding.traces.setAdapter(
            ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_dropdown_item_1line,
                allergens
            )
        )
    }


    override fun doOnPhotosPermissionGranted() = editIngredientsImage()

    private fun addIngredientsImage() {
        when {
            imagePath == null -> editIngredientsImage()
            photoFile != null -> cropRotateImage(photoFile!!, getString(R.string.ingredients_picture))
            else -> {
                lifecycleScope.launchWhenResumed {
                    val uri = fileDownloader.download(imagePath!!)
                    if (uri != null) {
                        photoFile = uri.toFile()
                        cropRotateImage(uri, getString(R.string.ingredients_picture))
                    }
                }
            }
        }
    }

    private fun editIngredientsImage() = doChooseOrTakePhotos()

    private fun verifyIngredients() {
        binding.ingredientsListVerified.visibility = View.VISIBLE
        binding.traces.requestFocus()
        binding.btnLooksGood.visibility = View.GONE
        binding.btnSkipIngredients.visibility = View.GONE
    }

    private fun extractIngredients() {
        (activity as? ProductEditActivity)?.let { activity ->

            imagePath?.let { imagePath ->
                if (!isEditingFromArgs || newImageSelected) {
                    photoFile = File(imagePath)
                    val image = ProductImage(code!!, ProductImageField.INGREDIENTS, photoFile!!, localeManager.getLanguage())
                    image.filePath = imagePath
                    activity.savePhoto(image, 1)
                } else {
                    activity.lifecycleScope.launch {
                        activity.performOCR(
                            code!!,
                            "ingredients_" + activity.getProductLanguageForEdition()
                        )
                    }
                }
            }

        }
    }

    private fun skipIngredients() {
        binding.ingredientsList.text = null
        binding.btnSkipIngredients.visibility = View.GONE
        binding.btnLooksGood.visibility = View.GONE
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    private fun getAllDetails(activity: ProductEditActivity) {
        activity.initialValues?.let { targetMap ->
            binding.traces.chipifyAllUnterminatedTokens()

            val lc = activity.getProductLanguageForEdition()
                ?.takeUnless { it.isEmpty() } ?: ApiFields.Defaults.DEFAULT_LANGUAGE

            targetMap[lcIngredientsKey(lc)] = binding.ingredientsList.text.toString()
            targetMap[ApiFields.Keys.ADD_TRACES.substring(4)] = binding.traces.chipValues.joinToString(",")
        }
    }

    /**
     * adds only those fields to the query map which are not empty and have changed.
     */
    override fun getUpdatedFieldsMap(): Map<String, String?> {
        // TODO: hacky fix, better use a shared view model for saving product details
        if (activity !is ProductEditActivity || _binding == null) return emptyMap()
        val targetMap = mutableMapOf<String, String?>()

        binding.traces.chipifyAllUnterminatedTokens()

        binding.ingredientsList
            .takeIf { it.isContentDifferent(product?.ingredientsText) }
            ?.let {
                val languageCode = (activity as ProductEditActivity).getProductLanguageForEdition()
                val lc = if (!languageCode.isNullOrEmpty()) languageCode else ApiFields.Defaults.DEFAULT_LANGUAGE
                targetMap[lcIngredientsKey(lc)] = it.getContent()
            }

        binding.traces
            .takeIf { it.isNotEmpty() && it.areChipsDifferent(extractTracesChipValues(product)) }
            ?.let {
                targetMap[ApiFields.Keys.ADD_TRACES] = it.chipValues.joinToString(",")
            }
        return targetMap
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data) {
            val uri = it.toURI()
            imagePath = uri.path
            newImageSelected = true
            photoFile = it
            val image = ProductImage(
                code!!,
                ProductImageField.INGREDIENTS,
                it,
                localeManager.getLanguage()
            ).apply {
                filePath = uri.path
            }

            (activity as? ProductEditActivity)?.savePhoto(image, 1)

            // Change UI state
            hideImageProgress(false, getString(R.string.image_uploaded_successfully))

            // Analytics
            matomoAnalytics.trackEvent(AnalyticsEvent.ProductIngredientsPictureEdited(code))
        }
    }

    /**
     * Displays progress bar and hides other views util image is still loading
     */
    override fun showImageProgress() {
        binding.imageProgress.visibility = View.VISIBLE
        binding.imageProgressText.visibility = View.VISIBLE
        binding.imageProgressText.setText(R.string.toastSending)
        binding.btnAddImageIngredients.visibility = View.INVISIBLE
        binding.btnEditImageIngredients.visibility = View.INVISIBLE
    }

    /**
     * After image is loaded hide image progress
     *
     * @param errorInUploading boolean variable is true, if there is an error while showing image
     * @param message error message in case of failure to display image
     */
    override fun hideImageProgress(errorInUploading: Boolean, message: String) {
        binding.imageProgress.visibility = View.INVISIBLE
        binding.imageProgressText.visibility = View.GONE
        binding.btnAddImageIngredients.visibility = View.VISIBLE
        binding.btnEditImageIngredients.visibility = View.VISIBLE

        if (!errorInUploading) {
            picasso.load(photoFile!!)
                .resize(dps50ToPixels, dps50ToPixels)
                .centerInside()
                .into(binding.btnAddImageIngredients)

            Toast.makeText(activity,R.string.ingredients_image_uploaded_successfully,Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Display the list of ingredients based on the result from ocr of IngredientsList photo
     *
     * @param status status of ocr, in case of proper OCR it returns "set" or "0"
     * @param ocrResult resultant string obtained after OCR of image
     */
    fun setIngredients(status: String?, ocrResult: String?) {
        if (activity == null || requireActivity().isFinishing) return
        when (status) {
            "set" -> {
                binding.ingredientsList.setText(ocrResult)
                loadIngredientsImage()
            }
            "0" -> {
                binding.ingredientsList.setText(ocrResult)
                binding.btnLooksGood.visibility = View.VISIBLE
                binding.btnSkipIngredients.visibility = View.VISIBLE
            }
            else -> Toast.makeText(activity, R.string.unable_to_extract_ingredients, Toast.LENGTH_SHORT).show()
        }
    }

    @UiThread
    fun showOCRProgress() {
        // Disable extract button and ingredients text field
        binding.btnExtractIngredients.isEnabled = false
        binding.ingredientsList.isEnabled = false

        // Delete ingredients text
        binding.ingredientsList.text = null

        // Show progress spinner and text
        binding.ocrProgress.visibility = View.VISIBLE
    }

    @UiThread
    fun hideOCRProgress() {
        // Re-enable extract button and ingredients text field
        binding.btnExtractIngredients.isEnabled = true
        binding.ingredientsList.isEnabled = true

        // Hide progress spinner and text
        binding.ocrProgress.visibility = View.GONE
    }

    private val dps50ToPixels by lazy { 50.toPx(requireContext()) }
}
