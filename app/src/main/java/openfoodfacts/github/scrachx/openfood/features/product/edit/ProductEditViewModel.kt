package openfoodfacts.github.scrachx.openfood.features.product.edit

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.jobs.ProductUploaderWorker
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository.Companion.addToHistory
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import java.io.IOException
import javax.inject.Inject

class ProductEditViewModel @Inject constructor(
    private val productsApi: ProductsAPI,
    private val dispatchers: CoroutineDispatchers,
    private val matomoAnalytics: MatomoAnalytics,
    private val sharedPreferences: SharedPreferences,
    private val daoSession: DaoSession,
    application: Application
) : AndroidViewModel(application) {


    sealed class IngredientsProgress {
        object LoadingOCR : IngredientsProgress()
        class Success(val status: String, val ingredientsText: String?) : IngredientsProgress()
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
    }

    private val _sideEffect = MutableSharedFlow<SideEffect>()
    val sideEffect = _sideEffect.asLiveData(viewModelScope.coroutineContext)

    fun nextFragment() {
        viewModelScope.launch(dispatchers.Default) { _sideEffect.emit(SideEffect.NextFragment) }
    }

    private val _initialValues = MutableStateFlow<MutableFields?>(null)
    val initialValues = _initialValues.asStateFlow()
    fun initValues() {
        viewModelScope.launch(dispatchers.Default) { _initialValues.value = mutableFieldsOf() }
    }

    fun addToInitialValues(fields: Fields) {
        viewModelScope.launch(dispatchers.Default) {
            _initialValues.value?.let { values ->
                _initialValues.value = values.apply { putAll(fields) }
            }
        }
    }

    private val _productFields = MutableStateFlow(mutableFieldsOf())
    val productFields = _productFields.asStateFlow()

    fun setProductDetails(fields: Fields) {
        viewModelScope.launch(dispatchers.Default) {
            _productFields.value = _productFields.value.apply { putAll(fields) }
        }
    }

    fun setProductDetails(vararg field: Field): Unit = setProductDetails(field.toMap())

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
}