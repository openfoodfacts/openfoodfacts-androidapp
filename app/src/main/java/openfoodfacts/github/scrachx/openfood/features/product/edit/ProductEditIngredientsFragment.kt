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
package openfoodfacts.github.scrachx.openfood.features.product.edit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.validator.ChipifyingNachoValidator
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductIngredientsBinding
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_PERFORM_OCR
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_SEND_UPDATED
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenNameDao
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Keys.lcIngredientsKey
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader.download
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import org.greenrobot.greendao.async.AsyncOperationListener
import java.io.File
import java.util.*

/**
 * Fragment for Add Product Ingredients
 *
 * @see R.layout.fragment_add_product_ingredients
 */
class ProductEditIngredientsFragment : ProductEditFragment() {
    private var _binding: FragmentAddProductIngredientsBinding? = null
    private val binding get() = _binding!!

    private var photoReceiverHandler: PhotoReceiverHandler? = null
    private var mAllergenNameDao: AllergenNameDao? = null
    private var photoFile: File? = null
    private var code: String? = null
    private val allergens: MutableList<String> = ArrayList()
    private var mOfflineSavedProduct: OfflineSavedProduct? = null
    private var productDetails = mutableMapOf<String, String?>()
    private var imagePath: String? = null
    private var product: Product? = null
    private var newImageSelected = false


    override fun allValid() = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductIngredientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoReceiverHandler = PhotoReceiverHandler {
            val uri = it.toURI()
            imagePath = uri.path
            newImageSelected = true
            photoFile = it
            val image = ProductImage(code!!, ProductImageField.INGREDIENTS, it).apply {
                filePath = uri.path
            }
            (activity as? ProductEditActivity)?.addToPhotoMap(image, 1)
            hideImageProgress(false, getString(R.string.image_uploaded_successfully))
        }
        val intent = if (activity == null) null else requireActivity().intent
        if (intent != null && intent.getBooleanExtra(ProductEditActivity.KEY_MODIFY_NUTRITION_PROMPT, false) && !intent
                        .getBooleanExtra(ProductEditActivity.KEY_MODIFY_CATEGORY_PROMPT, false)) {
            (activity as ProductEditActivity?)!!.proceed()
        }
        binding.btnAddImageIngredients.setOnClickListener { addIngredientsImage() }
        binding.btnEditImageIngredients.setOnClickListener { editIngredientsImage() }
        binding.btnNext.setOnClickListener { next() }
        binding.btnLooksGood.setOnClickListener { verifyIngredients() }
        binding.btnSkipIngredients.setOnClickListener { skipIngredients() }
        binding.btnExtractIngredients.setOnClickListener { extractIngredients() }
        binding.ingredientsList.doAfterTextChanged { toggleOCRButtonVisibility() }

        val bundle = arguments
        if (bundle != null) {
            mAllergenNameDao = Utils.daoSession.allergenNameDao
            product = getProductFromArgs()
            mOfflineSavedProduct = getEditOfflineProductFromArgs()
            if (product != null) {
                code = product!!.code
            }
            if (isEditingFromArgs && product != null) {
                code = product!!.code
                preFillProductValues()
            } else if (mOfflineSavedProduct != null) {
                code = mOfflineSavedProduct!!.barcode
                preFillValuesForOffline()
            } else {
                // Fast addition
                val enabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("fastAdditionMode", false)
                enableFastAdditionMode(enabled)
            }
            if (bundle.getBoolean(KEY_PERFORM_OCR)) extractIngredients()

            if (bundle.getBoolean(KEY_SEND_UPDATED)) editIngredientsImage()
        } else {
            Toast.makeText(activity, R.string.error_adding_ingredients, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }

        val imageIngredients = getImageIngredients()
        if (binding.ingredientsList.isEmpty() && !imageIngredients.isNullOrEmpty()) {
            binding.btnExtractIngredients.visibility = View.VISIBLE
            imagePath = imageIngredients
        } else if (isEditingFromArgs
                && binding.ingredientsList.isEmpty()
                && !product!!.imageIngredientsUrl.isNullOrEmpty()) {
            binding.btnExtractIngredients.visibility = View.VISIBLE
        }

        loadAutoSuggestions()

        if (activity is ProductEditActivity && (activity as ProductEditActivity).initialValues != null) {
            getAllDetails((activity as ProductEditActivity).initialValues!!)
        }
    }

    private fun getImageIngredients() = productDetails[ApiFields.Keys.IMAGE_INGREDIENTS]

    private fun getAddProductActivity() = activity as ProductEditActivity?

    private fun extractTracesChipValues(product: Product?): List<String> =
            product?.tracesTags?.map { getTracesName(getLanguage(activity), it) } ?: emptyList()

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private fun preFillProductValues() {
        loadIngredientsImage()
        if (!product!!.ingredientsText.isNullOrEmpty()) {
            binding.ingredientsList.setText(product!!.ingredientsText)
        }
        if (product!!.tracesTags.isNotEmpty()) {
            val chipValues = extractTracesChipValues(product)
            binding.traces.setText(chipValues)
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
            picassoBuilder(requireContext())
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
     * returns alergen name from tag
     *
     * @param languageCode language in which additive name and tag are written
     * @param tag Tag associated with the allergen
     */
    private fun getTracesName(languageCode: String, tag: String): String {
        val allergenName = mAllergenNameDao!!.queryBuilder()
                .where(AllergenNameDao.Properties.AllergenTag.eq(tag), AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .unique()
        return allergenName?.name ?: tag
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * To enable fast addition mode
     */
    private fun enableFastAdditionMode(isEnabled: Boolean) {
        if (isEnabled) {
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
    private fun preFillValuesForOffline() {
        productDetails = mOfflineSavedProduct!!.productDetails.toMutableMap()
        if (getImageIngredients() != null) {
            binding.imageProgress.visibility = View.VISIBLE
            picassoBuilder(requireContext())
                    .load(LOCALE_FILE_SCHEME + getImageIngredients())
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
        mOfflineSavedProduct!!.ingredients.let {
            if (!it.isNullOrEmpty()) binding.ingredientsList.setText(it)
        }


        productDetails[ApiFields.Keys.ADD_TRACES]?.let {
            val chipValues = it.split(Regex("\\s*,\\s*"))
            binding.traces.setText(chipValues)
        }
    }

    /**
     * Automatically load suggestions for allergen names
     */
    private fun loadAutoSuggestions() {
        val daoSession = OFFApplication.daoSession
        val asyncSessionAllergens = daoSession.startAsyncSession()
        val allergenNameDao = daoSession.allergenNameDao
        val appLanguageCode = getLanguage(activity)

        asyncSessionAllergens.listenerMainThread = AsyncOperationListener { operation ->
            val allergenNames = operation.result as List<AllergenName>
            allergens.clear()
            allergenNames.forEach { allergens += it.name }

            val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_dropdown_item_1line, allergens)
            binding.traces.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN)
            binding.traces.setNachoValidator(ChipifyingNachoValidator())
            binding.traces.enableEditChipOnTouch(false, true)
            binding.traces.setAdapter(adapter)
        }

        asyncSessionAllergens.queryList(allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.LanguageCode.eq(appLanguageCode))
                .orderDesc(AllergenNameDao.Properties.Name).build())
    }

    override fun doOnPhotosPermissionGranted() = editIngredientsImage()

    private fun addIngredientsImage() {
        when {
            imagePath == null -> editIngredientsImage()
            photoFile != null -> cropRotateImage(photoFile, getString(R.string.ingredients_picture))
            else -> {
                download(requireContext(), imagePath!!)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { file ->
                            photoFile = file
                            cropRotateImage(photoFile, getString(R.string.ingredients_picture))
                        }.addTo(disp)
            }
        }
    }

    private fun editIngredientsImage() = doChooseOrTakePhotos(getString(R.string.ingredients_picture))

    private fun verifyIngredients() {
        binding.ingredientsListVerified.visibility = View.VISIBLE
        binding.traces.requestFocus()
        binding.btnLooksGood.visibility = View.GONE
        binding.btnSkipIngredients.visibility = View.GONE
    }

    private fun extractIngredients() {
        (activity as? ProductEditActivity)?.let {
            val imagePath = imagePath
            if (imagePath != null && (!isEditingFromArgs || newImageSelected)) {
                photoFile = File(imagePath)
                val image = ProductImage(code!!, ProductImageField.INGREDIENTS, photoFile!!)
                image.filePath = imagePath
                (activity as ProductEditActivity).addToPhotoMap(image, 1)
            } else if (imagePath != null) {
                (activity as ProductEditActivity).performOCR(code!!, "ingredients_" + (activity as ProductEditActivity).getProductLanguageForEdition())
            }
        }
    }

    private fun skipIngredients() {
        binding.ingredientsList.text = null
        binding.btnSkipIngredients.visibility = View.GONE
        binding.btnLooksGood.visibility = View.GONE
    }

    private fun toggleOCRButtonVisibility() {
        if (binding.ingredientsList.isEmpty()) {
            binding.btnExtractIngredients.visibility = View.VISIBLE
        } else {
            binding.btnExtractIngredients.visibility = View.GONE
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    private fun getAllDetails(targetMap: MutableMap<String, String?>) {
        binding.traces.chipifyAllUnterminatedTokens()
        if (activity is ProductEditActivity) {
            val languageCode = (activity as ProductEditActivity).getProductLanguageForEdition()
            val lc = if (!languageCode.isNullOrEmpty()) languageCode else ApiFields.Defaults.DEFAULT_LANGUAGE
            targetMap[lcIngredientsKey(lc)] = binding.ingredientsList.text.toString()
            val string = binding.traces.chipValues.joinToString(",")
            targetMap[ApiFields.Keys.ADD_TRACES.substring(4)] = string
        }
    }

    /**
     * adds only those fields to the query map which are not empty and have changed.
     */
    override fun addUpdatedFieldsToMap(targetMap: MutableMap<String, String?>) {
        binding.traces.chipifyAllUnterminatedTokens()
        if (activity !is ProductEditActivity) return

        if (binding.ingredientsList.isNotEmpty() && binding.ingredientsList.isContentDifferent(if (product != null) product!!.ingredientsText else null)) {
            val languageCode = (activity as ProductEditActivity).getProductLanguageForEdition()
            val lc = if (!languageCode.isNullOrEmpty()) languageCode else ApiFields.Defaults.DEFAULT_LANGUAGE
            targetMap[lcIngredientsKey(lc)] = binding.ingredientsList.text.toString()
        }
        if (binding.traces.chipValues.isNotEmpty() && binding.traces.areChipsDifferent(extractTracesChipValues(product))) {
            targetMap[ApiFields.Keys.ADD_TRACES] = binding.traces.chipValues.joinToString(",")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler!!.onActivityResult(this, requestCode, resultCode, data)
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
            Picasso.get()
                    .load(photoFile!!)
                    .resize(dps50ToPixels, dps50ToPixels)
                    .centerInside()
                    .into(binding.btnAddImageIngredients)
        }
    }

    /**
     * Display the list of ingredients based on the result from ocr of IngredientsList photo
     *
     * @param status status of ocr, in case of proper OCR it returns "set" or "0"
     * @param ocrResult resultant string obtained after OCR of image
     */
    fun setIngredients(status: String?, ocrResult: String?) {
        if (activity != null && !requireActivity().isFinishing) {
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
    }

    fun showOCRProgress() {
        binding.btnExtractIngredients.visibility = View.GONE
        binding.ingredientsList.text = null
        binding.ocrProgress.visibility = View.VISIBLE
        binding.ocrProgressText.visibility = View.VISIBLE
    }

    fun hideOCRProgress() {
        binding.ocrProgress.visibility = View.GONE
        binding.ocrProgressText.visibility = View.GONE
    }

    private val dps50ToPixels by lazy { requireContext().dpsToPixel(50) }
}