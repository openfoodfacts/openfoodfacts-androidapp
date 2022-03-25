package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.fasterxml.jackson.databind.JsonNode
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.rx2.rxSingle
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.analytics.SentryAnalytics
import openfoodfacts.github.scrachx.openfood.images.*
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProductDao
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Keys
import openfoodfacts.github.scrachx.openfood.network.ApiFields.getAllFields
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API Client for all API callbacks
 */
@Singleton
class ProductRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val daoSession: DaoSession,
    private val rawApi: ProductsAPI,
    private val sentryAnalytics: SentryAnalytics,
    private val localeManager: LocaleManager,
    private val installationService: InstallationService
) {

    suspend fun getProductStateFull(
        barcode: String,
        fields: String = getAllFields(localeManager.getLanguage()),
        userAgent: String = Utils.HEADER_USER_AGENT_SEARCH
    ): ProductState {
        sentryAnalytics.setBarcode(barcode)
        return withContext(IO) {
            rawApi.getProductByBarcode(barcode, fields, localeManager.getLanguage(), getUserAgent(userAgent))
        }
    }

    suspend fun getProductsByBarcode(
        codes: List<String>,
        customHeader: String = Utils.HEADER_USER_AGENT_SEARCH
    ) = rawApi.getProductsByBarcode(
        codes.joinToString(","),
        getAllFields(localeManager.getLanguage()),
        customHeader
    ).products

    /**
     * Open the product activity if the barcode exist.
     * Also add it in the history if the product exist.
     *
     * @param barcode product barcode
     */
    suspend fun getProductImages(barcode: String): ProductState = withContext(IO) {
        val fields = Keys.PRODUCT_IMAGES_FIELDS.toMutableSet().also {
            it += Keys.lcProductNameKey(localeManager.getLanguage())
        }.joinToString(",")

        rawApi.getProductByBarcode(
            barcode,
            fields,
            localeManager.getLanguage(),
            getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
        )
    }

    /**
     * @return a list of product ingredients (can be empty)
     */
    suspend fun getIngredients(product: Product) = withContext(IO) {
        // TODO: This or the field inside Product.kt?
        val productState = rawApi.getIngredientsByBarcode(product.code)

        productState["product"][Keys.INGREDIENTS]?.map {
            ProductIngredient(
                it["id"].asText(),
                it["text"].asText(),
                it["rank"]?.asLong(-1)!!
            )
        } ?: emptyList()
    }


    fun searchProductsByName(name: String, page: Int) = rxSingle(IO) {
        rawApi.searchProductByName(name, fieldsToFetchFacets, page)
    }

    fun getProductsByCountry(country: String, page: Int) = rxSingle(IO) {
        rawApi.getProductsByCountry(country, page, fieldsToFetchFacets)
    }

    /**
     * Returns a map for images uploaded for product/ingredients/nutrition/other images
     *
     * @param image object of ProductImage
     */
    private fun getUploadableMap(image: ProductImage): Map<String, RequestBody?> {
        val lang = image.language

        val imgMap = hashMapOf(PRODUCT_BARCODE to image.barcodeBody, "imagefield" to image.fieldBody)
        if (image.imgFront != null) {
            imgMap["""imgupload_front"; filename="front_$lang$PNG_EXT"""] = image.imgFront!!
        }
        if (image.imgIngredients != null) {
            imgMap["""imgupload_ingredients"; filename="ingredients_$lang$PNG_EXT"""] = image.imgIngredients!!
        }
        if (image.imgNutrition != null) {
            imgMap["""imgupload_nutrition"; filename="nutrition_$lang$PNG_EXT"""] = image.imgNutrition!!
        }
        if (image.imgPackaging != null) {
            imgMap["""imgupload_packaging"; filename="packaging_$lang$PNG_EXT"""] = image.imgPackaging!!
        }
        if (image.imgOther != null) {
            imgMap["""imgupload_other"; filename="other_$lang$PNG_EXT"""] = image.imgOther!!
        }

        // Attribute the upload to the connected user
        getUserInfo().forEach { (key, value) ->
            imgMap[key] = RequestBody.create(MIME_TEXT, value)
        }
        return imgMap
    }

    fun getProductsByCategory(category: String, page: Int) = rxSingle {
        rawApi.getProductByCategory(category, page, fieldsToFetchFacets)
    }

    fun getProductsByLabel(label: String, page: Int) = rxSingle {
        rawApi.getProductsByLabel(label, page, fieldsToFetchFacets)
    }

    /**
     * Add a product to ScanHistory asynchronously
     */
    suspend fun addToHistory(product: Product) = withContext(IO) {
        daoSession.historyProductDao.addToHistory(product, localeManager.getLanguage())
    }

    fun getProductsByContributor(contributor: String, page: Int) = rxSingle(IO) {
        rawApi.getProductsByContributor(contributor, page, fieldsToFetchFacets)
    }

    /**
     * upload images in offline mode
     *
     * @return ListenableFuture
     */
    suspend fun uploadOfflineImages() = withContext(IO) {
        daoSession.toUploadProductDao.queryBuilder()
            .where(ToUploadProductDao.Properties.Uploaded.eq(false))
            .list()
            .forEach { product ->
                val imageFile = try {
                    File(product.imageFilePath)
                } catch (e: Exception) {
                    Log.e("OfflineUploadingTask", "doInBackground", e)
                    return@forEach
                }
                val productImage = ProductImage(
                    product.barcode,
                    product.productField,
                    imageFile,
                    localeManager.getLanguage()
                )
                val jsonNode = rawApi.saveImage(getUploadableMap(productImage))

                Log.d("onResponse", jsonNode.toString())
                if (!jsonNode.isObject) {
                    throw IOException("jsonNode is not an object")
                } else if (jsonNode[Keys.STATUS].asText().contains(ApiFields.Defaults.STATUS_NOT_OK)) {
                    daoSession.toUploadProductDao.delete(product)
                    throw IOException(ApiFields.Defaults.STATUS_NOT_OK)
                } else {
                    daoSession.toUploadProductDao.delete(product)
                }
            }
    }

    fun getProductsByPackaging(packaging: String, page: Int): Single<Search> = rxSingle {
        rawApi.getProductsByPackaging(packaging, page, fieldsToFetchFacets)
    }

    fun getProductsByStore(store: String, page: Int): Single<Search> = rxSingle(IO) {
        rawApi.getProductByStores(store, page, fieldsToFetchFacets)
    }

    /**
     * Search for products using bran name
     *
     * @param brand search query for product
     * @param page page numbers
     */
    fun getProductsByBrand(brand: String, page: Int) = rxSingle(IO) {
        rawApi.getProductByBrands(brand, page, fieldsToFetchFacets)
    }

    suspend fun postImg(image: ProductImage, setAsDefault: Boolean = false) = withContext(IO) {
        try {
            val body = rawApi.saveImage(getUploadableMap(image))

            if (!body.isObject) {
                throw IOException("body is not an object")
            } else if (
                ApiFields.Defaults.STATUS_NOT_OK in body[Keys.STATUS].asText()) {
                throw IOException(body["error"].asText())
            } else if (setAsDefault) setDefaultImageFromServerResponse(body, image)

        } catch (err: Exception) {
            daoSession.toUploadProductDao.insertOrReplace(
                ToUploadProduct(
                    image.barcode,
                    image.filePath,
                    image.imageField.toString()
                )
            )
        }

        return@withContext
    }

    private suspend fun setDefaultImageFromServerResponse(body: JsonNode, image: ProductImage) {
        val queryMap = getUserInfo() + listOf(
            IMG_ID to body["image"][IMG_ID].asText(),
            "id" to body["imagefield"].asText()
        )

        val node = rawApi.editImage(image.barcode, queryMap)

        if (node[Keys.STATUS].asText() != "status ok") throw IOException(node["error"].asText())
    }

    suspend fun editImage(code: String, imgMap: MutableMap<String, String>) = withContext(IO) {
        rawApi.editImages(code, imgMap + getUserInfo())
    }

    /**
     * Unselect the image from the product code.
     *
     * @param code code of the product
     */
    suspend fun unSelectImage(code: String, field: ProductImageField, language: String) = withContext(IO) {
        val imgMap = getUserInfo() + (IMAGE_STRING_ID to getImageStringKey(field, language))
        rawApi.unSelectImage(code, imgMap)
    }

    fun getProductsByOrigin(origin: String, page: Int) = rxSingle(IO) {
        rawApi.getProductsByOrigin(origin, page, fieldsToFetchFacets)
    }

    suspend fun syncOldHistory() = withContext(IO) {
        val fields = listOf(
            Keys.IMAGE_SMALL_URL,
            Keys.PRODUCT_NAME,
            Keys.BRANDS,
            Keys.QUANTITY,
            IMAGE_URL,
            Keys.NUTRITION_GRADE_FR,
            Keys.BARCODE
        ).joinToString(",")

        daoSession.historyProductDao.loadAll().forEach { historyProduct ->
            val state = rawApi.getProductByBarcode(
                historyProduct.barcode,
                fields,
                localeManager.getLanguage(),
                getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
            )

            // Products not found should be skipped
            if (state.status == 0L && state.statusVerbose?.contains("not found") != true) {
                throw IOException("Could not sync history. Error with product ${state.code} ")
            } else if (state.status > 0L) {
                val product = state.product!!
                val hp = HistoryProduct(
                    product.productName,
                    product.brands,
                    product.getImageSmallUrl(localeManager.getLanguage()),
                    product.code,
                    product.quantity,
                    product.nutritionGradeFr,
                    product.ecoscore,
                    product.novaGroups
                )
                Log.d("syncOldHistory", hp.toString())
                hp.lastSeen = historyProduct.lastSeen
                daoSession.historyProductDao.insertOrReplace(hp)
            }
        }


        context.getAppPreferences().edit {
            putBoolean("is_old_history_data_synced", true)
        }
    }

    fun getInfoAddedIncompleteProductsSingle(contributor: String, page: Int) = rxSingle(IO) {
        rawApi.getInfoAddedIncompleteProducts(contributor, page)
    }

    fun getProductsByManufacturingPlace(manufacturingPlace: String, page: Int) = rxSingle(IO) {
        rawApi.getProductsByManufacturingPlace(manufacturingPlace, page, fieldsToFetchFacets)
    }

    /**
     * call API service to return products using Additives
     *
     * @param additive search query for products
     * @param page number of pages
     */
    fun getProductsByAdditive(additive: String, page: Int) = rxSingle(IO) {
        rawApi.getProductsByAdditive(additive, page, fieldsToFetchFacets)
    }

    fun getProductsByAllergen(allergen: String, page: Int) = rxSingle(IO) {
        rawApi.getProductsByAllergen(allergen, page, fieldsToFetchFacets)
    }

    fun getToBeCompletedProductsByContributor(contributor: String, page: Int) = rxSingle(IO) {
        rawApi.getToBeCompletedProductsByContributor(contributor, page)
    }

    fun getPicturesContributedProducts(contributor: String, page: Int) = rxSingle(IO) {
        rawApi.getPicturesContributedProducts(contributor, page)
    }

    fun getPicturesContributedIncompleteProducts(contributor: String?, page: Int) = rxSingle(IO) {
        rawApi.getPicturesContributedIncompleteProducts(contributor, page)
    }

    fun getInfoAddedProducts(contributor: String?, page: Int) = rxSingle(IO) {
        rawApi.getInfoAddedProducts(contributor, page)
    }


    fun getIncompleteProducts(page: Int) = rxSingle(IO) {
        rawApi.getIncompleteProducts(page, fieldsToFetchFacets)
    }

    fun getProductsByStates(state: String?, page: Int) = rxSingle(IO) {
        rawApi.getProductsByState(state, page, fieldsToFetchFacets)
    }

    companion object {
        val MIME_TEXT: MediaType = MediaType.get("text/plain")
        const val PNG_EXT = ".png"

        suspend fun HistoryProductDao.addToHistory(prod: OfflineSavedProduct): Unit = withContext(IO) {
            val savedProduct = unique {
                where(HistoryProductDao.Properties.Barcode.eq(prod.barcode))
            }

            val details = prod.productDetails
            val hp = HistoryProduct(
                prod.name,
                details[Keys.ADD_BRANDS],
                prod.imageFrontLocalUrl,
                prod.barcode,
                details[Keys.QUANTITY],
                details[Keys.NUTRITION_GRADE_FR],
                details[Keys.ECOSCORE],
                details[Keys.NOVA_GROUPS]
            )
            if (savedProduct != null) hp.id = savedProduct.id
            insertOrReplace(hp)
        }

        /**
         * Add a product to ScanHistory synchronously
         */
        suspend fun HistoryProductDao.addToHistory(product: Product, language: String): Unit =
            withContext(IO) {

                val savedProduct: HistoryProduct? = unique {
                    where(HistoryProductDao.Properties.Barcode.eq(product.code))
                }

                val hp = HistoryProduct(
                    product.productName,
                    product.brands,
                    product.getImageSmallUrl(language),
                    product.code,
                    product.quantity,
                    product.nutritionGradeFr,
                    product.ecoscore,
                    product.novaGroups
                )

                if (savedProduct != null) hp.id = savedProduct.id
                insertOrReplace(hp)

                return@withContext
            }
    }

    /**
     * Return a [Map] with user info (username, password, comment)
     */
    private fun getUserInfo(): Map<String, String> {
        val imgMap = mutableMapOf<String, String>()

        val settings = context.getLoginPreferences()
        val userName = settings.getString("user", null)
        val userPassword = settings.getString("pass", null)

        if (userName?.isNotBlank() == true && userPassword?.isNotBlank() == true) {
            imgMap[Keys.USER_COMMENT] = getCommentToUpload(userName)
            imgMap[Keys.USER_ID] = userName
            imgMap[Keys.USER_PASS] = userPassword
        }

        return imgMap
    }

    /**
     * Uploads comment by users
     *
     * @param login the username
     */
    fun getCommentToUpload(login: String? = null) = buildString {
        append(
            when (BuildConfig.FLAVOR) {
                OBF -> StringBuilder("Official Open Beauty Facts Android app")
                OPFF -> StringBuilder("Official Open Pet Food Facts Android app")
                OPF -> StringBuilder("Official Open Products Facts Android app")
                OFF -> StringBuilder("Official Open Food Facts Android app")
                else -> StringBuilder("Official Open Food Facts Android app")
            }
        )
        append(" ")
        append(context.getVersionName())
        if (login.isNullOrEmpty()) {
            append(" (Added by ").append(installationService.id).append(")")
        }
    }

    val localeProductNameField get() = "product_name_${localeManager.getLanguage()}"

    private val fieldsToFetchFacets
        get() = (Keys.PRODUCT_SEARCH_FIELDS + localeProductNameField).joinToString(",")

    suspend fun getEMBCodeSuggestions(term: String) = rawApi.getSuggestions("emb_codes", term)
    suspend fun getPeriodAfterOpeningSuggestions(term: String) = rawApi.getSuggestions("periods_after_opening", term)
}

