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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.validator.ChipifyingNachoValidator
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductIngredientsBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenNameDao
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Keys.lcIngredientsKey
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils.areChipsDifferent
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils.isDifferent
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils.isEmpty
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils.isNotEmpty
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader.download
import openfoodfacts.github.scrachx.openfood.utils.LOCALE_FILE_SCHEME
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.Utils.dpsToPixel
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import org.apache.commons.lang.StringUtils
import org.greenrobot.greendao.async.AsyncOperation
import org.greenrobot.greendao.async.AsyncOperationListener
import java.io.File
import java.util.*

/**
 * Fragment for Add Product Ingredients
 *
 * @see R.layout.fragment_add_product_ingredients
 */
class ProductEditIngredientsFragment : BaseFragment() {
    private var _binding: FragmentAddProductIngredientsBinding? = null
    private val binding get() = _binding!!
    private var photoReceiverHandler: PhotoReceiverHandler? = null
    private var mAllergenNameDao: AllergenNameDao? = null
    private var activity: Activity? = null
    private var photoFile: File? = null
    private var code: String? = null
    private val allergens: MutableList<String> = ArrayList()
    private var mOfflineSavedProduct: OfflineSavedProduct? = null
    private var productDetails: HashMap<String, String?>? = hashMapOf()
    private val disp = CompositeDisposable()
    private var imagePath: String? = null
    private var editProduct = false
    private var product: Product? = null
    private var newImageSelected = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductIngredientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoReceiverHandler = PhotoReceiverHandler { newPhotoFile: File ->
            val uri = newPhotoFile.toURI()
            imagePath = uri.path
            newImageSelected = true
            photoFile = newPhotoFile
            val image = ProductImage(code, ProductImageField.INGREDIENTS, newPhotoFile)
            image.filePath = uri.path
            if (activity is ProductEditActivity) {
                (activity as ProductEditActivity).addToPhotoMap(image, 1)
            }
            hideImageProgress(false, getString(R.string.image_uploaded_successfully))
        }
        binding.btnExtractIngredients.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_compare_arrows_black_18dp, 0, 0, 0)
        val intent = if (getActivity() == null) null else requireActivity().intent
        if (intent != null && intent.getBooleanExtra(ProductEditActivity.MODIFY_NUTRITION_PROMPT, false) && !intent
                        .getBooleanExtra(ProductEditActivity.MODIFY_CATEGORY_PROMPT, false)) {
            (getActivity() as ProductEditActivity?)!!.proceed()
        }
        binding.btnAddImageIngredients.setOnClickListener { addIngredientsImage() }
        binding.btnEditImageIngredients.setOnClickListener { onClickBtnEditImageIngredients() }
        binding.btnNext.setOnClickListener { next() }
        binding.btnLooksGood.setOnClickListener { ingredientsVerified() }
        binding.btnSkipIngredients.setOnClickListener { skipIngredients() }
        binding.btnExtractIngredients.setOnClickListener { onClickExtractIngredients() }
        binding.ingredientsList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Ignored
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Ignored
            }

            override fun afterTextChanged(s: Editable) {
                toggleExtractIngredientsButtonVisibility()
            }
        })
        val b = arguments
        if (b != null) {
            mAllergenNameDao = Utils.daoSession.allergenNameDao
            product = b.getSerializable("product") as Product?
            mOfflineSavedProduct = b.getSerializable("edit_offline_product") as OfflineSavedProduct?
            editProduct = b.getBoolean(ProductEditActivity.KEY_IS_EDITING)
            if (product != null) {
                code = product!!.code
            }
            if (editProduct && product != null) {
                code = product!!.code
                preFillProductValues()
            } else if (mOfflineSavedProduct != null) {
                code = mOfflineSavedProduct!!.barcode
                preFillValuesForOffline()
            } else {
                //addition
                val enabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("fastAdditionMode", false)
                enableFastAdditionMode(enabled)
            }
            if (b.getBoolean("perform_ocr")) {
                onClickExtractIngredients()
            }
            if (b.getBoolean("send_updated")) {
                onClickBtnEditImageIngredients()
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_ingredients, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
        val imageIngredients = imageIngredients
        if (binding.ingredientsList.isEmpty() && !imageIngredients.isNullOrEmpty()) {
            binding.btnExtractIngredients.visibility = View.VISIBLE
            imagePath = imageIngredients
        } else if (editProduct
                && binding.ingredientsList.isEmpty()
                && !product!!.imageIngredientsUrl.isNullOrEmpty()) {
            binding.btnExtractIngredients.visibility = View.VISIBLE
        }
        loadAutoSuggestions()
        if (getActivity() is ProductEditActivity && (getActivity() as ProductEditActivity?)!!.initialValues != null) {
            getAllDetails((getActivity() as ProductEditActivity?)!!.initialValues!!)
        }
    }

    private val imageIngredients: String?
        get() = productDetails!![ApiFields.Keys.IMAGE_INGREDIENTS]

    private val addProductActivity: ProductEditActivity?
        get() = getActivity() as ProductEditActivity?

    private fun extractTracesChipValues(product: Product?): List<String> {
        return product?.tracesTags?.map { getTracesName(getLanguage(activity), it) } ?: emptyList()
    }

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
        if (addProductActivity == null) {
            return
        }
        val newImageIngredientsUrl = product!!.getImageIngredientsUrl(addProductActivity!!.productLanguageForEdition)
        photoFile = null
        if (newImageIngredientsUrl != null && newImageIngredientsUrl.isNotEmpty()) {
            binding.imageProgress.visibility = View.VISIBLE
            imagePath = newImageIngredientsUrl
            picassoBuilder(activity)
                    .load(newImageIngredientsUrl)
                    .resize(dps50ToPixels(), dps50ToPixels())
                    .centerInside()
                    .into(binding.btnAddImageIngredients, object : Callback {
                        override fun onSuccess() {
                            imageLoaded()
                        }

                        override fun onError(ex: Exception) {
                            imageLoaded()
                        }
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
        val allergenName = mAllergenNameDao!!.queryBuilder().where(AllergenNameDao.Properties.AllergenTag.eq(tag), AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .unique()
        return if (allergenName != null) {
            allergenName.name
        } else tag
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disp.dispose()
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
        productDetails = mOfflineSavedProduct!!.productDetailsMap
        if (productDetails != null) {
            if (imageIngredients != null) {
                binding.imageProgress.visibility = View.VISIBLE
                picassoBuilder(activity)
                        .load(LOCALE_FILE_SCHEME + imageIngredients)
                        .resize(dps50ToPixels(), dps50ToPixels())
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
            val ingredientsText = mOfflineSavedProduct!!.ingredients
            if (!TextUtils.isEmpty(ingredientsText)) {
                binding.ingredientsList.setText(ingredientsText)
            }
            if (productDetails!![ApiFields.Keys.ADD_TRACES] != null) {
                val chipValues = productDetails!![ApiFields.Keys.ADD_TRACES]!!.split(Regex("\\s*,\\s*"))
                binding.traces.setText(chipValues)
            }
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
        asyncSessionAllergens.queryList(allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.LanguageCode.eq(appLanguageCode))
                .orderDesc(AllergenNameDao.Properties.Name).build())
        asyncSessionAllergens.listenerMainThread = AsyncOperationListener { operation: AsyncOperation ->
            val allergenNames = operation.result as List<AllergenName>
            allergens.clear()
            for (allergenName in allergenNames) {
                allergens.add(allergenName.name)
            }
            val adapter = ArrayAdapter(requireActivity(),
                    android.R.layout.simple_dropdown_item_1line, allergens)
            binding.traces.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN)
            binding.traces.setNachoValidator(ChipifyingNachoValidator())
            binding.traces.enableEditChipOnTouch(false, true)
            binding.traces.setAdapter(adapter)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity()
    }

    private fun addIngredientsImage() {
        if (imagePath != null) {
            if (photoFile != null) {
                cropRotateImage(photoFile, getString(R.string.ingredients_picture))
            } else {
                disp.add(download(requireContext(), imagePath!!)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { file: File? ->
                            photoFile = file
                            cropRotateImage(photoFile, getString(R.string.ingredients_picture))
                        })
            }
        } else {
            onClickBtnEditImageIngredients()
        }
    }

    private fun onClickBtnEditImageIngredients() {
        doChooseOrTakePhotos(getString(R.string.ingredients_picture))
    }

    override fun doOnPhotosPermissionGranted() {
        onClickBtnEditImageIngredients()
    }

    operator fun next() {
        val fragmentActivity: Activity? = getActivity()
        if (fragmentActivity is ProductEditActivity) {
            fragmentActivity.proceed()
        }
    }

    private fun ingredientsVerified() {
        binding.ingredientsListVerified.visibility = View.VISIBLE
        binding.traces.requestFocus()
        binding.btnLooksGood.visibility = View.GONE
        binding.btnSkipIngredients.visibility = View.GONE
    }

    private fun skipIngredients() {
        binding.ingredientsList.text = null
        binding.btnSkipIngredients.visibility = View.GONE
        binding.btnLooksGood.visibility = View.GONE
    }

    private fun onClickExtractIngredients() {
        (activity as? ProductEditActivity)?.let {
            val imagePath = imagePath
            if (imagePath != null && (!editProduct || newImageSelected)) {
                photoFile = File(imagePath)
                val image = ProductImage(code, ProductImageField.INGREDIENTS, photoFile)
                image.filePath = imagePath
                (activity as ProductEditActivity).addToPhotoMap(image, 1)
            } else if (imagePath != null) {
                (activity as ProductEditActivity).performOCR(code, "ingredients_" + (activity as ProductEditActivity).productLanguageForEdition)
            }
        }
    }

    private fun toggleExtractIngredientsButtonVisibility() {
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
            val languageCode = (activity as ProductEditActivity).productLanguageForEdition
            val lc = if (!languageCode.isNullOrEmpty()) languageCode else ApiFields.Defaults.DEFAULT_LANGUAGE
            targetMap[lcIngredientsKey(lc)] = binding.ingredientsList.text.toString()
            val list = binding.traces.chipValues
            val string = StringUtils.join(list, ",")
            targetMap[ApiFields.Keys.ADD_TRACES.substring(4)] = string
        }
    }

    /**
     * adds only those fields to the query map which are not empty and have changed.
     */
    fun addUpdatedFieldsTomap(targetMap: MutableMap<String, String?>) {
        binding.traces.chipifyAllUnterminatedTokens()
        if (activity !is ProductEditActivity) {
            return
        }
        if (binding.ingredientsList.isNotEmpty() && isDifferent(binding.ingredientsList, if (product != null) product!!.ingredientsText else null)) {
            val languageCode = (activity as ProductEditActivity).productLanguageForEdition
            val lc = if (!languageCode.isNullOrEmpty()) languageCode else ApiFields.Defaults.DEFAULT_LANGUAGE
            targetMap[lcIngredientsKey(lc)] = binding.ingredientsList.text.toString()
        }
        if (binding.traces.chipValues.isNotEmpty() && areChipsDifferent(binding.traces, extractTracesChipValues(product))) {
            val string = StringUtils.join(binding.traces.chipValues, ",")
            targetMap[ApiFields.Keys.ADD_TRACES] = string
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler!!.onActivityResult(this, requestCode, resultCode, data)
    }

    /**
     * Displays progress bar and hides other views util image is still loading
     */
    fun showImageProgress() {
        if (!isAdded) {
            return
        }
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
    fun hideImageProgress(errorInUploading: Boolean, message: String?) {
        if (!isAdded) {
            return
        }
        binding.imageProgress.visibility = View.INVISIBLE
        binding.imageProgressText.visibility = View.GONE
        binding.btnAddImageIngredients.visibility = View.VISIBLE
        binding.btnEditImageIngredients.visibility = View.VISIBLE
        if (!errorInUploading) {
            Picasso.get()
                    .load(photoFile!!)
                    .resize(dps50ToPixels(), dps50ToPixels())
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
        if (getActivity() != null && !requireActivity().isFinishing) {
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

    private fun dps50ToPixels(): Int {
        return dpsToPixel(50, getActivity())
    }
}