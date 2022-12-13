package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.models.Barcode
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.InstallationService
import openfoodfacts.github.scrachx.openfood.utils.MediaTypes
import openfoodfacts.github.scrachx.openfood.utils.list
import openfoodfacts.github.scrachx.openfood.utils.unique
import org.greenrobot.eventbus.EventBus
import org.greenrobot.greendao.query.QueryBuilder
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineProductRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val daoSession: DaoSession,
    private val installationService: InstallationService,
    private val api: ProductsAPI,
) {
    /**
     * @return true if there is still products to upload, false otherwise
     */
    suspend fun uploadAll(includeImages: Boolean) = withContext(Dispatchers.IO) {
        for (product in getOfflineProducts()) {
            if (product.barcode.isEmpty()) {
                logcat(LogPriority.WARN) { "Ignoring product because of empty barcode: $product" }
                continue
            }

            logcat(LogPriority.DEBUG) { "Uploading product ${product.barcode}" }

            val ok = mutableListOf(
                uploadProductIfNeededSync(product)
            ).apply {
                if (includeImages) {
                    this += uploadImageIfNeededSync(product, ProductImageField.FRONT)
                    this += uploadImageIfNeededSync(product, ProductImageField.INGREDIENTS)
                    this += uploadImageIfNeededSync(product, ProductImageField.NUTRITION)
                }
            }.all { it }

            if (ok) {
                daoSession.offlineSavedProductDao.deleteByKey(product.id)
            }
        }

        if (includeImages) getOfflineProducts().isNotEmpty()
        else getOfflineProductsNotSynced().isNotEmpty()
    }

    fun getOfflineProductByBarcode(barcode: String): OfflineSavedProduct? {
        return daoSession.offlineSavedProductDao.unique {
            where(OfflineSavedProductDao.Properties.Barcode.eq(barcode))
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun getOfflineProductByBarcode(barcode: Barcode): OfflineSavedProduct? {
        return getOfflineProductByBarcode(barcode.raw)
    }

    /**
     * Performs network call and uploads the product to the server.
     * Before doing that strip images data from the product map.
     *
     */
    private suspend fun uploadProductIfNeededSync(product: OfflineSavedProduct): Boolean {
        if (product.isDataUploaded) return true

        var productDetails = product.productDetails

        // Remove the images from the HashMap before uploading the product details
        productDetails.run {
            // Remove the images from the HashMap before uploading the product details
            remove(ApiFields.Keys.IMAGE_FRONT)
            remove(ApiFields.Keys.IMAGE_INGREDIENTS)
            remove(ApiFields.Keys.IMAGE_NUTRITION)

            // Remove the status of the images from the HashMap before uploading the product details
            remove(ApiFields.Keys.IMAGE_FRONT_UPLOADED)
            remove(ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED)
            remove(ApiFields.Keys.IMAGE_NUTRITION_UPLOADED)
        }

        productDetails = productDetails.filter {
            !it.value.isNullOrEmpty() || ApiFields.Keys.PRODUCT_FIELDS_WITH_EMPTY_VALUE.contains(it.key)
        }

        logcat(LogPriority.DEBUG) { "Uploading data for product ${product.barcode}" }

        try {
            val comment = ProductRepository.getCommentToUpload(context, installationService, null)
            val productState = api.saveProduct(product.barcode, productDetails, comment)

            if (productState.status == 1L) {
                product.isDataUploaded = true
                daoSession.offlineSavedProductDao.insertOrReplace(product)
                logcat(LogPriority.INFO) { "Product ${product.barcode} uploaded." }

                // Refresh product if open
                EventBus.getDefault().post(ProductNeedsRefreshEvent(product.barcode))
                return true
            } else {
                logcat(LogPriority.WARN) {
                    "Could not upload product ${product.barcode}. Error code: ${productState.status}"
                }
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.asLog() }
        }
        return false
    }

    private suspend fun uploadImageIfNeededSync(
        product: OfflineSavedProduct,
        imageField: ProductImageField,
    ) = withContext(Dispatchers.IO) {

        val imageType = imageField.apiKey
        val imageFilePath = product.productDetails["image_$imageType"]

        if (imageFilePath == null || !needsImageUpload(product.productDetails, imageField)) {
            // no need or nothing to upload
            logcat(LogPriority.DEBUG) { "No need to upload image_$imageType for product ${product.barcode}" }
            return@withContext true
        }

        logcat(LogPriority.DEBUG) { "Uploading image_$imageType for product ${product.barcode}" }

        val imgMap = createRequestBodyMap(product.barcode, product.productDetails, imageField)
        val image = RequestBody.create(MediaType.parse("image/*"), File(imageFilePath))

        imgMap["""imgupload_$imageType"; filename="${imageType}_${product.language}.png""""] = image

        return@withContext try {
            val jsonNode = api.saveImage(imgMap)
            val status = jsonNode["status"].asText()

            if (status == "status not ok") {
                val error = jsonNode["error"].asText()
                if (error == "This picture has already been sent.") {
                    product.productDetails["image_${imageType}_uploaded"] = "true"
                    daoSession.offlineSavedProductDao.insertOrReplace(product)
                    return@withContext true
                }

                logcat(LogPriority.ERROR) { "Error uploading $imageType for product ${product.barcode}: $error" }
                return@withContext false
            }

            product.productDetails["image_${imageType}_uploaded"] = "true"

            // Refresh db
            daoSession.offlineSavedProductDao.insertOrReplace(product)
            logcat(LogPriority.DEBUG) { "Uploaded image_$imageType for product ${product.barcode}" }

            // Refresh event
            EventBus.getDefault().post(ProductNeedsRefreshEvent(product.barcode))
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message, e)
            false
        }
    }

    private inline fun getOfflineProducts(
        filterAction: QueryBuilder<OfflineSavedProduct>.() -> Unit = {},
    ): List<OfflineSavedProduct> {
        return daoSession.offlineSavedProductDao.list {
            where(OfflineSavedProductDao.Properties.Barcode.isNotNull)
            where(OfflineSavedProductDao.Properties.Barcode.notEq(""))
            filterAction()
        }
    }

    private fun getOfflineProductsNotSynced(): List<OfflineSavedProduct> {
        return getOfflineProducts {
            where(OfflineSavedProductDao.Properties.IsDataUploaded.notEq(true))
        }
    }

    private fun needsImageUpload(productDetails: Map<String, String>, imageType: ProductImageField): Boolean {
        val imageUploaded = productDetails["image_${imageType.apiKey}_uploaded"].toBoolean()
        val imageFilePath = productDetails["image_$imageType"]
        return !imageUploaded && !imageFilePath.isNullOrEmpty()
    }

    private fun createRequestBodyMap(
        code: String,
        productDetails: Map<String, String>,
        frontImg: ProductImageField,
    ): MutableMap<String, RequestBody> {
        val barcode = RequestBody.create(MediaTypes.MIME_TEXT, code)

        val imageField = RequestBody.create(
            MediaTypes.MIME_TEXT,
            "${frontImg}_${productDetails["lang"]}"
        )

        return hashMapOf(
            ApiFields.Keys.BARCODE to barcode,
            ApiFields.Keys.IMAGE_FIELD to imageField
        )
    }

    companion object {
        private val LOG_TAG = OfflineProductRepository::class.simpleName!!
    }
}
