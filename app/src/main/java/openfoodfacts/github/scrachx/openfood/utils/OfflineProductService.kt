package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient.Companion.getCommentToUpload
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*

object OfflineProductService {

    /**
     * @return true if there is still products to upload, false otherwise
     */
    fun uploadAll(includeImages: Boolean) = Single.fromCallable {
        getListOfflineProducts().forEach { product ->
            if (product.barcode.isEmpty()) {
                Log.d(LOG_TAG, "Ignore product because empty barcode: $product")
                return@forEach
            }
            Log.d(LOG_TAG, "Start treating of product $product")

            var ok = product.uploadProductIfNeededSync()
            if (includeImages) {
                ok = ok && product.uploadImageIfNeededSync(FRONT)
                ok = ok && product.uploadImageIfNeededSync(INGREDIENTS)
                ok = ok && product.uploadImageIfNeededSync(NUTRITION)
                if (ok) {
                    offlineProductDAO.deleteByKey(product.id)
                }
            }

        }
        if (includeImages) {
            return@fromCallable getListOfflineProducts().isNotEmpty()
        }
        return@fromCallable getListOfflineProductsNotSynced().isNotEmpty()
    }

    /**
     * Performs network call and uploads the product to the server.
     * Before doing that strip images data from the product map.
     *
     * @param this@uploadProductIfNeededSync The offline product to be uploaded to the server.
     */
    private fun OfflineSavedProduct.uploadProductIfNeededSync(): Boolean {
        if (isDataUploaded) return true

        // Remove the images from the HashMap before uploading the product details
        val productDetails = productDetails.apply {
            // Remove the images from the HashMap before uploading the product details
            remove(ApiFields.Keys.IMAGE_FRONT)
            remove(ApiFields.Keys.IMAGE_INGREDIENTS)
            remove(ApiFields.Keys.IMAGE_NUTRITION)

            // Remove the status of the images from the HashMap before uploading the product details
            remove(ApiFields.Keys.IMAGE_FRONT_UPLOADED)
            remove(ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED)
            remove(ApiFields.Keys.IMAGE_NUTRITION_UPLOADED)
        }.filter { !it.value.isNullOrEmpty() }

        Log.d(LOG_TAG, "Uploading data for product $barcode: $productDetails")
        try {
            val productState = productsApi
                    .saveProduct(barcode, productDetails, getCommentToUpload())
                    .blockingGet()
            if (productState.status == 1L) {
                isDataUploaded = true
                offlineProductDAO.insertOrReplace(this)
                Log.i(LOG_TAG, "Product $barcode uploaded.")

                // Refresh product if open
                EventBus.getDefault().post(ProductNeedsRefreshEvent(barcode))
                return true
            } else {
                Log.i(LOG_TAG, "Could not upload product $barcode. Error code: ${productState.status}")
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message, e)
        }
        return false
    }

    private fun OfflineSavedProduct.uploadImageIfNeededSync(imageField: ProductImageField): Boolean {
        val imageType = imageTypeFromImageField(imageField)
        val imageFilePath = productDetails["image_$imageType"]
        if (imageFilePath == null || !needImageUpload(productDetails, imageType)) {
            // no need or nothing to upload
            Log.d(LOG_TAG, "No need to upload image_$imageType for product $barcode")
            return true
        }
        Log.d(LOG_TAG, "Uploading image_$imageType for product $barcode")
        val imgMap = createRequestBodyMap(barcode, productDetails, imageField)
        val image = ProductImage.createImageRequest(File(imageFilePath))
        imgMap["""imgupload_$imageType"; filename="${imageType}_$language.png""""] = image
        return try {
            val jsonNode = productsApi.saveImageSingle(imgMap).blockingGet()
            val status = jsonNode["status"].asText()
            if (status == "status not ok") {
                val error = jsonNode["error"].asText()
                if (error == "This picture has already been sent.") {
                    productDetails["image_${imageType}_uploaded"] = "true"
                    offlineProductDAO.insertOrReplace(this)
                    return true
                }
                Log.e(LOG_TAG, "Error uploading $imageType: $error")
                return false
            }
            productDetails["image_${imageType}_uploaded"] = "true"
            offlineProductDAO.insertOrReplace(this)
            Log.d(LOG_TAG, "Uploaded image_$imageType for product $barcode")
            EventBus.getDefault().post(ProductNeedsRefreshEvent(barcode))
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message, e)
            false
        }
    }

    private val LOG_TAG = OfflineProductService::class.simpleName!!
    private val offlineProductDAO = OFFApplication.daoSession.offlineSavedProductDao

    fun getOfflineProductByBarcode(barcode: String): OfflineSavedProduct? =
            offlineProductDAO.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(barcode)).unique()

    private fun getListOfflineProducts() = offlineProductDAO.queryBuilder()
            .where(OfflineSavedProductDao.Properties.Barcode.isNotNull)
            .where(OfflineSavedProductDao.Properties.Barcode.notEq(""))
            .list()

    private fun getListOfflineProductsNotSynced() = offlineProductDAO.queryBuilder()
            .where(OfflineSavedProductDao.Properties.Barcode.isNotNull)
            .where(OfflineSavedProductDao.Properties.Barcode.notEq(""))
            .where(OfflineSavedProductDao.Properties.IsDataUploaded.notEq(true))
            .list()

    private fun imageTypeFromImageField(imageField: ProductImageField) = when (imageField) {
        FRONT -> "front"
        INGREDIENTS -> "ingredients"
        NUTRITION -> "nutrition"
        else -> "other"
    }

    private fun needImageUpload(productDetails: Map<String, String>, imageType: String): Boolean {
        val imageUploaded = productDetails["image_${imageType}_uploaded"].toBoolean()
        val imageFilePath = productDetails["image_$imageType"]
        return !imageUploaded && !imageFilePath.isNullOrEmpty()
    }

    private fun createRequestBodyMap(code: String, productDetails: Map<String, String>, front: ProductImageField): MutableMap<String, RequestBody> {
        val barcode = RequestBody.create(MediaType.parse(OpenFoodAPIClient.MIME_TEXT), code)
        val imageField = RequestBody.create(
                MediaType.parse(OpenFoodAPIClient.MIME_TEXT),
                "${front}_${productDetails["lang"]}"
        )

        return hashMapOf("code" to barcode, "imagefield" to imageField)
    }
}