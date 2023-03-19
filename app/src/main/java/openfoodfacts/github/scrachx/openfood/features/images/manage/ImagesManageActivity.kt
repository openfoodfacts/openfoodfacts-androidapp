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
package openfoodfacts.github.scrachx.openfood.features.images.manage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toFile
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageActivity
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityFullScreenImageBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.LanguageDataAdapter
import openfoodfacts.github.scrachx.openfood.features.images.select.ImagesSelectActivity
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.IMAGE_STRING_ID
import openfoodfacts.github.scrachx.openfood.images.IMAGE_TYPE
import openfoodfacts.github.scrachx.openfood.images.IMAGE_URL
import openfoodfacts.github.scrachx.openfood.images.IMG_ID
import openfoodfacts.github.scrachx.openfood.images.ImageSize
import openfoodfacts.github.scrachx.openfood.images.ImageTransformation
import openfoodfacts.github.scrachx.openfood.images.LANGUAGE
import openfoodfacts.github.scrachx.openfood.images.PRODUCT
import openfoodfacts.github.scrachx.openfood.images.PRODUCT_BARCODE
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.images.getImageStringKey
import openfoodfacts.github.scrachx.openfood.images.getInitialServerTransformation
import openfoodfacts.github.scrachx.openfood.images.getLanguageCodeFromUrl
import openfoodfacts.github.scrachx.openfood.images.getResourceId
import openfoodfacts.github.scrachx.openfood.images.getResourceIdForEditAction
import openfoodfacts.github.scrachx.openfood.images.getScreenTransformation
import openfoodfacts.github.scrachx.openfood.images.toServerTransformation
import openfoodfacts.github.scrachx.openfood.models.Barcode
import openfoodfacts.github.scrachx.openfood.models.LanguageData
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.findByCode
import openfoodfacts.github.scrachx.openfood.models.getSelectedImageUrl
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.LocaleUtils
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import openfoodfacts.github.scrachx.openfood.utils.SupportedLanguages
import openfoodfacts.github.scrachx.openfood.utils.SwipeDetector
import openfoodfacts.github.scrachx.openfood.utils.SwipeDetector.OnSwipeEventListener
import openfoodfacts.github.scrachx.openfood.utils.SwipeDetector.SwipeTypeEnum
import openfoodfacts.github.scrachx.openfood.utils.getAppPreferences
import openfoodfacts.github.scrachx.openfood.utils.isAbsoluteUrl
import openfoodfacts.github.scrachx.openfood.utils.isUserSet
import org.apache.commons.lang3.StringUtils
import pl.aprilapps.easyphotopicker.EasyImage
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import java.io.File
import javax.inject.Inject

/**
 * Activity to display/edit product images
 */
@AndroidEntryPoint
class ImagesManageActivity : BaseActivity() {
    private var _binding: ActivityFullScreenImageBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var client: ProductRepository

    @Inject
    lateinit var fileDownloader: FileDownloader

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var localeManager: LocaleManager

    @Inject
    lateinit var photoReceiverHandler: PhotoReceiverHandler

    private var lastViewedImage: File? = null
    private lateinit var attacher: PhotoViewAttacher
    private val settings by lazy { getAppPreferences() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Setup onclick listeners
        binding.btnDone.setOnClickListener { exit() }
        binding.btnUnselectImage.setOnClickListener { unSelectImage() }
        binding.btnChooseImage.setOnClickListener { selectImage() }
        binding.btnAddImage.setOnClickListener { addImage() }
        binding.btnChooseDefaultLanguage.setOnClickListener { selectDefaultLanguage() }
        binding.btnEditImage.setOnClickListener { onStartEditExistingImage() }

        binding.comboLanguages.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                onLanguageChanged()

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit // Do nothing
        }

        binding.comboImageType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                onImageTypeChanged()

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit // Do nothing
        }
        settings.let {
            if (it.getBoolean(getString(R.string.check_first_time), true)) {
                startShowCase(getString(R.string.title_image_type),
                    getString(R.string.content_image_type),
                    R.id.comboImageType,
                    1)
            }
        }

        val product = intent.getSerializableExtra(PRODUCT) as Product?
        binding.btnEditImage.visibility = if (product != null) View.VISIBLE else View.INVISIBLE
        binding.btnUnselectImage.visibility = binding.btnEditImage.visibility
        attacher = PhotoViewAttacher(binding.imageViewFullScreen)

        // Delaying the transition until the view has been laid out
        ActivityCompat.postponeEnterTransition(this)

        SwipeDetector(binding.imageViewFullScreen, object : OnSwipeEventListener {
            override fun onSwipeEventDetected(v: View?, swipeType: SwipeTypeEnum?) = when (swipeType) {
                SwipeTypeEnum.LEFT_TO_RIGHT -> incrementImageType(-1)
                SwipeTypeEnum.RIGHT_TO_LEFT -> incrementImageType(1)
                SwipeTypeEnum.TOP_TO_BOTTOM -> onRefresh(true)
                else -> stopRefresh()
            }
        })
        binding.comboImageType.adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item_white,
            generateImageTypeNames()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_list_item_single_choice)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        product?.let { loadLanguage(it) }
        binding.comboImageType.setSelection(ApiFields.Keys.TYPE_IMAGE.indexOf(getSelectedType()))
        updateProductImagesInfo()
        onRefresh(false)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun startShowCase(title: String, content: String, viewId: Int, type: Int) {
        GuideView.Builder(this)
            .setTitle(title)
            .setContentText(content)
            .setTargetView(findViewById(viewId))
            .setContentTextSize(12)
            .setTitleTextSize(16)
            .setDismissType(GuideView.DismissType.outside)
            .setGuideListener {
                when (type) {
                    1 -> startShowCase(getString(R.string.title_choose_language),
                        getString(R.string.content_choose_language),
                        R.id.comboLanguages,
                        2)
                    2 -> startShowCase(getString(R.string.title_add_photo),
                        getString(R.string.content_add_photo),
                        R.id.btnAddImage,
                        3)
                    3 -> startShowCase(getString(R.string.title_choose_photo),
                        getString(R.string.content_choose_photo),
                        R.id.btnChooseImage,
                        4)
                    4 -> startShowCase(getString(R.string.title_edit_photo),
                        getString(R.string.content_edit_photo),
                        R.id.btnEditImage,
                        5)
                    5 -> startShowCase(getString(R.string.title_unselect_photo),
                        getString(R.string.content_unselect_photo),
                        R.id.btnUnselectImage,
                        6)
                    6 -> startShowCase(getString(R.string.title_exit),
                        getString(R.string.content_exit),
                        R.id.btn_done,
                        7)
                    7 -> settings.edit { putBoolean(getString(R.string.check_first_time), false) }
                }
            }
            .build()
            .show()
    }

    private fun generateImageTypeNames() =
        ApiFields.Keys.TYPE_IMAGE.map { resources.getString(getResourceId(it)) }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun incrementImageType(inc: Int) {
        stopRefresh()
        var newPosition = binding.comboImageType.selectedItemPosition + inc
        val count = binding.comboImageType.adapter.count
        newPosition = if (newPosition < 0) {
            count - 1
        } else {
            newPosition % count
        }
        binding.comboImageType.setSelection(newPosition, true)
    }

    private fun loadLanguage(product: Product) {
        // We load all available languages for product/type
        val currentLanguage = getCurrentLanguage()
        val productImageField = getSelectedType()

        val addedLanguages = product.getAvailableLanguageForImage(productImageField, ImageSize.DISPLAY).toMutableSet()
        val languageForImage = LocaleUtils.getLanguageData(addedLanguages, true).toMutableList()
        val selectedIndex = languageForImage.findByCode(currentLanguage)
        if (selectedIndex < 0) {
            addedLanguages.add(currentLanguage)
            languageForImage.add(LocaleUtils.getLanguageData(currentLanguage, false))
        }

        val otherNotSupportedCode = SupportedLanguages.codes().filter { it !in addedLanguages }

        languageForImage.addAll(LocaleUtils.getLanguageData(otherNotSupportedCode, false))
        val adapter = LanguageDataAdapter(this, R.layout.simple_spinner_item_white, languageForImage)
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice)
        binding.comboLanguages.adapter = adapter

        val i = languageForImage.indexOfFirst { it.code == currentLanguage }
        if (i >= 0) binding.comboLanguages.setSelection(i)

        updateLanguageStatus()
        updateSelectDefaultLanguageAction()
    }

    /**
     * Use to warn the user that there is no image for the selected image.
     */
    private fun updateLanguageStatus(): Boolean {
        val serializableExtra = getSelectedType()
        val imageUrl = getCurrentImageUrl()
        val imgLangCode = getLanguageCodeFromUrl(serializableExtra, imageUrl)
        val appLangCode = getCurrentLanguage()

        // If the language of the displayed image is not the same that the language in this activity
        // we use the language of the image
        val isLanguageSupported = appLangCode == imgLangCode
        if (isLanguageSupported) {
            binding.textInfo.text = null
            binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.textInfo.setText(R.string.image_not_defined_for_language)
            binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.orange))
        }
        binding.btnEditImage.visibility = if (isLanguageSupported) View.VISIBLE else View.GONE
        binding.btnUnselectImage.visibility = binding.btnEditImage.visibility
        return isLanguageSupported
    }

    private fun getCurrentLanguage(): String = intent.getStringExtra(LANGUAGE) ?: localeManager.getLanguage()

    private fun updateToolbarTitle(product: Product?) {
        product?.let {
            binding.toolbar.title =
                "${it.getProductName(localeManager.getLanguage()).orEmpty()} / ${binding.comboImageType.selectedItem}"
        }
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(getProduct())
    }

    private fun onRefresh(reloadProduct: Boolean) {
        val imageUrl = getCurrentImageUrl()
        if (reloadProduct || imageUrl == null) reloadProduct()
        else loadImage(imageUrl)
    }

    private fun loadImage(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            binding.imageViewFullScreen.setImageDrawable(null)
            stopRefresh()
        } else {
            var url = imageUrl
            if (isAbsoluteUrl(url)) {
                url = "file://$url"
            }
            startRefresh(getString(R.string.txtLoading))
            picasso
                .load(url)
                .into(binding.imageViewFullScreen, object : Callback {
                    override fun onSuccess() {
                        if (_binding == null) return
                        attacher.update()
                        scheduleStartPostponedTransition(binding.imageViewFullScreen)
                        binding.imageViewFullScreen.visibility = View.VISIBLE
                        stopRefresh()
                    }

                    override fun onError(ex: Exception) {
                        if (_binding == null) return
                        binding.imageViewFullScreen.visibility = View.VISIBLE
                        Toast.makeText(this@ImagesManageActivity,
                            resources.getString(R.string.txtConnectionError),
                            Toast.LENGTH_LONG).show()
                        stopRefresh()
                    }
                })
        }
    }

    /**
     * Reloads product images from the server. Updates images and the language.
     */
    private fun reloadProduct() {
        if (isFinishing) return

        getProduct()?.let {
            startRefresh(getString(R.string.loading_product, "${it.getProductName(localeManager.getLanguage())}..."))

            lifecycleScope.launch {
                val barcode = Barcode(it.code)
                val newState = client.getProductImages(barcode)
                val newProduct = newState.product
                var imageReloaded = false

                if (newProduct != null) {
                    updateToolbarTitle(newProduct)

                    val imgUrl = getCurrentImageUrl()
                    intent.putExtra(PRODUCT, newProduct)

                    val newImgUrl = getImageUrlToDisplay(newProduct)
                    loadLanguage(newProduct)
                    if (imgUrl == null || imgUrl != newImgUrl) {
                        intent.putExtra(IMAGE_URL, newImgUrl)
                        loadImage(newImgUrl)
                        imageReloaded = true
                    }
                } else if (!newState.statusVerbose.isNullOrBlank()) {
                    Toast.makeText(this@ImagesManageActivity, newState.statusVerbose, Toast.LENGTH_LONG).show()
                }

                if (!imageReloaded) stopRefresh()
            }

        }
    }

    /**
     * The additional field "images" is not loaded by default by OFF as it's only used to edit an image.
     * So we load the product images in background.
     * Could be improved by loading only the field "images".
     */
    private fun updateProductImagesInfo(toDoAfter: () -> Unit = {}) {
        val product = getProduct() ?: return

        val barcode = Barcode(product.code)
        lifecycleScope.launchWhenCreated {
            val newState = client.getProductImages(barcode)
            newState.product?.let { intent.putExtra(PRODUCT, it) }
            toDoAfter()
        }
    }

    private fun getImageUrlToDisplay(product: Product) =
        product.getSelectedImageUrl(getCurrentLanguage(), getSelectedType(), ImageSize.DISPLAY)

    private fun getCurrentImageUrl() = intent.getStringExtra(IMAGE_URL)

    /**
     * @see .startRefresh
     */
    private fun stopRefresh() {
        binding.progressBar.visibility = View.GONE
        updateLanguageStatus()
    }

    private fun isRefreshing() = binding.progressBar.visibility == View.VISIBLE

    /**
     * @param text
     * @see .stopRefresh
     */
    private fun startRefresh(text: String?) {
        binding.progressBar.visibility = View.VISIBLE
        if (text != null) {
            binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.textInfo.text = text
        }
    }

    private fun selectDefaultLanguage() {
        val lang = LocaleUtils.parseLocale(getProduct()!!.lang).language
        val position = (binding.comboLanguages.adapter as LanguageDataAdapter).getPosition(lang)
        if (position >= 0) {
            binding.comboLanguages.setSelection(position, true)
        }
    }

    private fun exit() {
        setResult(RESULT_OK)
        finish()
    }

    private fun unSelectImage() {
        if (cannotEdit(REQUEST_UNSELECT_IMAGE_AFTER_LOGIN)) return
        startRefresh(getString(R.string.unselect_image))
        lifecycleScope.launch {
            try {
                client.unSelectImage(getProduct()!!.code, getSelectedType(), getCurrentLanguage())
            } catch (err: Exception) {
                reloadProduct()
            }
            setResult(RESULTCODE_MODIFIED)
            reloadProduct()
        }
    }

    private fun selectImage() {
        if (cannotEdit(REQUEST_CHOOSE_IMAGE_AFTER_LOGIN)) return
        selectImageLauncher.launch(getProduct()!!.code)
    }

    /**
     * Check if user is able to edit or not.
     *
     * @param loginRequestCode request code to pass to [.startActivityForResult].
     * @return true if user **cannot edit**, false otherwise.
     */
    private fun cannotEdit(loginRequestCode: Int): Boolean {
        if (isRefreshing()) {
            Toast.makeText(this, R.string.cant_modify_if_refreshing, LENGTH_SHORT).show()
            return true
        }
        //if user not logged in, we force to log
        if (!isUserSet()) {
            startActivityForResult(Intent(this, LoginActivity::class.java), loginRequestCode)
            return true
        }
        return false
    }

    private val requestCameraLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    { isGranted -> if (isGranted) addImage() }

    private fun addImage() {
        if (cannotEdit(REQUEST_ADD_IMAGE_AFTER_LOGIN)) return

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraLauncher.launch(Manifest.permission.CAMERA)
        } else {
            EasyImage.openCamera(this, 0)
        }
    }

    private fun updateSelectDefaultLanguageAction() {
        val isDefault =
            getProduct()?.lang != null && getCurrentLanguage() == LocaleUtils.parseLocale(getProduct()!!.lang).language
        binding.btnChooseDefaultLanguage.visibility = if (isDefault) View.INVISIBLE else View.VISIBLE
    }

    private fun onStartEditExistingImage() {
        if (cannotEdit(REQUEST_EDIT_IMAGE_AFTER_LOGIN)) return

        val productImageField = getSelectedType()
        val language = getCurrentLanguage()
        val product = this.getProduct()!!

        // The rotation/crop set on the server
        val transformation = getScreenTransformation(product, productImageField, language)

        // The first time, the images properties are not loaded...
        if (transformation.isUrlEmpty()) {
            updateProductImagesInfo {
                editPhoto(productImageField,
                    getScreenTransformation(product, productImageField, language))
            }
        }
        editPhoto(productImageField, transformation)
    }

    private fun editPhoto(field: ProductImageField, transformation: ImageTransformation) {
        val url = transformation.imageUrl?.takeIf { it.isNotBlank() } ?: return

        lifecycleScope.launchWhenResumed {
            val fileUri = fileDownloader.download(url)
            if (fileUri != null) {
                //to delete the file after:
                lastViewedImage = fileUri.toFile()
                cropRotateExistingImageOnServer(
                    fileUri,
                    getString(getResourceIdForEditAction(field)),
                    transformation
                )
            }
        }
    }

    private fun getProduct() = intent.getSerializableExtra(PRODUCT) as Product?

    private fun requireProduct() = getProduct() ?: error("Cannot start $LOG_TAG without product.")

    private fun getSelectedType(): ProductImageField = intent.getSerializableExtra(IMAGE_TYPE) as ProductImageField?
        ?: error("Cannot initialize $LOG_TAG without IMAGE_TYPE")

    private fun onLanguageChanged() {
        val data = binding.comboLanguages.selectedItem as LanguageData
        val product = requireProduct()
        if (data.code != getCurrentLanguage()) {
            intent.putExtra(LANGUAGE, data.code)
            intent.putExtra(IMAGE_URL, getImageUrlToDisplay(product))
            updateToolbarTitle(product)
            onRefresh(false)
        }
        updateSelectDefaultLanguageAction()
    }

    private fun onImageTypeChanged() {
        getProduct()?.let {
            val newTypeSelected = ApiFields.Keys.TYPE_IMAGE[binding.comboImageType.selectedItemPosition]
            val selectedType = getSelectedType()
            if (newTypeSelected == selectedType) return

            intent.putExtra(IMAGE_TYPE, newTypeSelected)
            intent.putExtra(IMAGE_URL, getImageUrlToDisplay(it))

            onRefresh(false)
            loadLanguage(it)
            updateToolbarTitle(it)
        }
    }

    private fun cropRotateExistingImageOnServer(fileUri: Uri, title: String, transformation: ImageTransformation) {

        val activityBuilder = CropImage.activity(fileUri)
            .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
            .setAllowFlipping(false) //we just want crop size/rotation
            .setNoOutputImage(true)
            .setAllowRotation(true)
            .setAllowCounterRotation(true)
            .setAutoZoomEnabled(false)
            .setInitialRotation(transformation.rotationInDegree)
            .setActivityTitle(title)

        if (transformation.cropRectangle != null) {
            activityBuilder.setInitialCropWindowRectangle(transformation.cropRectangle)
        } else {
            activityBuilder.setInitialCropWindowPaddingRatio(0f)
        }
        startActivityForResult(activityBuilder.getIntent(this, CropImageActivity::class.java), REQUEST_EDIT_IMAGE)
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val isResultOk = resultCode == RESULT_OK
        when (requestCode) {
            REQUEST_EDIT_IMAGE -> applyEditExistingImage(isResultOk, data)
            REQUEST_EDIT_IMAGE_AFTER_LOGIN -> if (isResultOk) onStartEditExistingImage()
            REQUEST_ADD_IMAGE_AFTER_LOGIN -> if (isResultOk) addImage()
            REQUEST_CHOOSE_IMAGE_AFTER_LOGIN -> if (isResultOk) selectImage()
            REQUEST_UNSELECT_IMAGE_AFTER_LOGIN -> if (isResultOk) unSelectImage()
            else -> photoReceiverHandler
                .onActivityResult(this, requestCode, resultCode, data) { onPhotoReturned(it) }
        }
    }

    private val selectImageLauncher = registerForActivityResult(ImagesSelectActivity.Companion.SelectImageContract(""))
    { (imgId, file) ->
        // Photo chosen from gallery
        if (file != null) {
            onPhotoReturned(file)
        } else if (!imgId.isNullOrBlank()) {
            postEditImage(mutableMapOf(IMG_ID to imgId))
        }
    }

    /**
     * @param isResultOk should
     * @param dataFromCropActivity from the crop activity. If not, action is ignored
     */
    private fun applyEditExistingImage(isResultOk: Boolean, dataFromCropActivity: Intent?) {
        // Delete downloaded local file
        deleteLocalFiles()

        // if the selected language is not the same than current image we can't modify: only add
        if (!isUserSet() || !updateLanguageStatus() || dataFromCropActivity == null) return

        if (isResultOk) {
            startRefresh(StringUtils.EMPTY)
            val result = CropImage.getActivityResult(dataFromCropActivity)!!
            val product = requireProduct()
            val currentServerTransformation =
                getInitialServerTransformation(product, getSelectedType(), getCurrentLanguage())
            val newServerTransformation =
                toServerTransformation(ImageTransformation(result.rotation, result.cropRect),
                    product,
                    getSelectedType(),
                    getCurrentLanguage())
            val isModified = currentServerTransformation != newServerTransformation
            if (isModified) {
                startRefresh(getString(R.string.toastSending))
                val imgMap = newServerTransformation.toMap() + mapOf(IMG_ID to newServerTransformation.imageId!!)
                postEditImage(imgMap)
            } else {
                stopRefresh()
            }
        }
    }

    private fun postEditImage(imgMap: Map<String, String>) {
        val code = requireProduct().code
        val map = imgMap.toMutableMap().apply {
            put(PRODUCT_BARCODE, code)
            put(IMAGE_STRING_ID, getImageStringKey(getSelectedType(), getCurrentLanguage()))
        }
        binding.imageViewFullScreen.visibility = View.INVISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            client.editImage(code, map)
            setResult(RESULTCODE_MODIFIED)
            withContext(Dispatchers.Main) { reloadProduct() }
        }
    }

    private fun deleteLocalFiles() {
        lastViewedImage?.let {
            if (!it.delete()) {
                Log.w(ImagesManageActivity::class.simpleName, "Cannot delete file ${lastViewedImage!!.absolutePath}.")
            } else {
                lastViewedImage = null
            }
        }
    }

    /**
     * For scheduling a postponed transition after the proper measures of the view are done
     * and the view has been properly laid out in the View hierarchy
     */
    private fun scheduleStartPostponedTransition(sharedElement: View) {
        sharedElement.viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                sharedElement.viewTreeObserver.removeOnPreDrawListener(this)
                ActivityCompat.startPostponedEnterTransition(this@ImagesManageActivity)
                return true
            }
        })
    }

    /**
     * @param newPhotoFile photo selected by the user to be sent to the server.
     */
    private fun onPhotoReturned(newPhotoFile: File) {
        startRefresh(getString(R.string.uploading_image))
        val image = ProductImage(
            requireProduct().barcode,
            getSelectedType(),
            getCurrentLanguage(),
            newPhotoFile.readBytes(),
            newPhotoFile.absolutePath
        )
        // Send image
        lifecycleScope.launchWhenCreated {
            try {
                client.postImg(image, true)
            } catch (err: Exception) {
                Toast.makeText(this@ImagesManageActivity, err.message, Toast.LENGTH_LONG).show()
                Log.e(ImagesManageActivity::class.simpleName, err.message, err)
                stopRefresh()
            }
            reloadProduct()
            setResult(RESULTCODE_MODIFIED)
        }
    }

    companion object {
        private const val RESULTCODE_MODIFIED = 1
        private const val REQUEST_EDIT_IMAGE_AFTER_LOGIN = 1
        private const val REQUEST_ADD_IMAGE_AFTER_LOGIN = 2
        private const val REQUEST_CHOOSE_IMAGE_AFTER_LOGIN = 3
        private const val REQUEST_UNSELECT_IMAGE_AFTER_LOGIN = 4
        const val REQUEST_EDIT_IMAGE = 1000

        fun isImageModified(requestCode: Int, resultCode: Int) =
            requestCode == REQUEST_EDIT_IMAGE && resultCode == RESULTCODE_MODIFIED

        private val LOG_TAG = ImagesManageActivity::class.simpleName
    }
}
