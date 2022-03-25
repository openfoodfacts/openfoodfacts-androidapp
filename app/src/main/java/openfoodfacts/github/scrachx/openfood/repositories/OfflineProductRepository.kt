package openfoodfacts.github.scrachx.openfood.repositories

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductImageField.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.list
import openfoodfacts.github.scrachx.openfood.utils.unique
import org.greenrobot.eventbus.EventBus
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineProductRepository @Inject constructor(
    private val daoSession: DaoSession,
    private val api: ProductsAPI,
    private val client: ProductRepository
) {
    /**
     * @return true if there is still products to upload, false otherwise
     */
    suspend fun uploadAll(includeImages: Boolean) = withContext(Dispatchers.IO) {
        for (product in getOfflineProducts()) {
            if (product.barcode.isEmpty()) {
                Log.d(LOG_TAG, "Ignore product because empty barcode: $product")
                continue
            }
            Log.d(LOG_TAG, "Start treating of product $product")


            val ok = mutableListOf(
                uploadProductIfNeededSync(product)
            ).apply {
                if (includeImages) {
                    this += uploadImageIfNeededSync(product, FRONT)
                    this += uploadImageIfNeededSync(product, INGREDIENTS)
                    this += uploadImageIfNeededSync(product, NUTRITION)
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
        }.filter { !it.value.isNullOrEmpty() || ApiFields.Keys.PRODUCT_FIELDS_WITH_EMPTY_VALUE.contains(it.key) }

        Log.d(LOG_TAG, "Uploading data for product ${product.barcode}: $productDetails")
        try {
            val productState = api.saveProduct(product.barcode, productDetails, client.getCommentToUpload())

            if (productState.status == 1L) {
                product.isDataUploaded = true
                daoSession.offlineSavedProductDao.insertOrReplace(product)
                Log.i(LOG_TAG, "Product ${product.barcode} uploaded.")

                // Refresh product if open
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

    private suspend fun uploadImageIfNeededSync(
        product: OfflineSavedProduct,
        imageField: ProductImageField
    ) = withContext(Dispatchers.IO) {

        val imageType = imageField.imageType()
        val imageFilePath = product.productDetails["image_$imageType"]

        if (imageFilePath == null || !isImageUploadNeede(product.productDetails, imageType)) {
            // no need or nothing to upload
            Log.d(LOG_TAG, "No need to upload image_$imageType for product ${product.barcode}")
            return@withContext true
        }

        Log.d(LOG_TAG, "Uploading image_$imageType for product ${product.barcode}")

        val imgMap = createRequestBodyMap(product.barcode, product.productDetails, imageField)
        val image = ProductImage.createImageRequest(File(imageFilePath))

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

                Log.e(LOG_TAG, "Error uploading $imageType: $error")
                return@withContext false
            }

            product.productDetails["image_${imageType}_uploaded"] = "true"

            // Refresh db
            daoSession.offlineSavedProductDao.insertOrReplace(product)
            Log.d(LOG_TAG, "Uploaded image_$imageType for product ${product.barcode}")

            // Refresh event
            EventBus.getDefault().post(ProductNeedsRefreshEvent(product.barcode))
            true
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message, e)
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


    private fun ProductImageField.imageType() = when (this) {
        FRONT -> "front"
        INGREDIENTS -> "ingredients"
        NUTRITION -> "nutrition"
        else -> "other"
    }

    private fun isImageUploadNeede(productDetails: Map<String, String>, imageType: String): Boolean {
        val imageUploaded = productDetails["image_${imageType}_uploaded"].toBoolean()
        val imageFilePath = productDetails["image_$imageType"]
        return !imageUploaded && !imageFilePath.isNullOrEmpty()
    }

    private fun createRequestBodyMap(
        code: String,
        productDetails: Map<String, String>,
        frontImg: ProductImageField
    ): MutableMap<String, RequestBody> {
        val barcode = RequestBody.create(ProductRepository.MIME_TEXT, code)

        val imageField = RequestBody.create(
            ProductRepository.MIME_TEXT,
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
