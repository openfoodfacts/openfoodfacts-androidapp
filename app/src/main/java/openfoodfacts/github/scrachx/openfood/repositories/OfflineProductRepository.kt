package openfoodfacts.github.scrachx.openfood.repositories

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.ImageType
import openfoodfacts.github.scrachx.openfood.models.ImageType.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.asImageRequest
import openfoodfacts.github.scrachx.openfood.utils.asRequestBody
import openfoodfacts.github.scrachx.openfood.utils.list
import openfoodfacts.github.scrachx.openfood.utils.uniqueAsync
import org.greenrobot.eventbus.EventBus
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineProductRepository @Inject constructor(
    private val daoSession: DaoSession,
    private val productsAPI: ProductsAPI,
    private val client: ProductRepository
) {
    /**
     * @return true if there is still products to upload, false otherwise
     */
    suspend fun uploadAll(includeImages: Boolean) = withContext(Dispatchers.IO) {

        getOfflineProducts()
            .asSequence()
            .filter { product ->
                val barcodeEmpty = product.barcode.isEmpty()
                if (barcodeEmpty) {
                    logcat { "Ignoring upload because of empty barcode: $product" }
                }
                barcodeEmpty
            }
            .forEach { product ->
                logcat { "Uploading offline product with barcode ${product.barcode}..." }

                val ok = uploadProduct(product, includeImages)

                if (ok) {
                    daoSession.offlineSavedProductDao.deleteByKey(product.id)
                }
            }

        if (includeImages) getOfflineProducts().isNotEmpty()
        else getOfflineProductsNotSynced().isNotEmpty()
    }

    private suspend fun uploadProduct(
        product: OfflineSavedProduct,
        includeImages: Boolean
    ): Boolean {
        val ok = mutableListOf(uploadProductIfNeededSync(product))
        if (includeImages) {
            ok += uploadImageIfNeededSync(product, FRONT)
            ok += uploadImageIfNeededSync(product, INGREDIENTS)
            ok += uploadImageIfNeededSync(product, NUTRITION)
        }
        return ok.all { it }
    }

    suspend fun getOfflineProductByBarcode(barcode: String): OfflineSavedProduct? {
        return daoSession.offlineSavedProductDao.uniqueAsync {
            where(OfflineSavedProductDao.Properties.Barcode.eq(barcode))
        }
    }

    /**
     * Performs network call and uploads the product to the server.
     * Before doing that strip images data from the product map.
     *
     */
    private suspend fun uploadProductIfNeededSync(product: OfflineSavedProduct): Boolean {
        if (product.isDataUploaded) return true

        // Remove the images from the HashMap before uploading the product details
        val productDetails = product.productDetails.apply {
            // Remove the images from the HashMap before uploading the product details
            remove(ApiFields.Keys.IMAGE_FRONT)
            remove(ApiFields.Keys.IMAGE_INGREDIENTS)
            remove(ApiFields.Keys.IMAGE_NUTRITION)

            // Remove the status of the images from the HashMap before uploading the product details
            remove(ApiFields.Keys.IMAGE_FRONT_UPLOADED)
            remove(ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED)
            remove(ApiFields.Keys.IMAGE_NUTRITION_UPLOADED)
        }.filter { !it.value.isNullOrEmpty() || it.key in ApiFields.Keys.PRODUCT_FIELDS_WITH_EMPTY_VALUE }

        logcat { "Uploading data for product ${product.barcode}: $productDetails" }
        try {
            val productState = productsAPI.saveProduct(product.barcode, productDetails, client.getCommentToUpload())

            if (productState.status == 1L) {
                product.isDataUploaded = true
                daoSession.offlineSavedProductDao.insertOrReplace(product)
                logcat { "Product ${product.barcode} uploaded." }

                // Refresh product if open
                EventBus.getDefault().post(ProductNeedsRefreshEvent(product.barcode))
                return true
            } else {
                logcat { "Could not upload product ${product.barcode}. Error code: ${productState.status}" }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message, e)
        }
        return false
    }

    private suspend fun uploadImageIfNeededSync(
        product: OfflineSavedProduct,
        imageField: ImageType
    ) = withContext(Dispatchers.IO) {

        val type = imageField.fieldName
        val imageFilePath = product.productDetails["image_$type"]

        if (imageFilePath == null || !product.isImageUploadNeeded(type)) {
            // no need or nothing to upload
            logcat { "No need to upload image_$type for product ${product.barcode}" }
            return@withContext true
        }

        logcat { "Uploading image_$type for product ${product.barcode}" }

        val imgMap = createRequestBodyMap(product, imageField)
        val imageReqBody = File(imageFilePath).asImageRequest()

        imgMap["""imgupload_$type"; filename="${type}_${product.language}.png""""] = imageReqBody

        return@withContext try {
            val jsonNode = productsAPI.uploadImage(imgMap)
            val status = jsonNode["status"].asText()

            if (status == "status not ok") {
                val error = jsonNode["error"].asText()
                if (error == "This picture has already been sent.") {
                    product.productDetails["image_${type}_uploaded"] = "true"
                    daoSession.offlineSavedProductDao.insertOrReplace(product)
                    return@withContext true
                }

                Log.e(LOG_TAG, "Error uploading $type: $error")
                return@withContext false
            }

            product.productDetails["image_${type}_uploaded"] = "true"

            // Refresh db
            daoSession.offlineSavedProductDao.insertOrReplace(product)
            logcat { "Uploaded image_$type for product ${product.barcode}" }

            // Refresh event
            EventBus.getDefault().post(ProductNeedsRefreshEvent(product.barcode))
            true
        } catch (e: Exception) {
            logcat(LogPriority.ERROR) { e.asLog() }
            false
        }
    }

    private fun getOfflineProducts(): List<OfflineSavedProduct> {
        return daoSession.offlineSavedProductDao.list {
            where(OfflineSavedProductDao.Properties.Barcode.isNotNull)
            where(OfflineSavedProductDao.Properties.Barcode.notEq(""))
        }
    }

    private fun getOfflineProductsNotSynced(): List<OfflineSavedProduct> {
        return daoSession.offlineSavedProductDao.list {
            where(OfflineSavedProductDao.Properties.Barcode.isNotNull)
            where(OfflineSavedProductDao.Properties.Barcode.notEq(""))
            where(OfflineSavedProductDao.Properties.IsDataUploaded.notEq(true))
        }
    }


    private val ImageType.fieldName
        get() = when (this) {
            FRONT -> "front"
            INGREDIENTS -> "ingredients"
            NUTRITION -> "nutrition"
            PACKAGING -> "packaging"
            OTHER -> "other"
        }

    private fun OfflineSavedProduct.isImageUploadNeeded(imageType: String): Boolean {
        val productDetails = productDetails
        val imageUploaded = productDetails["image_${imageType}_uploaded"].toBoolean()
        val imageFilePath = productDetails["image_$imageType"]

        return !imageUploaded && !imageFilePath.isNullOrEmpty()
    }

    private fun createRequestBodyMap(
        product: OfflineSavedProduct,
        type: ImageType
    ): MutableMap<String, RequestBody> {

        val barcode = product.barcode.asRequestBody()
        val productDetails = product.productDetails

        val imageField =
            "${type}_${productDetails["lang"]}".asRequestBody()


        return mutableMapOf(
            ApiFields.Keys.BARCODE to barcode,
            ApiFields.Keys.IMAGE_FIELD to imageField
        )
    }

    companion object {
        private val LOG_TAG = OfflineProductRepository::class.simpleName!!
    }
}
