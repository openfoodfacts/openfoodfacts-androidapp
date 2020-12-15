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
@file:Suppress("KotlinDeprecation")

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
import com.fasterxml.jackson.databind.JsonNode
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.app.OFFApplication.Companion.appComponent
import openfoodfacts.github.scrachx.openfood.databinding.ActivityEditProductBinding
import openfoodfacts.github.scrachx.openfood.features.product.ProductFragmentPagerAdapter
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.jobs.OfflineProductWorker.Companion.scheduleSync
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProductDao
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
    private val addProductPhotosFragment = ProductEditPhotosFragment()
    private val disp = CompositeDisposable()
    private val fragmentsBundle = Bundle()
    private val imagesFilePath = arrayOfNulls<String>(3)


    private var _binding: ActivityEditProductBinding? = null
    private val binding get() = _binding!!
    private val productEditIngredientsFragment = ProductEditIngredientsFragment()
    private var editingMode = false
    private val productEditNutritionFactsFragment = ProductEditNutritionFactsFragment()
    private var imageFrontUploaded = false
    private var imageIngredientsUploaded = false
    private var imageNutritionFactsUploaded = false
    private val productEditOverviewFragment = ProductEditOverviewFragment()
    var initialValues: MutableMap<String, String?>? = null
        private set
    private var mOfflineSavedProductDao: OfflineSavedProductDao? = null
    private var mProduct: Product? = null
    private var mToUploadProductDao: ToUploadProductDao? = null
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
        updateTimeLine(overviewStage, binding.overviewIndicator)
        updateTimeLine(ingredientsStage, binding.ingredientsIndicator)
        updateTimeLine(nutritionFactsStage, binding.nutritionFactsIndicator)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            MaterialDialog.Builder(this)
                    .content(R.string.save_product)
                    .positiveText(R.string.txtSave)
                    .negativeText(R.string.txt_discard)
                    .onPositive { _, _ -> checkFieldsThenSave() }
                    .onNegative { _, _ -> finish() }
                    .show()
            return true
        } else if (itemId == R.id.save_product) {
            checkFieldsThenSave()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun selectPage(position: Int) {
        when (position) {
            1 -> updateTimelineIndicator(2, 1, 0)
            2 -> updateTimelineIndicator(2, 2, 1)
            0 -> updateTimelineIndicator(1, 0, 0)
            else -> updateTimelineIndicator(1, 0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent!!.inject(this)
        super.onCreate(savedInstanceState)

        // Setup view binding
        _binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup onclick listeners
        binding.overviewIndicator.setOnClickListener { switchToOverviewPage() }
        binding.ingredientsIndicator.setOnClickListener { switchToIngredientsPage() }
        binding.nutritionFactsIndicator.setOnClickListener { switchToNutritionFactsPage() }
        binding.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectPage(position)
            }
        })
        setTitle(R.string.offline_product_addition_title)
        val actionBar = supportActionBar
        if (actionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        mToUploadProductDao = daoSession.toUploadProductDao
        mOfflineSavedProductDao = daoSession.offlineSavedProductDao
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
            imagesFilePath[1] = offlineSavedProduct.productDetailsMap[ApiFields.Keys.IMAGE_INGREDIENTS]
            imagesFilePath[2] = offlineSavedProduct.productDetailsMap[ApiFields.Keys.IMAGE_NUTRITION]
            // get the status of images from productDetailsMap, whether uploaded or not
            imageFrontUploaded = "true" == offlineSavedProduct.productDetailsMap[ApiFields.Keys.IMAGE_FRONT_UPLOADED]
            imageIngredientsUploaded = "true" == offlineSavedProduct.productDetailsMap[ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED]
            imageNutritionFactsUploaded = "true" == offlineSavedProduct.productDetailsMap[ApiFields.Keys.IMAGE_NUTRITION_UPLOADED]
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
        productEditOverviewFragment.arguments = fragmentsBundle
        productEditIngredientsFragment.arguments = fragmentsBundle
        adapterResult.addFragment(productEditOverviewFragment, "Overview")
        adapterResult.addFragment(productEditIngredientsFragment, "Ingredients")

        // If on off or opff, add Nutrition Facts fragment
        if (isNutritionDataAvailable()) {
            productEditNutritionFactsFragment.arguments = fragmentsBundle
            adapterResult.addFragment(productEditNutritionFactsFragment, "Nutrition Facts")
        } else if (isFlavors(AppFlavors.OBF, AppFlavors.OPF)) {
            binding.textNutritionFactsIndicator.setText(R.string.photos)
            addProductPhotosFragment.arguments = fragmentsBundle
            adapterResult.addFragment(addProductPhotosFragment, "Photos")
        }
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = adapterResult
    }

    private fun createTextPlain(code: String): RequestBody {
        return RequestBody.create(MediaType.parse(OpenFoodAPIClient.MIME_TEXT), code)
    }

    private fun addLoginPasswordInfo(imgMap: MutableMap<String, RequestBody?>) {
        val settings = getLoginPreferences()
        val login = settings.getString("user", "") ?: ""
        val password = settings.getString("pass", "") ?: ""
        if (login.isNotEmpty() && password.isNotEmpty()) {
            imgMap[ApiFields.Keys.USER_ID] = createTextPlain(login)
            imgMap[ApiFields.Keys.USER_PASS] = createTextPlain(password)
        }
        imgMap["comment"] = createTextPlain(OpenFoodAPIClient.getCommentToUpload(login))
    }

    private fun saveProduct() {
        productEditOverviewFragment.addUpdatedFieldsToMap(productDetails)
        productEditIngredientsFragment.addUpdatedFieldsTomap(productDetails)
        if (isNutritionDataAvailable()) {
            productEditNutritionFactsFragment.addUpdatedFieldsToMap(productDetails)
        }
        addLoginInfoToProductDetails(productDetails)
        saveProductOffline()
    }

    fun proceed() {
        when (binding.viewpager.currentItem) {
            0 -> binding.viewpager.setCurrentItem(1, true)
            1 -> binding.viewpager.setCurrentItem(2, true)
            2 -> checkFieldsThenSave()
        }
    }

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
            productDetails[ApiFields.Keys.IMAGE_FRONT_UPLOADED] = "true"
        }
        if (imageIngredientsUploaded) {
            productDetails[ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED] = "true"
        }
        if (imageNutritionFactsUploaded) {
            productDetails[ApiFields.Keys.IMAGE_NUTRITION_UPLOADED] = "true"
        }
        val toSaveOfflineProduct = OfflineSavedProduct()
        toSaveOfflineProduct.barcode = productDetails["code"]
        toSaveOfflineProduct.setProductDetailsMap(productDetails)
        mOfflineSavedProductDao!!.insertOrReplace(toSaveOfflineProduct)
        scheduleSync()
        addToHistorySync(daoSession.historyProductDao, toSaveOfflineProduct)
        Toast.makeText(this, R.string.productSavedToast, Toast.LENGTH_SHORT).show()
        hideKeyboard(this)
        setResult(RESULT_OK)
        finish()
    }

    private fun checkFieldsThenSave() {
        if (editingMode) {
            // edit mode, therefore do not check whether front image is empty or not however do check the nutrition facts values.
            if (isNutritionDataAvailable() && productEditNutritionFactsFragment.containsInvalidValue()) {
                // If there are any invalid field and there is nutrition data, scroll to the nutrition fragment
                binding.viewpager.setCurrentItem(2, true)
            } else {
                saveProduct()
            }
        } else {
            // add mode, check if we have required fields
            if (productEditOverviewFragment.areRequiredFieldsEmpty()) {
                binding.viewpager.setCurrentItem(0, true)
            } else if (isNutritionDataAvailable() && productEditNutritionFactsFragment.containsInvalidValue()) {
                binding.viewpager.setCurrentItem(2, true)
            } else {
                saveProduct()
            }
        }
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
        val lang = productLanguageForEdition
        var ocr = false
        val imgMap: MutableMap<String, RequestBody?> = HashMap()
        imgMap["code"] = image.code
        val imageField = createTextPlain(image.imageField.toString() + '_' + lang)
        imgMap["imagefield"] = imageField
        if (image.imgFront != null) {
            imagesFilePath[0] = image.filePath
            imgMap["imgupload_front\"; filename=\"front_$lang.png\""] = image.imgFront
        }
        if (image.imgIngredients != null) {
            imgMap["imgupload_ingredients\"; filename=\"ingredients_$lang.png\""] = image.imgIngredients
            ocr = true
            imagesFilePath[1] = image.filePath
        }
        if (image.imgNutrition != null) {
            imgMap["imgupload_nutrition\"; filename=\"nutrition_$lang.png\""] = image.imgNutrition
            imagesFilePath[2] = image.filePath
        }
        if (image.imgPackaging != null) {
            imgMap["imgupload_packaging\"; filename=\"packaging_$lang.png\""] = image.imgPackaging
            imagesFilePath[3] = image.filePath
        }
        if (image.imgOther != null) {
            imgMap["imgupload_other\"; filename=\"other_$lang.png\""] = image.imgOther
        }
        // Attribute the upload to the connected user
        addLoginPasswordInfo(imgMap)
        savePhoto(imgMap, image, position, ocr)
    }

    private fun savePhoto(imgMap: Map<String, RequestBody?>, image: ProductImage, position: Int, ocr: Boolean) {
        disp.add(productsApi.saveImageSingle(imgMap)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showImageProgress(position) }
                .subscribe({ jsonNode: JsonNode ->
                    val status = jsonNode["status"].asText()
                    if (status == "status not ok") {
                        val error = jsonNode["error"].asText()
                        val alreadySent = error == "This picture has already been sent."
                        if (alreadySent && ocr) {
                            hideImageProgress(position, false, getString(R.string.image_uploaded_successfully))
                            performOCR(image.barcode, "ingredients_$productLanguageForEdition")
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
                        val imagefield = jsonNode["imagefield"].asText()
                        val imgid = jsonNode["image"]["imgid"].asText()
                        if (position != 3 && position != 4) {
                            // Not OTHER image
                            setPhoto(image, imagefield, imgid, ocr)
                        }
                    }
                }) { e ->
                    // A network error happened
                    if (e is IOException) {
                        hideImageProgress(position, false, getString(R.string.no_internet_connection))
                        Log.e(LOGGER_TAG, e.message!!)
                        if (image.imageField === ProductImageField.OTHER) {
                            val product = ToUploadProduct(image.barcode, image.filePath, image.imageField.toString())
                            mToUploadProductDao!!.insertOrReplace(product)
                        }
                    } else {
                        hideImageProgress(position, true, e.message)
                        Log.i(this.javaClass.simpleName, e.message!!)
                        Toast.makeText(OFFApplication.instance, e.message, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun setPhoto(image: ProductImage, imagefield: String, imgid: String, ocr: Boolean) {
        val queryMap: MutableMap<String, String> = HashMap()
        queryMap["imgid"] = imgid
        queryMap["id"] = imagefield
        disp.add(productsApi.editImageSingle(image.barcode, queryMap)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ jsonNode: JsonNode ->
                    val status = jsonNode["status"].asText()
                    if (ocr && status == "status ok") {
                        performOCR(image.barcode, imagefield)
                    }
                }) { throwable: Throwable ->
                    if (throwable is IOException) {
                        if (ocr) {
                            val view = findViewById<View>(R.id.coordinator_layout)
                            Snackbar.make(view, R.string.no_internet_unable_to_extract_ingredients, Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.txt_try_again) { setPhoto(image, imagefield, imgid, true) }.show()
                        }
                    } else {
                        Log.i(this.javaClass.simpleName, throwable.message!!)
                        Toast.makeText(OFFApplication.instance, throwable.message, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    fun performOCR(code: String?, imageField: String?) {
        disp.add(productsApi.getIngredients(code, imageField)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { productEditIngredientsFragment.showOCRProgress() }
                .subscribe({ jsonNode: JsonNode ->
                    productEditIngredientsFragment.hideOCRProgress()
                    val status = jsonNode["status"].toString()
                    if (status == "0") {
                        val ocrResult = jsonNode["ingredients_text_from_image"].asText()
                        productEditIngredientsFragment.setIngredients(status, ocrResult)
                    } else {
                        productEditIngredientsFragment.setIngredients(status, null)
                    }
                }) { throwable: Throwable ->
                    productEditIngredientsFragment.hideOCRProgress()
                    if (throwable is IOException) {
                        val view = findViewById<View>(R.id.coordinator_layout)
                        Snackbar.make(view, R.string.no_internet_unable_to_extract_ingredients, BaseTransientBottomBar.LENGTH_INDEFINITE)
                                .setAction(R.string.txt_try_again) { performOCR(code, imageField) }.show()
                    } else {
                        Log.e(this.javaClass.simpleName, throwable.message, throwable)
                        Toast.makeText(this@ProductEditActivity, throwable.message, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun hideImageProgress(position: Int, errorUploading: Boolean, message: String?) {
        when (position) {
            0 -> productEditOverviewFragment.hideImageProgress(errorUploading, message)
            1 -> productEditIngredientsFragment.hideImageProgress(errorUploading, message)
            2 -> productEditNutritionFactsFragment.hideImageProgress(errorUploading)
            3 -> productEditOverviewFragment.hideOtherImageProgress(errorUploading, message)
            4 -> addProductPhotosFragment.hideImageProgress(errorUploading, message)
        }
    }

    private fun showImageProgress(position: Int) {
        when (position) {
            0 -> productEditOverviewFragment.showImageProgress()
            1 -> productEditIngredientsFragment.showImageProgress()
            2 -> productEditNutritionFactsFragment.showImageProgress()
            3 -> productEditOverviewFragment.showOtherImageProgress()
            4 -> addProductPhotosFragment.showImageProgress()
        }
    }

    val productLanguageForEdition: String?
        get() = productDetails[ApiFields.Keys.LANG]

    fun setProductLanguage(languageCode: String) {
        productDetails[ApiFields.Keys.LANG] = languageCode
    }

    fun updateLanguage() {
        productEditIngredientsFragment.loadIngredientsImage()
        productEditNutritionFactsFragment.loadNutritionImage()
    }

    fun setIngredients(status: String?, ingredients: String?) {
        productEditIngredientsFragment.setIngredients(status, ingredients)
    }

    class EditProductPerformOCR : ActivityResultContract<Product?, Boolean>() {
        override fun createIntent(context: Context, product: Product?): Intent {
            val intent = Intent(context, ProductEditActivity::class.java)
            intent.putExtra(KEY_EDIT_PRODUCT, product)
            intent.putExtra(KEY_PERFORM_OCR, true)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == RESULT_OK
        }
    }

    class EditProductSendUpdatedImg : ActivityResultContract<Product?, Boolean>() {
        override fun createIntent(context: Context, product: Product?): Intent {
            val intent = Intent(context, ProductEditActivity::class.java)
            intent.putExtra(KEY_SEND_UPDATED, true)
            intent.putExtra(KEY_EDIT_PRODUCT, product)
            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == RESULT_OK
        }
    }

    companion object {
        const val KEY_PERFORM_OCR = "perform_ocr"
        const val KEY_SEND_UPDATED = "send_updated"
        private val LOGGER_TAG = ProductEditActivity::class.java.simpleName
        const val MODIFY_NUTRITION_PROMPT = "modify_nutrition_prompt"
        const val MODIFY_CATEGORY_PROMPT = "modify_category_prompt"
        const val KEY_EDIT_PRODUCT = "edit_product"
        const val KEY_IS_EDITING = "is_edition"
        const val KEY_EDIT_OFFLINE_PRODUCT = "edit_offline_product"
        const val KEY_STATE = "state"
        private fun getCameraPicLocation(context: Context): File {
            var cacheDir = context.cacheDir
            if (isExternalStorageWritable) {
                cacheDir = context.externalCacheDir
            }
            val dir = File(cacheDir, "EasyImage")
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    Log.i(LOGGER_TAG, "Directory created")
                } else {
                    Log.i(LOGGER_TAG, "Couldn't create directory")
                }
            }
            return dir
        }

        private fun clearCameraCachedPics(context: Context) {
            val files = getCameraPicLocation(context).listFiles() ?: return
            for (file in files) {
                if (file.delete()) {
                    Log.i(LOGGER_TAG, "Deleted cached photo")
                } else {
                    Log.i(LOGGER_TAG, "Couldn't delete cached photo")
                }
            }
        }

        private fun updateTimeLine(stage: Int, view: View) {
            when (stage) {
                0 -> view.setBackgroundResource(R.drawable.stage_inactive)
                1 -> view.setBackgroundResource(R.drawable.stage_active)
                2 -> view.setBackgroundResource(R.drawable.stage_complete)
            }
        }

        @JvmOverloads
        fun start(context: Context, state: ProductState?, sendUpdated: Boolean = false, performOcr: Boolean = false) {
            val starter = Intent(context, ProductEditActivity::class.java).apply {
                putExtra(KEY_STATE, state)
            }
            if (sendUpdated) {
                starter.putExtra(KEY_SEND_UPDATED, true)
            }
            if (performOcr) {
                starter.putExtra(KEY_PERFORM_OCR, true)
            }
            context.startActivity(starter)
        }

        private fun isNutritionDataAvailable() = isFlavors(AppFlavors.OFF, AppFlavors.OPFF)
    }
}