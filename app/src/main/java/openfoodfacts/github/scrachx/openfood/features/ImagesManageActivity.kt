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
package openfoodfacts.github.scrachx.openfood.features

import android.Manifest.permission
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.squareup.picasso.Callback
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityFullScreenImageBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.LanguageDataAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.*
import openfoodfacts.github.scrachx.openfood.images.ImageTransformationUtils.Companion.addTransformToMap
import openfoodfacts.github.scrachx.openfood.images.ImageTransformationUtils.Companion.getInitialServerTransformation
import openfoodfacts.github.scrachx.openfood.images.ImageTransformationUtils.Companion.getScreenTransformation
import openfoodfacts.github.scrachx.openfood.images.ImageTransformationUtils.Companion.toServerTransformation
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader.download
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.LanguageData
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.find
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguageData
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLocale
import openfoodfacts.github.scrachx.openfood.utils.SwipeDetector.OnSwipeEventListener
import openfoodfacts.github.scrachx.openfood.utils.SwipeDetector.SwipeTypeEnum
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import org.apache.commons.lang3.StringUtils
import pl.aprilapps.easyphotopicker.EasyImage
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import java.io.File
import java.util.*

/**
 * Activity to display/edit product images
 */
class ImagesManageActivity : BaseActivity() {
    private var _binding: ActivityFullScreenImageBinding? = null
    private val binding get() = _binding!!

    private lateinit var client: OpenFoodAPIClient

    private val disp = CompositeDisposable()

    private var lastViewedImage: File? = null
    private var attacher: PhotoViewAttacher? = null
    private val settings by lazy { getSharedPreferences("prefs", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = OpenFoodAPIClient(this)
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) =
                    onLanguageChanged()

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit // Do nothing
        }

        binding.comboImageType.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) =
                    onImageTypeChanged()

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit // Do nothing
        }
        settings.let {
            if (it.getBoolean(getString(R.string.check_first_time), true)) {
                startShowCase(getString(R.string.title_image_type), getString(R.string.content_image_type), R.id.comboImageType, 1)
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
        disp.dispose()
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
                        1 -> startShowCase(getString(R.string.title_choose_language), getString(R.string.content_choose_language), R.id.comboLanguages, 2)
                        2 -> startShowCase(getString(R.string.title_add_photo), getString(R.string.content_add_photo), R.id.btnAddImage, 3)
                        3 -> startShowCase(getString(R.string.title_choose_photo), getString(R.string.content_choose_photo), R.id.btnChooseImage, 4)
                        4 -> startShowCase(getString(R.string.title_edit_photo), getString(R.string.content_edit_photo), R.id.btnEditImage, 5)
                        5 -> startShowCase(getString(R.string.title_unselect_photo), getString(R.string.content_unselect_photo), R.id.btnUnselectImage, 6)
                        6 -> startShowCase(getString(R.string.title_exit), getString(R.string.content_exit), R.id.btn_done, 7)
                        7 -> settings!!.edit { putBoolean(getString(R.string.check_first_time), false) }
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
        val languageForImage = getLanguageData(addedLanguages, true)
        var selectedIndex = languageForImage.find(currentLanguage)
        if (selectedIndex < 0) {
            addedLanguages.add(currentLanguage)
            languageForImage.add(getLanguageData(currentLanguage, false))
        }
        val localeValues = resources.getStringArray(R.array.languages_array)
        val otherNotSupportedCode = localeValues.filter { it !in addedLanguages }

        languageForImage.addAll(getLanguageData(otherNotSupportedCode, false))
        val adapter = LanguageDataAdapter(this, R.layout.simple_spinner_item_white, languageForImage)
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice)
        binding.comboLanguages.adapter = adapter
        selectedIndex = languageForImage.find(currentLanguage)
        if (selectedIndex >= 0) binding.comboLanguages.setSelection(selectedIndex)
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

    private fun getCurrentLanguage() = intent.getStringExtra(LANGUAGE) ?: getLanguage(baseContext)

    private fun updateToolbarTitle(product: Product?) {
        product?.let {
            binding.toolbar.title = "${it.getLocalProductName(this).orEmpty()} / ${binding.comboImageType.selectedItem}"
        }
    }

    override fun onResume() {
        super.onResume()
        updateToolbarTitle(getProduct())
    }

    private fun onRefresh(reloadProduct: Boolean) {
        val imageUrl = getCurrentImageUrl()
        if (reloadProduct || imageUrl == null) {
            reloadProduct()
        } else {
            loadImage(imageUrl)
        }
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
            picassoBuilder(this)
                    .load(url)
                    .into(binding.imageViewFullScreen, object : Callback {
                        override fun onSuccess() {
                            attacher!!.update()
                            scheduleStartPostponedTransition(binding.imageViewFullScreen)
                            binding.imageViewFullScreen.visibility = View.VISIBLE
                            stopRefresh()
                        }

                        override fun onError(ex: Exception) {
                            binding.imageViewFullScreen.visibility = View.VISIBLE
                            Toast.makeText(this@ImagesManageActivity, resources.getString(R.string.txtConnectionError), Toast.LENGTH_LONG).show()
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
            startRefresh(getString(R.string.loading_product, "${it.getLocalProductName(this)}..."))
            client.getProductImages(it.code).observeOn(AndroidSchedulers.mainThread()).subscribe { newState ->
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
                } else {
                    if (!newState.statusVerbose.isNullOrBlank()) {
                        Toast.makeText(this@ImagesManageActivity, newState.statusVerbose, Toast.LENGTH_LONG).show()
                    }
                }
                if (!imageReloaded) {
                    stopRefresh()
                }
            }.addTo(disp)
        }
    }

    /**
     * The additional field "images" is not loaded by default by OFF as it's only used to edit an image.
     * So we load the product images in background.
     * Could be improved by loading only the field "images".
     */
    private fun updateProductImagesInfo(toDoAfter: () -> Unit = {}) {
        getProduct()?.let { product ->
            client.getProductImages(product.code).observeOn(AndroidSchedulers.mainThread()).subscribe { newState ->
                newState.product?.let { intent.putExtra(PRODUCT, it) }
                toDoAfter()
            }.addTo(disp)
        }
    }

    private fun getImageUrlToDisplay(product: Product) =
            product.getSelectedImage(getCurrentLanguage(), getSelectedType(), ImageSize.DISPLAY)

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
        val lang = getLocale(getProduct()!!.lang).language
        getLanguageData(lang, true)
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
        client.unSelectImage(getProduct()!!.code, getSelectedType(), getCurrentLanguage())
                .doOnError { reloadProduct() }
                .subscribe { _ ->
                    setResult(RESULTCODE_MODIFIED)
                    reloadProduct()
                }.addTo(disp)
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
            Toast.makeText(this, R.string.cant_modify_if_refreshing, Toast.LENGTH_SHORT).show()
            return true
        }
        //if user not logged in, we force to log
        if (!isUserSet()) {
            startActivityForResult(Intent(this, LoginActivity::class.java), loginRequestCode)
            return true
        }
        return false
    }

    private fun addImage() {
        if (cannotEdit(REQUEST_ADD_IMAGE_AFTER_LOGIN)) return

        if (ContextCompat.checkSelfPermission(this, permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
        } else {
            EasyImage.openCamera(this, 0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA && isAllGranted(grantResults)) {
            addImage()
        }
    }

    private fun updateSelectDefaultLanguageAction() {
        val isDefault = getProduct()?.lang != null && getCurrentLanguage() == getLocale(getProduct()!!.lang).language
        binding.btnChooseDefaultLanguage.visibility = if (isDefault) View.INVISIBLE else View.VISIBLE
    }

    private fun onStartEditExistingImage() {
        if (cannotEdit(REQUEST_EDIT_IMAGE_AFTER_LOGIN)) {
            return
        }
        val productImageField = getSelectedType()
        val language = getCurrentLanguage()
        val product = this.getProduct()!!
        // The rotation/crop set on the server
        val transformation = getScreenTransformation(product, productImageField, language)
        // The first time, the images properties are not loaded...
        if (transformation.isEmpty()) {
            updateProductImagesInfo { editPhoto(productImageField, getScreenTransformation(product, productImageField, language)) }
        }
        editPhoto(productImageField, transformation)
    }

    private fun editPhoto(productImageField: ProductImageField?, transformation: ImageTransformationUtils) {
        if (transformation.isNotEmpty()) {
            download(this, transformation.imageUrl!!)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { file: File? ->
                        //to delete the file after:
                        lastViewedImage = file
                        cropRotateExistingImageOnServer(file, getString(getResourceIdForEditAction(productImageField!!)), transformation)
                    }.addTo(disp)
        }
    }

    private fun getProduct() = intent.getSerializableExtra(PRODUCT) as Product?

    private fun onLanguageChanged() {
        val data = binding.comboLanguages.selectedItem as LanguageData
        val product = getProduct()
        if (data.code != getCurrentLanguage()) {
            intent.putExtra(LANGUAGE, data.code)
            intent.putExtra(IMAGE_URL, getImageUrlToDisplay(product!!))
            updateToolbarTitle(product)
            onRefresh(false)
        }
        updateSelectDefaultLanguageAction()
    }

    private fun getSelectedType(): ProductImageField = intent.getSerializableExtra(IMAGE_TYPE) as ProductImageField?
            ?: error("Cannot initialize ${this::class.simpleName} without IMAGE_TYPE")

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

    private fun cropRotateExistingImageOnServer(image: File?, title: String, transformation: ImageTransformationUtils) {
        val uri = Uri.fromFile(image)
        val activityBuilder = CropImage.activity(uri)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_EDIT_IMAGE -> applyEditExistingImage(resultCode, data)
            REQUEST_EDIT_IMAGE_AFTER_LOGIN -> if (resultCode == RESULT_OK) onStartEditExistingImage()
            REQUEST_ADD_IMAGE_AFTER_LOGIN -> if (resultCode == RESULT_OK) addImage()
            REQUEST_CHOOSE_IMAGE_AFTER_LOGIN -> if (resultCode == RESULT_OK) selectImage()
            REQUEST_UNSELECT_IMAGE_AFTER_LOGIN -> if (resultCode == RESULT_OK) unSelectImage()
            else -> PhotoReceiverHandler { onPhotoReturned(it) }
                    .onActivityResult(this, requestCode, resultCode, data)
        }
    }

    private val selectImageLauncher = registerForActivityResult(ImagesSelectActivity.Companion.SelectImageContract(""))
    { (imgId, file) ->
        //photo choosed from gallery
        if (file != null) {
            onPhotoReturned(file)
        } else if (!imgId.isNullOrBlank()) {
            postEditImage(mutableMapOf(IMG_ID to imgId))
        }
    }

    /**
     * @param resultCode should
     * @param dataFromCropActivity from the crop activity. If not, action is ignored
     */
    private fun applyEditExistingImage(resultCode: Int, dataFromCropActivity: Intent?) {
        // Delete downloaded local file
        deleteLocalFiles()
        // if the selected language is not the same than current image we can't modify: only add
        if (!isUserSet() || !updateLanguageStatus() || dataFromCropActivity == null) {
            return
        }
        if (resultCode == RESULT_OK) {
            startRefresh(StringUtils.EMPTY)
            val result = CropImage.getActivityResult(dataFromCropActivity)
            val product = getProduct()
            val currentServerTransformation = getInitialServerTransformation(product!!, getSelectedType(), getCurrentLanguage())
            val newServerTransformation = toServerTransformation(ImageTransformationUtils(result.rotation, result.cropRect), product, getSelectedType(), getCurrentLanguage())
            val isModified = currentServerTransformation != newServerTransformation
            if (isModified) {
                startRefresh(getString(R.string.toastSending))
                val imgMap = mutableMapOf(IMG_ID to newServerTransformation.imageId!!)
                addTransformToMap(newServerTransformation, imgMap)
                postEditImage(imgMap)
            } else {
                stopRefresh()
            }
        }
    }

    private fun postEditImage(imgMap: MutableMap<String, String>) {
        val code = getProduct()!!.code
        imgMap[PRODUCT_BARCODE] = code
        imgMap[IMAGE_STRING_ID] = getImageStringKey(getSelectedType(), getCurrentLanguage())
        binding.imageViewFullScreen.visibility = View.INVISIBLE
        client.editImage(code, imgMap).subscribe { value ->
            if (value != null) {
                setResult(RESULTCODE_MODIFIED)
            }
            reloadProduct()
        }.addTo(disp)
    }

    private fun deleteLocalFiles() {
        if (lastViewedImage == null) return

        val deleted = lastViewedImage!!.delete()
        if (!deleted) {
            Log.w(ImagesManageActivity::class.simpleName, "Cannot delete file ${lastViewedImage!!.absolutePath}.")
        } else {
            lastViewedImage = null
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
        val image = ProductImage(getProduct()!!.code, getSelectedType(), newPhotoFile, getCurrentLanguage()).apply {
            filePath = newPhotoFile.absolutePath
        }
        client.postImg(image, true)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Toast.makeText(this@ImagesManageActivity, it.message, Toast.LENGTH_LONG).show()
                    Log.e(ImagesManageActivity::class.java.simpleName, it.message, it)
                    stopRefresh()
                }
                .subscribe {
                    reloadProduct()
                    setResult(RESULTCODE_MODIFIED)
                }.addTo(disp)
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
    }
}