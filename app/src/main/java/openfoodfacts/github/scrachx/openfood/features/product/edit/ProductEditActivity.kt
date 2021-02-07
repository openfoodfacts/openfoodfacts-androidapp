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
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.app.OFFApplication.Companion.appComponent
import openfoodfacts.github.scrachx.openfood.databinding.ActivityEditProductBinding
import openfoodfacts.github.scrachx.openfood.features.product.ProductFragmentPagerAdapter
import openfoodfacts.github.scrachx.openfood.features.product.edit.overview.ProductEditOverviewFragment
import openfoodfacts.github.scrachx.openfood.images.IMG_ID
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.jobs.OfflineProductWorker.Companion.scheduleSync
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient.Companion.addToHistorySync
import openfoodfacts.github.scrachx.openfood.utils.OfflineProductService.getOfflineProductByBarcode
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession
import openfoodfacts.github.scrachx.openfood.utils.Utils.hideKeyboard
import openfoodfacts.github.scrachx.openfood.utils.Utils.isExternalStorageWritable
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import openfoodfacts.github.scrachx.openfood.utils.getProductState
import java.io.File
import java.io.IOException
import java.util.*

class ProductEditActivity : AppCompatActivity() {
    private var _binding: ActivityEditProductBinding? = null
    private val binding get() = _binding!!

    private val disp = CompositeDisposable()
    private val fragmentsBundle = Bundle()

    private val addProductPhotosFragment = ProductEditPhotosFragment()
    private val nutritionFactsFragment = ProductEditNutritionFactsFragment()
    private val ingredientsFragment = ProductEditIngredientsFragment()
    private val editOverviewFragment = ProductEditOverviewFragment()

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
        updateTimeLine(binding.overviewIndicator, overviewStage)
        updateTimeLine(binding.ingredientsIndicator, ingredientsStage)
        updateTimeLine(binding.nutritionFactsIndicator, nutritionFactsStage)
    }

    override fun onBackPressed() {
        MaterialDialog.Builder(this)
                .content(R.string.save_product)
                .positiveText(R.string.txtSave)
                .negativeText(R.string.txtPictureNeededDialogNo)
                .onPositive { _, _ -> checkFieldsThenSave() }
                .onNegative { _, _ -> super.onBackPressed() }
                .show()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            MaterialDialog.Builder(this)
                    .content(R.string.save_product)
                    .positiveText(R.string.txtSave)
                    .negativeText(R.string.txt_discard)
                    .onPositive { _, _ -> checkFieldsThenSave() }
                    .onNegative { _, _ -> finish() }
                    .show()
            true
        }
        R.id.save_product -> {
            checkFieldsThenSave()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun selectPage(position: Int) = when (position) {
        1 -> updateTimelineIndicator(2, 1, 0)
        2 -> updateTimelineIndicator(2, 2, 1)
        0 -> updateTimelineIndicator(1, 0, 0)
        else -> updateTimelineIndicator(1, 0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)
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
            offlineSavedProduct = getOfflineProductByBarcode(productState.product!!.code)
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
            imageIngredientsUploaded = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED].toBoolean()
            imageNutritionFactsUploaded = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_NUTRITION_UPLOADED].toBoolean()
        }
        if (productState == null && offlineSavedProduct == null && mEditProduct == null) {
            Toast.makeText(this, R.string.error_adding_product, Toast.LENGTH_SHORT).show()
            finish()
        }
        setupViewPager(binding.viewpager)
    }

    public override fun onDestroy() {
        super.onDestroy()
        disp.dispose()
        clearCameraCachedPics(this)
        _binding = null
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        // Initialize fragments
        val adapterResult = ProductFragmentPagerAdapter(this)
        fragmentsBundle.putSerializable("product", mProduct)

        editOverviewFragment.arguments = fragmentsBundle
        ingredientsFragment.arguments = fragmentsBundle

        adapterResult.add(editOverviewFragment, R.string.overview)
        adapterResult.add(ingredientsFragment, R.string.ingredients)

        // If on off or opff, add Nutrition Facts fragment
        when {
            isFlavors(OFF, OPFF) -> {
                nutritionFactsFragment.arguments = fragmentsBundle
                adapterResult.add(nutritionFactsFragment, R.string.nutrition_facts)
            }
            isFlavors(OBF, OPF) -> {
                binding.textNutritionFactsIndicator.setText(R.string.photos)
                addProductPhotosFragment.arguments = fragmentsBundle
                adapterResult.add(addProductPhotosFragment, R.string.photos)
            }
        }

        viewPager.offscreenPageLimit = 2
        viewPager.adapter = adapterResult
    }

    private fun createTextPlain(code: String) =
            RequestBody.create(MediaType.parse(OpenFoodAPIClient.MIME_TEXT), code)

    private fun addLoginPasswordInfo(imgMap: MutableMap<String, RequestBody?>) {
        val settings = getLoginPreferences()
        val login = settings.getString("user", "") ?: ""
        val password = settings.getString("pass", "") ?: ""
        if (login.isNotEmpty() && password.isNotEmpty()) {
            imgMap[ApiFields.Keys.USER_ID] = createTextPlain(login)
            imgMap[ApiFields.Keys.USER_PASS] = createTextPlain(password)
        }
        imgMap[ApiFields.Keys.USER_COMMENT] = createTextPlain(OpenFoodAPIClient.getCommentToUpload(login))
    }

    private fun saveProduct() {
        editOverviewFragment.addUpdatedFieldsToMap(productDetails)
        ingredientsFragment.addUpdatedFieldsToMap(productDetails)
        if (isFlavors(OFF, OPFF)) {
            nutritionFactsFragment.addUpdatedFieldsToMap(productDetails)
        }
        addLoginInfoToProductDetails(productDetails)
        saveProductOffline()
    }

    fun proceed() = if (binding.viewpager.currentItem < 2) {
        binding.viewpager.setCurrentItem(binding.viewpager.currentItem + 1, true)
    } else checkFieldsThenSave()

    /**
     * Save the current product in the offline db
     */
    private fun saveProductOffline() {
        // Add the images to the productDetails to display them in UI later.
        imagesFilePath[0]?.also { productDetails[ApiFields.Keys.IMAGE_FRONT] = it }
        imagesFilePath[1]?.also { productDetails[ApiFields.Keys.IMAGE_INGREDIENTS] = it }
        imagesFilePath[2]?.also { productDetails[ApiFields.Keys.IMAGE_NUTRITION] = it }

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
        val barcode = this@ProductEditActivity.productDetails[ApiFields.Keys.BARCODE]!!
        val toSaveOfflineProduct = OfflineSavedProduct(
                barcode,
                this@ProductEditActivity.productDetails
        )
        daoSession.offlineSavedProductDao!!.insertOrReplace(toSaveOfflineProduct)

        scheduleSync()
        daoSession.historyProductDao.addToHistorySync(toSaveOfflineProduct)

        Toast.makeText(this, R.string.productSavedToast, Toast.LENGTH_SHORT).show()
        hideKeyboard(this)
        setResult(RESULT_OK)
        finish()
    }

    private fun checkFieldsThenSave() = if (editingMode) {
        // edit mode, therefore do not check whether front image is empty or not however do check the nutrition facts values.
        if (isFlavors(OFF, OPFF) && nutritionFactsFragment.anyInvalid()) {
            // If there are any invalid field and there is nutrition data, scroll to the nutrition fragment
            binding.viewpager.setCurrentItem(2, true)
        } else saveProduct()
    } else {
        // add mode, check if we have required fields
        if (editOverviewFragment.anyInvalid()) {
            binding.viewpager.setCurrentItem(0, true)
        } else if (isFlavors(OFF, OPFF) && nutritionFactsFragment.anyInvalid()) {
            binding.viewpager.setCurrentItem(2, true)
        } else saveProduct()
    }

    private fun addLoginInfoToProductDetails(targetMap: MutableMap<String, String?>) {
        val settings = getLoginPreferences()
        val login = settings.getString("user", "") ?: ""
        val password = settings.getString("pass", "") ?: ""
        if (login.isNotEmpty() && password.isNotEmpty()) {
            targetMap[ApiFields.Keys.USER_ID] = login
            targetMap[ApiFields.Keys.USER_PASS] = password
        }
    }

    private fun switchToOverviewPage() = binding.viewpager.setCurrentItem(0, true)

    private fun switchToIngredientsPage() = binding.viewpager.setCurrentItem(1, true)

    private fun switchToNutritionFactsPage() = binding.viewpager.setCurrentItem(2, true)

    fun addToPhotoMap(image: ProductImage, position: Int) {
        val lang = getProductLanguageForEdition()
        var ocr = false
        val imgMap = hashMapOf<String, RequestBody?>()
        val imageField = createTextPlain("${image.imageField}_$lang")
        imgMap[ApiFields.Keys.BARCODE] = image.code
        imgMap["imagefield"] = imageField
        if (image.imgFront != null) {
            imagesFilePath[0] = image.filePath
            imgMap["""imgupload_front"; filename="front_$lang.png""""] = image.imgFront
        }
        if (image.imgIngredients != null) {
            imgMap["""imgupload_ingredients"; filename="ingredients_$lang.png""""] = image.imgIngredients
            ocr = true
            imagesFilePath[1] = image.filePath
        }
        if (image.imgNutrition != null) {
            imgMap["""imgupload_nutrition"; filename="nutrition_$lang.png""""] = image.imgNutrition
            imagesFilePath[2] = image.filePath
        }
        if (image.imgPackaging != null) {
            imgMap["""imgupload_packaging"; filename="packaging_$lang.png""""] = image.imgPackaging
            imagesFilePath[3] = image.filePath
        }
        if (image.imgOther != null) {
            imgMap["""imgupload_other"; filename="other_$lang.png""""] = image.imgOther
        }
        // Attribute the upload to the connected user
        addLoginPasswordInfo(imgMap)
        savePhoto(imgMap, image, position, ocr)
    }

    private fun savePhoto(imgMap: Map<String, RequestBody?>, image: ProductImage, position: Int, performOCR: Boolean) {
        productsApi.saveImageSingle(imgMap)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showImageProgress(position) }
                .doOnError {
                    // A network error happened
                    if (it is IOException) {
                        hideImageProgress(position, false, getString(R.string.no_internet_connection))
                        Log.e(LOGGER_TAG, it.message!!)
                        if (image.imageField === ProductImageField.OTHER) {
                            daoSession.toUploadProductDao!!.insertOrReplace(ToUploadProduct(
                                    image.barcode,
                                    image.filePath,
                                    image.imageField.toString()
                            ))
                        }
                    } else {
                        hideImageProgress(position, true, it.message ?: "Empty error.")
                        Log.i(this::class.simpleName, it.message ?: "Empty error.")
                        Toast.makeText(OFFApplication.instance, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
                .subscribe { jsonNode ->
                    val status = jsonNode["status"].asText()
                    if (status == "status not ok") {
                        val error = jsonNode["error"].asText()
                        val alreadySent = error == "This picture has already been sent."
                        if (alreadySent && performOCR) {
                            hideImageProgress(position, false, getString(R.string.image_uploaded_successfully))
                            performOCR(image.barcode!!, "ingredients_${getProductLanguageForEdition()}")
                        } else {
                            hideImageProgress(position, true, error)
                        }
                    } else {
                        when {
                            image.imageField === ProductImageField.FRONT -> {
                                imageFrontUploaded = true
                            }
                            image.imageField === ProductImageField.INGREDIENTS -> {
                                imageIngredientsUploaded = true
                            }
                            image.imageField === ProductImageField.NUTRITION -> {
                                imageNutritionFactsUploaded = true
                            }
                        }
                        hideImageProgress(position, false, getString(R.string.image_uploaded_successfully))
                        val imageField = jsonNode["imagefield"].asText()
                        val imgId = jsonNode["image"]["imgid"].asText()
                        if (position != 3 && position != 4) {
                            // Not OTHER image
                            setPhoto(image, imageField, imgId, performOCR)
                        }
                    }
                }.addTo(disp)
    }

    private fun setPhoto(image: ProductImage, imageField: String, imgId: String, performOCR: Boolean) {
        val queryMap = mapOf(IMG_ID to imgId, "id" to imageField)

        productsApi.editImageSingle(image.barcode, queryMap)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { e ->
                    if (e is IOException) {
                        if (performOCR) {
                            val view = findViewById<View>(R.id.coordinator_layout)
                            Snackbar.make(view, R.string.no_internet_unable_to_extract_ingredients, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.txt_try_again) { setPhoto(image, imageField, imgId, true) }.show()
                        }
                    } else {
                        Log.i(this::class.simpleName, e.message!!)
                        Toast.makeText(this@ProductEditActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                .subscribe { jsonNode ->
                    val status = jsonNode["status"].asText()
                    if (performOCR && status == "status ok") {
                        performOCR(image.barcode!!, imageField)
                    }
                }.addTo(disp)
    }

    fun performOCR(code: String, imageField: String) {
        productsApi.performOCR(code, imageField)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { ingredientsFragment.showOCRProgress() }
                .doOnError {
                    ingredientsFragment.hideOCRProgress()
                    if (it is IOException) {
                        val view = findViewById<View>(R.id.coordinator_layout)
                        Snackbar.make(view, R.string.no_internet_unable_to_extract_ingredients, BaseTransientBottomBar.LENGTH_INDEFINITE)
                                .setAction(R.string.txt_try_again) { performOCR(code, imageField) }
                                .show()
                    } else {
                        Log.e(this::class.simpleName, it.message, it)
                        Toast.makeText(this@ProductEditActivity, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
                .subscribe { node ->
                    ingredientsFragment.hideOCRProgress()
                    val status = node["status"].toString()
                    if (status == "0") {
                        val ocrResult = node["ingredients_text_from_image"].asText()
                        ingredientsFragment.setIngredients(status, ocrResult)
                    } else {
                        ingredientsFragment.setIngredients(status, null)
                    }
                }
                .addTo(disp)
    }

    private fun hideImageProgress(position: Int, errorUploading: Boolean, message: String) {
        when (position) {
            0 -> editOverviewFragment.hideImageProgress(errorUploading, message)
            1 -> ingredientsFragment.hideImageProgress(errorUploading, message)
            2 -> nutritionFactsFragment.hideImageProgress(errorUploading, message)
            3 -> editOverviewFragment.hideOtherImageProgress(errorUploading, message)
            4 -> addProductPhotosFragment.hideImageProgress(errorUploading, message)
        }
    }

    private fun showImageProgress(position: Int) {
        when (position) {
            0 -> editOverviewFragment.showImageProgress()
            1 -> ingredientsFragment.showImageProgress()
            2 -> nutritionFactsFragment.showImageProgress()
            3 -> editOverviewFragment.showOtherImageProgress()
            4 -> addProductPhotosFragment.showImageProgress()
        }
    }

    fun getProductLanguageForEdition() = productDetails[ApiFields.Keys.LANG]

    fun setProductLanguage(languageCode: String) {
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
                Intent(context, ProductEditActivity::class.java).apply {
                    putExtra(KEY_EDIT_PRODUCT, product)
                    putExtra(KEY_PERFORM_OCR, true)
                }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
    }

    class SendUpdatedImgContract : ActivityResultContract<Product?, Boolean>() {
        override fun createIntent(context: Context, product: Product?) =
                Intent(context, ProductEditActivity::class.java).apply {
                    putExtra(KEY_EDIT_PRODUCT, product)
                    putExtra(KEY_SEND_UPDATED, true)
                }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
    }

    open class EditProductContract : ActivityResultContract<Product, Boolean>() {
        override fun createIntent(context: Context, input: Product) =
                Intent(context, ProductEditActivity::class.java).apply {
                    putExtra(KEY_EDIT_PRODUCT, input)
                }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
    }

    class AddProductContract : EditProductContract() {
        override fun createIntent(context: Context, input: Product) =
                super.createIntent(context, input).apply { putExtra(KEY_IS_NEW_PRODUCT, true) }
    }

    companion object {
        private val LOGGER_TAG = ProductEditActivity::class.simpleName

        const val KEY_PERFORM_OCR = "perform_ocr"
        const val KEY_SEND_UPDATED = "send_updated"
        const val KEY_MODIFY_NUTRITION_PROMPT = "modify_nutrition_prompt"
        const val KEY_MODIFY_CATEGORY_PROMPT = "modify_category_prompt"

        const val KEY_EDIT_OFFLINE_PRODUCT = "edit_offline_product"
        const val KEY_EDIT_PRODUCT = "edit_product"

        const val KEY_IS_NEW_PRODUCT = "is_new_product"

        const val KEY_IS_EDITING = "is_edition"
        const val KEY_STATE = "state"

        private fun getCameraPicLocation(context: Context): File {
            var cacheDir = context.cacheDir
            if (isExternalStorageWritable()) {
                cacheDir = context.externalCacheDir
            }
            val picDir = File(cacheDir, "EasyImage")
            if (!picDir.exists()) {
                if (picDir.mkdirs()) Log.i(LOGGER_TAG, "Directory '${picDir.absolutePath}' created.")
                else Log.i(LOGGER_TAG, "Couldn't create directory '${picDir.absolutePath}'.")
            }
            return picDir
        }

        private fun clearCameraCachedPics(context: Context) {
            (getCameraPicLocation(context).listFiles() ?: return).forEach {
                if (it.delete()) Log.i(LOGGER_TAG, "Deleted cached photo '${it.absolutePath}'.")
                else Log.i(LOGGER_TAG, "Couldn't delete cached photo '${it.absolutePath}'.")
            }
        }

        private fun updateTimeLine(view: View, stage: Int) {
            when (stage) {
                0 -> view.setBackgroundResource(R.drawable.stage_inactive)
                1 -> view.setBackgroundResource(R.drawable.stage_active)
                2 -> view.setBackgroundResource(R.drawable.stage_complete)
            }
        }

        fun start(context: Context, product: Product, sendUpdated: Boolean = false, performOcr: Boolean = false) {
            Intent(context, ProductEditActivity::class.java).apply {
                putExtra(KEY_EDIT_PRODUCT, product)
                if (sendUpdated) putExtra(KEY_SEND_UPDATED, true)
                if (performOcr) putExtra(KEY_PERFORM_OCR, true)
                context.startActivity(this)
            }
        }

        fun start(context: Context, offlineProduct: OfflineSavedProduct, sendUpdated: Boolean = false, performOcr: Boolean = false) {
            Intent(context, ProductEditActivity::class.java).apply {
                putExtra(KEY_EDIT_OFFLINE_PRODUCT, offlineProduct)
                if (sendUpdated) putExtra(KEY_SEND_UPDATED, true)
                if (performOcr) putExtra(KEY_PERFORM_OCR, true)
                context.startActivity(this)
            }
        }
    }
}