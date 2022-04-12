package openfoodfacts.github.scrachx.openfood.features.product.edit

import android.app.Application
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.images.IMG_ID
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.jobs.ProductUploaderWorker
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository.Companion.addToHistory
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import java.io.IOException
import javax.inject.Inject
import kotlin.collections.set

class ProductEditViewModel @Inject constructor(
    private val productsApi: ProductsAPI,
    private val dispatchers: CoroutineDispatchers,
    private val matomoAnalytics: MatomoAnalytics,
    private val sharedPreferences: SharedPreferences,
    private val daoSession: DaoSession,
    private val productRepository: ProductRepository,
    application: Application
) : AndroidViewModel(application) {


    sealed class IngredientsProgress {
        object LoadingOCR : IngredientsProgress()
        data class Success(val status: String, val ingredientsText: String?) : IngredientsProgress()
        open class Error(val status: String, val code: String?, val imageField: String?) : IngredientsProgress()
        object NoInternetError : Error("No internet", null, null)
    }

    private val _ingredientsProgress = MutableSharedFlow<IngredientsProgress>()
    val ingredientsProgress = _ingredientsProgress.asLiveData(viewModelScope.coroutineContext)

    fun setIngredients(status: String, ingredientsText: String?) {
        viewModelScope.launch(dispatchers.Default) {
            _ingredientsProgress.emit(IngredientsProgress.Success(status, ingredientsText))
        }
    }

    fun performIngredientsOCR(barcode: String, imageField: String) {
        viewModelScope.launch(dispatchers.IO) {
            _ingredientsProgress.emit(IngredientsProgress.LoadingOCR)

            val node = productsApi.runCatching { performOCR(barcode, imageField) }
                .onFailure { err ->
                    if (err is IOException) {
                        _ingredientsProgress.emit(IngredientsProgress.NoInternetError)
                    } else {
                        logcat(LogPriority.ERROR) { err.asLog() }
                        _ingredientsProgress.emit(IngredientsProgress.Error(err.message ?: "Empty error", barcode, imageField))
                    }
                }
                .getOrNull()
                ?: return@launch


            val status = node["status"].asText()
            if (status == "0") {
                val ocrResult = node["ingredients_text_from_image"].asText()
                _ingredientsProgress.emit(IngredientsProgress.Success(status, ocrResult))
            } else {
                _ingredientsProgress.emit(IngredientsProgress.Error(status, barcode, imageField))
            }
        }
    }

    sealed class SideEffect {
        object NextFragment : SideEffect()
        object LanguageUpdated : SideEffect()
    }

    private val _sideEffect = MutableSharedFlow<SideEffect>()
    val sideEffects = _sideEffect.asLiveData(viewModelScope.coroutineContext)

    fun nextFragment() {
        viewModelScope.launch(dispatchers.Default) { _sideEffect.emit(SideEffect.NextFragment) }
    }

    fun onLanguageUpdated() {
        viewModelScope.launch(dispatchers.Default) { _sideEffect.emit(SideEffect.LanguageUpdated) }
    }

    private val _initialValues = MutableStateFlow(mutableFieldsOf())
    val initialValues = _initialValues.asStateFlow()

    fun addToInitialValues(fields: Fields) {
        viewModelScope.launch(dispatchers.Default) {
            _initialValues.value = _initialValues.value.apply { putAll(fields) }
        }
    }

    private val _productFields = MutableStateFlow(mutableFieldsOf())
    val productFields = _productFields.asStateFlow()

    fun setProductDetails(fields: Fields) {
        viewModelScope.launch(dispatchers.Default) {
            _productFields.value = _productFields.value.apply { putAll(fields) }
        }
    }

    fun setProductDetails(vararg field: Field) = setProductDetails(field.toMap())
    fun setProductLanguageCode(languageCode: String) = setProductDetails(ApiFields.Keys.LANG to languageCode)

    fun getProductLanguageForEdition() = productFields.value[ApiFields.Keys.LANG]

    data class ImageData(
        val pathsMap: MutableMap<ImageType, String?> = mutableMapOf(),
        val uploadedMap: MutableMap<ImageType, Boolean> = mutableMapOf()
    )

    val imagesData = MutableStateFlow(ImageData())

    fun updateImagesData(update: ImageData.() -> Unit) {
        imagesData.value = imagesData.value.apply(update)
    }

    fun saveProductOffline(editingMode: Boolean) {
        viewModelScope.launch(dispatchers.IO) {
            // Add the images to the productDetails to display them in UI later.
            val productFields = productFields.value
            val imageData = imagesData.value

            val pathsMap = imageData.pathsMap
            val uploadedMap = imageData.uploadedMap

            pathsMap[ImageType.FRONT]?.let { productFields[ApiFields.Keys.IMAGE_FRONT] = it }
            pathsMap[ImageType.INGREDIENTS]?.let { productFields[ApiFields.Keys.IMAGE_INGREDIENTS] = it }
            pathsMap[ImageType.NUTRITION]?.let { productFields[ApiFields.Keys.IMAGE_NUTRITION] = it }

            // Add the status of images to the productDetails, whether uploaded or not
            if (uploadedMap[ImageType.FRONT] == true) {
                productFields[ApiFields.Keys.IMAGE_FRONT_UPLOADED] = true.toString()
            }
            if (uploadedMap[ImageType.INGREDIENTS] == true) {
                productFields[ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED] = true.toString()
            }
            if (uploadedMap[ImageType.NUTRITION] == true) {
                productFields[ApiFields.Keys.IMAGE_NUTRITION_UPLOADED] = true.toString()
            }

            val barcode = productFields[ApiFields.Keys.BARCODE]!!

            // Save product to local database
            val toSaveOffline = OfflineSavedProduct(barcode, productFields)

            withContext(dispatchers.IO) {
                // Add to upload queue
                daoSession.offlineSavedProductDao.insertOrReplace(toSaveOffline)

                // Add to history db
                daoSession.historyProductDao.addToHistory(toSaveOffline)
            }

            ProductUploaderWorker.scheduleProductUpload(getApplication(), sharedPreferences)

            // Report analytics
            matomoAnalytics.trackEvent(
                if (editingMode) {
                    AnalyticsEvent.ProductEdited(productFields[ApiFields.Keys.BARCODE])
                } else {
                    AnalyticsEvent.ProductCreated(productFields[ApiFields.Keys.BARCODE])
                }
            )
        }
    }

    private val _product = MutableStateFlow<Product?>(null)
    val product = _product.asStateFlow()

    fun setProduct(product: Product) {
        viewModelScope.launch(dispatchers.Default) { _product.compareAndSet(null, product) }
    }

    enum class ImagePosition {
        OVERVIEW_MAIN, INGREDIENTS, NUTRITION, OVERVIEW_OTHER, PHOTOS
    }

    sealed class ImageProgress(val position: ImagePosition) {
        class Loading(position: ImagePosition) : ImageProgress(position)
        class Done(position: ImagePosition, val message: String) : ImageProgress(position)
        class Error(position: ImagePosition, val message: String) : ImageProgress(position)
    }

    private val _imageProgress = MutableSharedFlow<ImageProgress>()

    fun getImageProgress(position: ImagePosition): LiveData<ImageProgress> =
        _imageProgress
            .filter { it.position == position }
            .asLiveData(viewModelScope.coroutineContext)


    fun uploadPhoto(image: ProductImage, position: ImagePosition) {

        updateImagesData { pathsMap[image.imageField] = image.filePath }

        val performOCR = image.imageField == ImageType.INGREDIENTS

        viewModelScope.launch(dispatchers.IO) {
            // Start loading...
            _imageProgress.emit(ImageProgress.Loading(position))

            val jsonNode = productsApi
                .runCatching { uploadImage(productRepository.getImageUploadMap(image)) }
                .onFailure { onImageUploadError(it, position, image) }
                .getOrNull()
                ?: return@launch

            if (jsonNode["status"].asText() == "status not ok") {
                val error = jsonNode["error"].asText()

                val alreadySent = ("This picture has already been sent." in error)

                if (alreadySent && performOCR) {
                    _imageProgress.emit(
                        ImageProgress.Done(
                            position = position,
                            message = getApplication<OFFApplication>().getString(R.string.image_uploaded_successfully)
                        )
                    )

                    performIngredientsOCR(image.barcode, "ingredients_${getProductLanguageForEdition()}")

                } else {
                    _imageProgress.emit(
                        ImageProgress.Error(
                            position = position,
                            message = error
                        )
                    )
                }

            } else {

                updateImagesData {
                    uploadedMap[image.imageField] = true
                }

                _imageProgress.emit(
                    ImageProgress.Done(
                        position = position,
                        message = getApplication<OFFApplication>().getString(R.string.image_uploaded_successfully)
                    )
                )

                val imageField = jsonNode["imagefield"].asText()
                val imgId = jsonNode["image"]["imgid"].asText()

                if (position !in listOf(ImagePosition.OVERVIEW_OTHER, ImagePosition.PHOTOS)) {
                    // Not OTHER image
                    setProductPhoto(
                        image = image,
                        imageField = imageField,
                        imgId = imgId,
                        performOCR = performOCR
                    )
                }
            }
        }
    }

    private suspend fun onImageUploadError(
        err: Throwable,
        position: ImagePosition,
        image: ProductImage
    ) {
        when (err) {
            is IOException -> {
                // A network error happened

                _imageProgress.emit(
                    ImageProgress.Error(
                        position = position,
                        message = getApplication<OFFApplication>().getString(R.string.no_internet_connection)
                    )
                )

                logcat(LogPriority.ERROR) { err.asLog() }

                if (image.imageField === ImageType.OTHER) {
                    daoSession.toUploadProductDao.insertOrReplace(ToUploadProduct(image = image))
                }
            }
            else -> {
                logcat(LogPriority.WARN) { err.asLog() }

                _imageProgress.emit(
                    ImageProgress.Error(
                        position = position,
                        message = err.message ?: "Empty error."
                    )
                )
            }
        }
    }

    /**
     * Set a photo as the product photo for the [imageField].
     */
    private suspend fun setProductPhoto(
        image: ProductImage,
        imageField: String,
        imgId: String,
        performOCR: Boolean
    ) {
        viewModelScope.launch(dispatchers.IO) coroutine@{

            val queryMap = mapOf(
                IMG_ID to imgId,
                "id" to imageField
            )

            val jsonNode = withContext(dispatchers.IO) {
                productsApi.runCatching { editImage(image.barcode, queryMap) }
            }.onFailure { err ->
                if (err is IOException) {
                    if (performOCR) {
                        /* TODO: Handle this case
                        val view = findViewById<View>(R.id.coordinator_layout)
                        Snackbar.make(
                            view,
                            R.string.no_internet_unable_to_extract_ingredients,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(R.string.txt_try_again) {
                            lifecycleScope.launch { setPhoto(image, imageField, imgId, true) }
                        }.show()
                        */
                    }
                } else {
                    logcat(LogPriority.WARN) { err.asLog() }
                    withContext(dispatchers.Main) {
                        Toast.makeText(getApplication(), err.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }.getOrNull() ?: return@coroutine

            val status = jsonNode["status"].asText()
            if (performOCR && status == "status ok") {
                performIngredientsOCR(image.barcode, imageField)
            }
        }
    }
}