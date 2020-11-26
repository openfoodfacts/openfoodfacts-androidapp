package openfoodfacts.github.scrachx.openfood.utils

import android.text.TextUtils
import android.util.Log
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.*

class OfflineProductService private constructor() {
    private val apiClient = CommonApiManager.instance.productsApi

    private object Loader {
        // static synchronized singleton
        val INSTANCE = OfflineProductService()
    }

    /**
     * @return true if there is still products to upload, false otherwise
     */
    fun uploadAll(includeImages: Boolean): Single<Boolean> {
        return Single.fromCallable {
            listOfflineProducts.forEach { product ->
                if (product.barcode.isNullOrEmpty()) {
                    Log.d(LOG_TAG, "Ignore product because empty barcode: $product")
                    return@forEach
                }
                Log.d(LOG_TAG, "Start treating of product $product")

                var ok = addProductToServerIfNeeded(product)
                if (includeImages) {
                    ok = ok && uploadImageIfNeeded(product, ProductImageField.FRONT)
                    ok = ok && uploadImageIfNeeded(product, ProductImageField.INGREDIENTS)
                    ok = ok && uploadImageIfNeeded(product, ProductImageField.NUTRITION)
                    if (ok) {
                        offlineProductDAO.deleteByKey(product.id)
                    }
                }

            }
            if (includeImages) {
                return@fromCallable listOfflineProducts.isNotEmpty()
            }
            return@fromCallable listOfflineProductsWithoutDataSynced.isNotEmpty()
        }
    }

    /**
     * Performs network call and uploads the product to the server.
     * Before doing that strip images data from the product map.
     *
     * @param product The offline product to be uploaded to the server.
     */
    private fun addProductToServerIfNeeded(product: OfflineSavedProduct): Boolean {
        if (product.isDataUploaded) return true

        // Remove the images from the HashMap before uploading the product details
        val productDetails = product.productDetailsMap.apply {
            // Remove the images from the HashMap before uploading the product details
            remove(ApiFields.Keys.IMAGE_FRONT)
            remove(ApiFields.Keys.IMAGE_INGREDIENTS)
            remove(ApiFields.Keys.IMAGE_NUTRITION)

            // Remove the status of the images from the HashMap before uploading the product details
            remove(ApiFields.Keys.IMAGE_FRONT_UPLOADED)
            remove(ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED)
            remove(ApiFields.Keys.IMAGE_NUTRITION_UPLOADED)
        }.filter { !it.value.isNullOrEmpty() }

        Log.d(LOG_TAG, "Uploading data for product ${product.barcode}: $productDetails")
        try {
            val productState = apiClient
                    .saveProductSingle(product.barcode, productDetails, OpenFoodAPIClient.commentToUpload)
                    .blockingGet()
            val isResponseOk = productState.status == 1L
            if (isResponseOk) {
                product.isDataUploaded = true
                offlineProductDAO.insertOrReplace(product)
                Log.i(LOG_TAG, "Product ${product.barcode} uploaded.")
                EventBus.getDefault().post(ProductNeedsRefreshEvent(product.barcode))
                return true
            } else {
                Log.i(LOG_TAG, "Could not upload product ${product.barcode}. Error code: ${productState.status}")
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message, e)
        }
        return false
    }

    private fun uploadImageIfNeeded(product: OfflineSavedProduct, imageField: ProductImageField): Boolean {
        val imageType = imageTypeFromImageField(imageField)
        val code = product.barcode
        val productDetails = product.productDetailsMap
        val imageFilePath = productDetails["image_$imageType"]
        if (imageFilePath == null || !needImageUpload(productDetails, imageType)) {
            // no need or nothing to upload
            Log.d(LOG_TAG, "No need to upload image_$imageType for product $code")
            return true
        }
        Log.d(LOG_TAG, "Uploading image_$imageType for product $code")
        val imgMap = createRequestBodyMap(code, productDetails, imageField)
        val image = ProductImage.createImageRequest(File(imageFilePath))
        imgMap["""imgupload_$imageType"; filename="${imageType}_${product.language}.png""""] = image
        return try {
            val jsonNode = apiClient.saveImageSingle(imgMap)
                    .blockingGet()
            val status = jsonNode["status"].asText()
            if (status == "status not ok") {
                val error = jsonNode["error"].asText()
                if (error == "This picture has already been sent.") {
                    productDetails["image_" + imageType + "_uploaded"] = "true"
                    product.setProductDetailsMap(productDetails)
                    offlineProductDAO.insertOrReplace(product)
                    return true
                }
                Log.e(LOG_TAG, "Error uploading $imageType: $error")
                return false
            }
            productDetails["image_${imageType}_uploaded"] = "true"
            product.setProductDetailsMap(productDetails)
            offlineProductDAO.insertOrReplace(product)
            Log.d(LOG_TAG, "Uploaded image_$imageType for product $code")
            EventBus.getDefault().post(ProductNeedsRefreshEvent(code))
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message, e)
            false
        }
    }

    companion object {
        private val LOG_TAG = this::class.simpleName!!
        private val offlineProductDAO = OFFApplication.getDaoSession().offlineSavedProductDao

        fun sharedInstance(): OfflineProductService {
            return Loader.INSTANCE
        }

        @JvmStatic
        fun getOfflineProductByBarcode(barcode: String?): OfflineSavedProduct {
            return offlineProductDAO.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(barcode)).unique()
        }

        private val listOfflineProducts = offlineProductDAO.queryBuilder()
                .where(OfflineSavedProductDao.Properties.Barcode.isNotNull)
                .where(OfflineSavedProductDao.Properties.Barcode.notEq(""))
                .list()
        private val listOfflineProductsWithoutDataSynced = offlineProductDAO.queryBuilder()
                .where(OfflineSavedProductDao.Properties.Barcode.isNotNull)
                .where(OfflineSavedProductDao.Properties.Barcode.notEq(""))
                .where(OfflineSavedProductDao.Properties.IsDataUploaded.notEq(true))
                .list()

        private fun imageTypeFromImageField(imageField: ProductImageField) = when (imageField) {
            ProductImageField.FRONT -> "front"
            ProductImageField.INGREDIENTS -> "ingredients"
            ProductImageField.NUTRITION -> "nutrition"
            else -> "other"
        }

        private fun needImageUpload(productDetails: HashMap<String, String>, imageType: String): Boolean {
            val imageUploaded = "true" == productDetails["image_${imageType}_uploaded"]
            val imageFilePath = productDetails["image_$imageType"]
            return !imageUploaded && !TextUtils.isEmpty(imageFilePath)
        }

        private fun createRequestBodyMap(code: String, productDetails: HashMap<String, String>, front: ProductImageField): MutableMap<String, RequestBody> {
            val barcode = RequestBody.create(MediaType.parse(OpenFoodAPIClient.MIME_TEXT), code)
            val imageField = RequestBody.create(MediaType.parse(OpenFoodAPIClient.MIME_TEXT), "${front}_${productDetails["lang"]}")

            return hashMapOf("code" to barcode, "imagefield" to imageField)
        }
    }

}