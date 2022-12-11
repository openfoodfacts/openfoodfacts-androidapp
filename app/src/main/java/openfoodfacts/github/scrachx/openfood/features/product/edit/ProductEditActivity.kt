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

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.AppFlavor
import openfoodfacts.github.scrachx.openfood.AppFlavor.Companion.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.ActivityEditProductBinding
import openfoodfacts.github.scrachx.openfood.features.product.ProductFragmentPagerAdapter
import openfoodfacts.github.scrachx.openfood.features.product.edit.ingredients.EditIngredientsFragment
import openfoodfacts.github.scrachx.openfood.features.product.edit.nutrition.ProductEditNutritionFactsFragment
import openfoodfacts.github.scrachx.openfood.features.product.edit.overview.EditOverviewFragment
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.IMG_ID
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.jobs.ProductUploaderWorker.Companion.scheduleProductUpload
import openfoodfacts.github.scrachx.openfood.models.Barcode
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.repositories.OfflineProductRepository
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository.Companion.PNG_EXT
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository.Companion.addToHistory
import openfoodfacts.github.scrachx.openfood.utils.InstallationService
import openfoodfacts.github.scrachx.openfood.utils.Intent
import openfoodfacts.github.scrachx.openfood.utils.MediaTypes
import openfoodfacts.github.scrachx.openfood.utils.clearCameraCache
import openfoodfacts.github.scrachx.openfood.utils.getLoginPassword
import openfoodfacts.github.scrachx.openfood.utils.getLoginUsername
import openfoodfacts.github.scrachx.openfood.utils.getProductState
import openfoodfacts.github.scrachx.openfood.utils.hideKeyboard
import openfoodfacts.github.scrachx.openfood.utils.toRequestBody
import java.io.IOException
import javax.inject.Inject

// TODO: 12/10/2021 refactor to use an activity view model shared between fragments of ProductEditActivity
@AndroidEntryPoint
class ProductEditActivity : BaseActivity() {
    private var _binding: ActivityEditProductBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var client: ProductRepository

    @Inject
    lateinit var installationService: InstallationService

    @Inject
    lateinit var offlineRepository: OfflineProductRepository

    @Inject
    lateinit var daoSession: DaoSession

    @Inject
    lateinit var productsApi: ProductsAPI

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private val fragmentsBundle = Bundle()

    private val addProductPhotosFragment = ProductEditPhotosFragment()
    private val nutritionFactsFragment = ProductEditNutritionFactsFragment()
    private val ingredientsFragment = EditIngredientsFragment()
    private val editOverviewFragment = EditOverviewFragment()

    private val imagesFilePath = arrayOfNulls<String>(3)

    private var editingMode = false
    private var imageFrontUploaded = false
    private var imageIngredientsUploaded = false
    private var imageNutritionFactsUploaded = false

    var initialValues: MutableMap<String, String?>? = null
        private set

    private var mProduct: Product? = null
    private val productDetails = mutableMapOf<String, String?>()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.product_edit_menu, menu)
        return true
    }

    /**
     * This method is used to update the timeline.
     * 0 means inactive stage, 1 means active stage and 2 means completed stage
     *
     * @param overviewStage change the state of overview indicator
     * @param ingredientsStage change the state of ingredients indicator
     * @param nutritionFactsStage change the state of nutrition facts indicator
     */
    private fun updateTimelineIndicator(overviewStage: Int, ingredientsStage: Int, nutritionFactsStage: Int) {
        binding.overviewIndicator.updateTimeLine(overviewStage)
        binding.ingredientsIndicator.updateTimeLine(ingredientsStage)
        binding.nutritionFactsIndicator.updateTimeLine(nutritionFactsStage)
    }

    override fun onBackPressed() {
        // If the user changed something, alert before exiting
        if (getUpdatedFieldsMap().isNotEmpty()) showExitConfirmDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (getUpdatedFieldsMap().isNotEmpty()) {
                    showExitConfirmDialog()
                    true
                } else false
            }
            R.id.save_product -> {
                checkFieldsThenSave()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showExitConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.save_product)
            .setPositiveButton(R.string.txtSave) { _, _ -> checkFieldsThenSave() }
            .setNegativeButton(R.string.txt_discard) { _, _ -> super.onBackPressed() }
            .show()
    }

    private fun selectPage(position: Int) = when (position) {
        1 -> updateTimelineIndicator(2, 1, 0)
        2 -> updateTimelineIndicator(2, 2, 1)
        0 -> updateTimelineIndicator(1, 0, 0)
        else -> updateTimelineIndicator(1, 0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup view binding
        _binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle(R.string.offline_product_addition_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup onclick listeners
        binding.overviewIndicator.setOnClickListener { switchToOverviewPage() }
        binding.ingredientsIndicator.setOnClickListener { switchToIngredientsPage() }
        binding.nutritionFactsIndicator.setOnClickListener { switchToNutritionFactsPage() }
        binding.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = selectPage(position)
        })

        val productState = getProductState()
        var offlineSavedProduct = intent.getSerializableExtra(KEY_EDIT_OFFLINE_PRODUCT) as OfflineSavedProduct?

        val mEditProduct = intent.getSerializableExtra(KEY_EDIT_PRODUCT) as Product?
        if (intent.getBooleanExtra(KEY_PERFORM_OCR, false)) {
            fragmentsBundle.putBoolean(KEY_PERFORM_OCR, true)
        }
        if (intent.getBooleanExtra(KEY_SEND_UPDATED, false)) {
            fragmentsBundle.putBoolean(KEY_SEND_UPDATED, true)
        }
        if (productState != null) {
            mProduct = productState.product

            // Search if the barcode already exists in the OfflineSavedProducts db
            offlineSavedProduct = offlineRepository.getOfflineProductByBarcode(productState.product!!.code)
        }
        if (mEditProduct != null) {
            setTitle(R.string.edit_product_title)
            mProduct = mEditProduct
            editingMode = true
            fragmentsBundle.putBoolean(KEY_IS_EDITING, true)
            initialValues = mutableMapOf()
        } else if (offlineSavedProduct != null) {
            fragmentsBundle.putSerializable(KEY_EDIT_OFFLINE_PRODUCT, offlineSavedProduct)

            // Save the already existing images in productDetails for UI
            imagesFilePath[0] = offlineSavedProduct.imageFront
            imagesFilePath[1] = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_INGREDIENTS]
            imagesFilePath[2] = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_NUTRITION]

            // get the status of images from productDetailsMap, whether uploaded or not
            imageFrontUploaded = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_FRONT_UPLOADED].toBoolean()
            imageIngredientsUploaded =
                offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED].toBoolean()
            imageNutritionFactsUploaded =
                offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_NUTRITION_UPLOADED].toBoolean()
        }
        if (productState == null && offlineSavedProduct == null && mEditProduct == null) {
            Toast.makeText(this, R.string.error_adding_product, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setupViewPager(binding.viewpager)
    }

    public override fun onDestroy() {
        super.onDestroy()
        clearCameraCache()
        _binding = null
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        // Initialize fragments
        fragmentsBundle.putSerializable(KEY_PRODUCT, mProduct)

        editOverviewFragment.arguments = fragmentsBundle
        ingredientsFragment.arguments = fragmentsBundle

        val adapterResult = ProductFragmentPagerAdapter(this).also {
            it += editOverviewFragment to getString(R.string.overview)
            it += ingredientsFragment to getString(R.string.ingredients)
        }

        // If on off or opff, add Nutrition Facts fragment
        when {
            isFlavors(AppFlavor.OFF, AppFlavor.OPFF) -> {
                nutritionFactsFragment.arguments = fragmentsBundle
                adapterResult += nutritionFactsFragment to getString(R.string.nutrition_facts)
            }
            isFlavors(AppFlavor.OBF, AppFlavor.OPF) -> {
                binding.textNutritionFactsIndicator.setText(R.string.photos)
                addProductPhotosFragment.arguments = fragmentsBundle
                adapterResult += addProductPhotosFragment to getString(R.string.photos)
            }
        }

        viewPager.offscreenPageLimit = 2
        viewPager.adapter = adapterResult
    }

    private fun getLoginPasswordInfo(): Map<String, RequestBody> {
        val map = hashMapOf<String, RequestBody>()

        val login = getLoginUsername() ?: ""
        val password = getLoginPassword() ?: ""

        if (login.isNotEmpty() && password.isNotEmpty()) {
            map[ApiFields.Keys.USER_ID] = login.toRequestBody()
            map[ApiFields.Keys.USER_PASS] = password.toRequestBody()
        }

        val comment = ProductRepository.getCommentToUpload(this, installationService, login)
        map[ApiFields.Keys.USER_COMMENT] = comment.toRequestBody()
        return map
    }

    private suspend fun saveProduct() {
        productDetails += getUpdatedFieldsMap() + getLoginInfoMap()
        saveProductOffline()
    }

    private fun getUpdatedFieldsMap(): Map<String, String?> {
        val updatedValues = editOverviewFragment.getUpdatedFieldsMap().toMutableMap()
        updatedValues += ingredientsFragment.getUpdatedFieldsMap()

        if (isFlavors(AppFlavor.OFF, AppFlavor.OPFF))
            updatedValues += nutritionFactsFragment.getUpdatedFieldsMap()

        return updatedValues
    }

    fun proceed() = if (binding.viewpager.currentItem < 2) {
        binding.viewpager.setCurrentItem(binding.viewpager.currentItem + 1, true)
    } else checkFieldsThenSave()

    /**
     * Save the current product in the offline db
     */
    private suspend fun saveProductOffline() {
        // Add the images to the productDetails to display them in UI later.
        imagesFilePath[0]?.let { productDetails[ApiFields.Keys.IMAGE_FRONT] = it }
        imagesFilePath[1]?.let { productDetails[ApiFields.Keys.IMAGE_INGREDIENTS] = it }
        imagesFilePath[2]?.let { productDetails[ApiFields.Keys.IMAGE_NUTRITION] = it }

        // Add the status of images to the productDetails, whether uploaded or not
        if (imageFrontUploaded) {
            productDetails[ApiFields.Keys.IMAGE_FRONT_UPLOADED] = true.toString()
        }
        if (imageIngredientsUploaded) {
            productDetails[ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED] = true.toString()
        }
        if (imageNutritionFactsUploaded) {
            productDetails[ApiFields.Keys.IMAGE_NUTRITION_UPLOADED] = true.toString()
        }
        val barcode = productDetails[ApiFields.Keys.BARCODE]!!


        // Save product to local database
        val toSaveOffline = OfflineSavedProduct(barcode, productDetails)
        withContext(IO) { daoSession.offlineSavedProductDao.insertOrReplace(toSaveOffline) }

        // Add to history db
        daoSession.historyProductDao.addToHistory(toSaveOffline)

        scheduleProductUpload(this, sharedPreferences)

        Toast.makeText(this, R.string.productSavedToast, Toast.LENGTH_SHORT).show()
        hideKeyboard()

        // Report analytics
        if (editingMode) {
            matomoAnalytics.trackEvent(AnalyticsEvent.ProductEdited(productDetails[ApiFields.Keys.BARCODE]))
        } else {
            matomoAnalytics.trackEvent(AnalyticsEvent.ProductCreated(productDetails[ApiFields.Keys.BARCODE]))
        }

        setResult(RESULT_OK)
        finish()
    }

    private fun checkFieldsThenSave() {
        if (editingMode) {
            // edit mode, therefore do not check whether front image is empty
            // or not, however do check the nutrition facts values.
            if (isFlavors(AppFlavor.OFF, AppFlavor.OPFF) && nutritionFactsFragment.anyInvalid()) {
                // If there are any invalid field and there is nutrition data,
                // scroll to the nutrition fragment
                binding.viewpager.setCurrentItem(2, true)
                return
            }
        } else {
            // add mode, check if we have required fields
            if (editOverviewFragment.anyInvalid()) {
                binding.viewpager.setCurrentItem(0, true)
                return
            } else if (isFlavors(AppFlavor.OFF, AppFlavor.OPFF) && nutritionFactsFragment.anyInvalid()) {
                binding.viewpager.setCurrentItem(2, true)
                return
            }
        }
        // If all is correct, save the product
        lifecycleScope.launch { saveProduct() }
    }

    private fun getLoginInfoMap(): Map<String, String?> {
        val login = getLoginUsername()
        val password = getLoginPassword()

        return if (login.isNullOrEmpty() || password.isNullOrEmpty()) emptyMap()
        else mapOf(
            ApiFields.Keys.USER_ID to login,
            ApiFields.Keys.USER_PASS to password
        )
    }

    private fun switchToOverviewPage() = binding.viewpager.setCurrentItem(0, true)

    private fun switchToIngredientsPage() = binding.viewpager.setCurrentItem(1, true)

    private fun switchToNutritionFactsPage() = binding.viewpager.setCurrentItem(2, true)

    fun savePhoto(image: ProductImage, fragmentIndex: Int) {
        val lang = getProductLanguageForEdition()
        var ocr = false
        val imgMap = hashMapOf<String, RequestBody?>(
            ApiFields.Keys.BARCODE to image.getBarcodeBody(),
            "imagefield" to "${image.field}_$lang".toRequestBody()
        )
        imgMap[when (image.field) {
            ProductImageField.FRONT -> """imgupload_front"; filename="front_$lang$PNG_EXT"""
            ProductImageField.INGREDIENTS -> """imgupload_ingredients"; filename="ingredients_$lang$PNG_EXT"""
            ProductImageField.NUTRITION -> """imgupload_nutrition"; filename="nutrition_$lang$PNG_EXT"""
            ProductImageField.PACKAGING -> """imgupload_packaging"; filename="packaging_$lang$PNG_EXT"""
            ProductImageField.OTHER -> """imgupload_other"; filename="other_$lang$PNG_EXT"""
        }] = image.bytes.toRequestBody(MediaTypes.MIME_IMAGE)

        // Attribute the upload to the connected user
        imgMap += getLoginPasswordInfo()

        lifecycleScope.launch { savePhoto(imgMap, image, fragmentIndex, ocr) }

    }

    private suspend fun savePhoto(
        imgMap: Map<String, RequestBody?>,
        image: ProductImage,
        fragmentIndex: Int,
        performOCR: Boolean,
    ) = withContext(IO) {

        showImageProgress(fragmentIndex)

        val jsonNode = try {
            productsApi.saveImage(imgMap)
        } catch (err: Throwable) {
            // A network error happened
            if (err is IOException) {

                hideImageProgress(fragmentIndex, getString(R.string.no_internet_connection))

                logcat(LogPriority.ERROR, LOGGER_TAG) { err.asLog() }
                if (image.field === ProductImageField.OTHER) {
                    daoSession.toUploadProductDao.insertOrReplace(
                        ToUploadProduct(
                            image.barcode.raw,
                            image.filePath,
                            image.field.toString()
                        )
                    )
                }
            } else {
                Log.i(this::class.simpleName, err.message ?: "Empty error.")
                withContext(Main) {
                    hideImageProgress(fragmentIndex, err.message ?: "Empty error.", true)
                    Toast.makeText(this@ProductEditActivity, err.message, Toast.LENGTH_SHORT).show()
                }
            }
            return@withContext
        }

        val status = jsonNode["status"].asText()
        if (status == "status not ok") {
            val error = jsonNode["error"].asText()

            val alreadySent = error == "This picture has already been sent."
            if (alreadySent && performOCR) {
                hideImageProgress(fragmentIndex, getString(R.string.image_uploaded_successfully))
                withContext(Main) {
                    performOCR(
                        barcode = image.barcode,
                        imageField = "ingredients_${getProductLanguageForEdition()}"
                    )
                }
            } else {
                hideImageProgress(fragmentIndex, error, true)
            }
        } else {
            when (image.field) {
                ProductImageField.FRONT -> {
                    imageFrontUploaded = true
                }
                ProductImageField.INGREDIENTS -> {
                    imageIngredientsUploaded = true
                }
                ProductImageField.NUTRITION -> {
                    imageNutritionFactsUploaded = true
                }
                else -> {
                    // Do nothing
                }
            }

            hideImageProgress(fragmentIndex, getString(R.string.image_uploaded_successfully))

            val imageField = jsonNode["imagefield"].asText()
            val imgId = jsonNode["image"]["imgid"].asText()
            if (fragmentIndex != 3 && fragmentIndex != 4) {
                // Not OTHER image
                setPhoto(image, imageField, imgId, performOCR)
            }
        }
    }

    private suspend fun setPhoto(image: ProductImage, imageField: String, imgId: String, performOCR: Boolean) {
        val queryMap = mapOf(
            IMG_ID to imgId,
            "id" to imageField
        )

        val jsonNode = withContext(IO) {
            try {
                productsApi.editImage(image.barcode.raw, queryMap)
            } catch (err: Exception) {
                if (err is IOException) {
                    if (performOCR) {
                        val view = findViewById<View>(R.id.coordinator_layout)
                        Snackbar.make(
                            view,
                            R.string.no_internet_unable_to_extract_ingredients,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(R.string.txt_try_again) {
                            lifecycleScope.launch { setPhoto(image, imageField, imgId, true) }
                        }.show()
                    }
                } else {
                    withContext(Main) {
                        logcat(LogPriority.WARN) { err.asLog() }
                        Toast.makeText(this@ProductEditActivity, err.message, Toast.LENGTH_SHORT).show()
                    }
                }
                return@withContext null
            }
        } ?: return

        withContext(Main) {
            val status = jsonNode["status"].asText()
            if (performOCR && status == "status ok") {
                performOCR(image.barcode, imageField)
            }
        }

    }


    suspend fun performOCR(barcode: Barcode, imageField: String) {
        withContext(Main) { ingredientsFragment.showOCRProgress() }

        val result = kotlin.runCatching {
            withContext(IO) { productsApi.performOCR(barcode.raw, imageField) }
        }.onFailure {
            withContext(Main) { ingredientsFragment.hideOCRProgress() }
            if (it is IOException) {
                val view = findViewById<View>(R.id.coordinator_layout)
                Snackbar.make(view, R.string.no_internet_unable_to_extract_ingredients, LENGTH_INDEFINITE)
                    .setAction(R.string.txt_try_again) {
                        lifecycleScope.launch { performOCR(barcode, imageField) }
                    }
                    .show()
            } else {
                Log.e(this::class.simpleName, it.message, it)
                Toast.makeText(this@ProductEditActivity, it.message, Toast.LENGTH_SHORT).show()
            }
        }

        val node = result.getOrNull() ?: return


        withContext(Main) {
            ingredientsFragment.hideOCRProgress()

            val status = node["status"].asText()
            if (status == "0") {
                val ocrResult = node["ingredients_text_from_image"].asText()
                ingredientsFragment.setIngredients(status, ocrResult)
            } else {
                ingredientsFragment.setIngredients(status, null)
            }
        }
    }

    private suspend fun hideImageProgress(
        position: Int,
        msg: String,
        error: Boolean = false,
    ) = withContext(Main) {
        when (position) {
            0 -> editOverviewFragment.hideImageProgress(error, msg)
            1 -> ingredientsFragment.hideImageProgress(error, msg)
            2 -> nutritionFactsFragment.hideImageProgress(error, msg)
            3 -> editOverviewFragment.hideOtherImageProgress(error, msg)
            4 -> addProductPhotosFragment.hideImageProgress(error, msg)
        }
    }

    private suspend fun showImageProgress(position: Int) = withContext(Main) {
        when (position) {
            0 -> editOverviewFragment.showImageProgress()
            1 -> ingredientsFragment.showImageProgress()
            2 -> nutritionFactsFragment.showImageProgress()
            3 -> editOverviewFragment.showOtherImageProgress()
            4 -> addProductPhotosFragment.showImageProgress()
        }
    }

    fun getProductLanguageForEdition() = productDetails[ApiFields.Keys.LANG]

    fun setProductLanguageCode(languageCode: String) {
        productDetails[ApiFields.Keys.LANG] = languageCode
    }

    fun updateLanguage() {
        ingredientsFragment.loadIngredientsImage()
        nutritionFactsFragment.loadNutritionImage()
    }

    fun setIngredients(status: String?, ingredients: String?) =
        ingredientsFragment.setIngredients(status, ingredients)

    class PerformOCRContract : ActivityResultContract<Product?, Boolean>() {
        override fun createIntent(context: Context, product: Product?) =
            Intent<ProductEditActivity>(context) {
                putExtra(KEY_EDIT_PRODUCT, product)
                putExtra(KEY_PERFORM_OCR, true)
            }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
    }

    class SendUpdatedImgContract : ActivityResultContract<Product, Boolean>() {
        override fun createIntent(context: Context, input: Product) =
            Intent<ProductEditActivity>(context) {
                putExtra(KEY_EDIT_PRODUCT, input)
                putExtra(KEY_SEND_UPDATED, true)
            }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
    }

    open class EditProductContract : ActivityResultContract<Product, Boolean>() {
        override fun createIntent(context: Context, input: Product) =
            Intent<ProductEditActivity>(context) {
                putExtra(KEY_EDIT_PRODUCT, input)
            }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
    }

    companion object {
        private val LOGGER_TAG = ProductEditActivity::class.simpleName

        const val KEY_PERFORM_OCR = "perform_ocr"
        const val KEY_SEND_UPDATED = "send_updated"
        const val KEY_MODIFY_NUTRITION_PROMPT = "modify_nutrition_prompt"
        const val KEY_MODIFY_CATEGORY_PROMPT = "modify_category_prompt"

        const val KEY_EDIT_OFFLINE_PRODUCT = "edit_offline_product"
        const val KEY_EDIT_PRODUCT = "edit_product"
        const val KEY_PRODUCT = "product"

        const val KEY_IS_EDITING = "is_edition"
        const val KEY_STATE = "state"


        private fun View.updateTimeLine(stage: Int) {
            when (stage) {
                0 -> setBackgroundResource(R.drawable.stage_inactive)
                1 -> setBackgroundResource(R.drawable.stage_active)
                2 -> setBackgroundResource(R.drawable.stage_complete)
            }
        }

        fun start(
            context: Context,
            product: Product,
            sendUpdated: Boolean = false,
            performOcr: Boolean = false,
            showCategoryPrompt: Boolean = false,
            showNutritionPrompt: Boolean = false,
        ) {
            Intent<ProductEditActivity>(context).apply {
                putExtra(KEY_EDIT_PRODUCT, product)

                if (sendUpdated) putExtra(KEY_SEND_UPDATED, true)
                if (performOcr) putExtra(KEY_PERFORM_OCR, true)
                if (showCategoryPrompt) putExtra(KEY_MODIFY_CATEGORY_PROMPT, true)
                if (showNutritionPrompt) putExtra(KEY_MODIFY_NUTRITION_PROMPT, true)

                context.startActivity(this)
            }
        }

        fun start(
            context: Context,
            offlineProduct: OfflineSavedProduct,
            sendUpdated: Boolean = false,
            performOcr: Boolean = false,
            showCategoryPrompt: Boolean = false,
            showNutritionPrompt: Boolean = false,
        ) {
            Intent<ProductEditActivity>(context).apply {
                putExtra(KEY_EDIT_OFFLINE_PRODUCT, offlineProduct)

                if (sendUpdated) putExtra(KEY_SEND_UPDATED, true)
                if (performOcr) putExtra(KEY_PERFORM_OCR, true)
                if (showCategoryPrompt) putExtra(KEY_MODIFY_CATEGORY_PROMPT, true)
                if (showNutritionPrompt) putExtra(KEY_MODIFY_NUTRITION_PROMPT, true)

                context.startActivity(this)
            }
        }


    }
}
