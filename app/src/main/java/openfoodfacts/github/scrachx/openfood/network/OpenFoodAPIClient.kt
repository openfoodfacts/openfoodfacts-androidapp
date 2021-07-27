package openfoodfacts.github.scrachx.openfood.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.afollestad.materialdialogs.MaterialDialog
import com.fasterxml.jackson.databind.JsonNode
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.SentryAnalytics
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_EDIT_PRODUCT
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.images.*
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProductDao
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Keys
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.Companion.start as startProductViewActivity

/**
 * API Client for all API callbacks
 */
@Singleton
class OpenFoodAPIClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val daoSession: DaoSession,
    internal val rawApi: ProductsAPI,
    private val sentryAnalytics: SentryAnalytics,
    private val localeManager: LocaleManager
) {

    suspend fun getProductStateFull(
        barcode: String,
        fields: String = getAllFields(),
        userAgent: String = Utils.HEADER_USER_AGENT_SEARCH
    ): ProductState {
        sentryAnalytics.setBarcode(barcode)
        return withContext(IO) {
            rawApi.getProductByBarcode(barcode, fields, localeManager.getLanguage(), getUserAgent(userAgent)).await()
        }
    }

    suspend fun getProductsByBarcode(
        codes: List<String>,
        customHeader: String = Utils.HEADER_USER_AGENT_SEARCH
    ) = rawApi.getProductsByBarcode(
        codes.joinToString(","),
        getAllFields(),
        customHeader
    ).products

    private fun getAllFields(): String {
        val allFields = Keys.PRODUCT_COMMON_FIELDS
        val fieldsToLocalize = Keys.PRODUCT_LOCAL_FIELDS

        val langCode = localeManager.getLanguage()
        val fieldsSet = allFields.toMutableSet()
        fieldsToLocalize.forEach { (field, shouldAddEn) ->
            fieldsSet += "${field}_$langCode"
            if (shouldAddEn) fieldsSet += "${field}_en"
        }
        return fieldsSet.joinToString(",")
    }

    private fun productNotFoundDialogBuilder(activity: Activity, barcode: String): MaterialDialog.Builder =
        MaterialDialog.Builder(activity)
            .title(R.string.txtDialogsTitle)
            .content(R.string.txtDialogsContent)
            .positiveText(R.string.txtYes)
            .negativeText(R.string.txtNo)
            .onPositive { _, _ ->
                activity.startActivity(Intent(activity, ProductEditActivity::class.java).apply {
                    putExtra(KEY_EDIT_PRODUCT, Product().apply {
                        code = barcode
                        lang = localeManager.getLanguage()
                    })
                })
                activity.finish()
            }

    /**
     * Open the product activity if the barcode exist.
     * Also add it in the history if the product exist.
     *
     * @param barcode product barcode
     */
    fun getProductImages(barcode: String): Single<ProductState> = rxSingle(IO) {
        val fields = Keys.PRODUCT_IMAGES_FIELDS.toMutableSet().also {
            it += Keys.lcProductNameKey(localeManager.getLanguage())
        }.joinToString(",")
        return@rxSingle rawApi.getProductByBarcode(
            barcode,
            fields,
            localeManager.getLanguage(),
            getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
        ).await()
    }

    // TODO: This is not part of the client, move it to another class (preferably a utility class)
    /**
     * Open the product in [ProductViewActivity] if the barcode exist.
     * Also add it in the history if the product exist.
     *
     * @param barcode product barcode
     * @param activity
     */
    suspend fun openProduct(barcode: String, activity: Activity) {
        val state = try {
            rawApi.getProductByBarcode(
                barcode,
                getAllFields(),
                localeManager.getLanguage(),
                getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
            ).await()
        } catch (err: Exception) {
            when (err) {
                is IOException -> Toast.makeText(activity, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                else -> productNotFoundDialogBuilder(activity, barcode).show()
            }
            return
        }
        withContext(Main) {
            if (state.status == 0L) {
                productNotFoundDialogBuilder(activity, barcode)
                    .onNegative { _, _ -> activity.onBackPressed() }
                    .show()
            } else {
                addToHistory(state.product!!).subscribe()
                startProductViewActivity(activity, state)
            }
        }
    }

    /**
     * @param barcode
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


    fun searchProductsByName(name: String, page: Int) = rxSingle {
        rawApi.searchProductByName(name, fieldsToFetchFacets, page)
    }

    fun getProductsByCountry(country: String, page: Int) =
        rawApi.getProductsByCountry(country, page, fieldsToFetchFacets)

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

    fun getProductsByCategory(category: String, page: Int) =
        rawApi.getProductByCategory(category, page, fieldsToFetchFacets)

    fun getProductsByLabel(label: String, page: Int) =
        rawApi.getProductsByLabel(label, page, fieldsToFetchFacets)

    /**
     * Add a product to ScanHistory asynchronously
     */
    fun addToHistory(product: Product) = rxCompletable(IO) {
        daoSession.historyProductDao.addToHistory(product, localeManager.getLanguage())
    }

    fun getProductsByContributor(contributor: String, page: Int) =
        rawApi.getProductsByContributor(contributor, page, fieldsToFetchFacets)
            .subscribeOn(Schedulers.io())

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

    fun getProductsByPackaging(packaging: String, page: Int): Single<Search> =
        rawApi.getProductsByPackaging(packaging, page, fieldsToFetchFacets)

    fun getProductsByStore(store: String, page: Int): Single<Search> =
        rawApi.getProductByStores(store, page, fieldsToFetchFacets)

    /**
     * Search for products using bran name
     *
     * @param brand search query for product
     * @param page page numbers
     */
    fun getProductsByBrand(brand: String, page: Int): Single<Search> = rxSingle(IO) {
        rawApi.getProductByBrands(brand, page, fieldsToFetchFacets)
    }

    fun postImg(image: ProductImage, setAsDefault: Boolean = false) = rxCompletable {
        try {
            val body = rawApi.saveImage(getUploadableMap(image))

            when {
                !body.isObject -> {
                    throw IOException("body is not an object")
                }
                ApiFields.Defaults.STATUS_NOT_OK in body[Keys.STATUS].asText() -> {
                    throw IOException(body["error"].asText())
                }
                setAsDefault -> setDefaultImageFromServerResponse(body, image)
            }
        } catch (err: Exception) {
            daoSession.toUploadProductDao.insertOrReplace(
                ToUploadProduct(
                    image.barcode,
                    image.filePath,
                    image.imageField.toString()
                )
            )
        }
    }

    private fun setDefaultImageFromServerResponse(body: JsonNode, image: ProductImage): Completable {
        val queryMap = getUserInfo() + listOf(
            IMG_ID to body["image"][IMG_ID].asText(),
            "id" to body["imagefield"].asText()
        )

        return rawApi.editImage(image.barcode, queryMap).flatMapCompletable { node ->
            if (node[Keys.STATUS].asText() != "status ok") throw IOException(node["error"].asText())
            else Completable.complete()
        }
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
        return@withContext rawApi.unSelectImage(code, imgMap)
    }

    fun getProductsByOrigin(origin: String, page: Int) =
        rawApi.getProductsByOrigin(origin, page, fieldsToFetchFacets)

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
            ).await()

            if (state.status == 0L) throw IOException("Could not sync history. Error with product ${state.code} ")
            else {
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


        context.getSharedPreferences("prefs", 0).edit {
            putBoolean("is_old_history_data_synced", true)
        }
    }

    fun getInfoAddedIncompleteProductsSingle(contributor: String, page: Int) =
        rawApi.getInfoAddedIncompleteProductsSingle(contributor, page)

    fun getProductsByManufacturingPlace(manufacturingPlace: String, page: Int) =
        rawApi.getProductsByManufacturingPlace(manufacturingPlace, page, fieldsToFetchFacets)

    /**
     * call API service to return products using Additives
     *
     * @param additive search query for products
     * @param page number of pages
     */
    fun getProductsByAdditive(additive: String, page: Int) =
        rawApi.getProductsByAdditive(additive, page, fieldsToFetchFacets)

    fun getProductsByAllergen(allergen: String, page: Int) =
        rawApi.getProductsByAllergen(allergen, page, fieldsToFetchFacets)

    fun getToBeCompletedProductsByContributor(contributor: String, page: Int) =
        rawApi.getToBeCompletedProductsByContributor(contributor, page)

    fun getPicturesContributedProducts(contributor: String, page: Int) =
        rawApi.getPicturesContributedProducts(contributor, page)

    fun getPicturesContributedIncompleteProducts(contributor: String?, page: Int) =
        rawApi.getPicturesContributedIncompleteProducts(contributor, page)

    fun getInfoAddedProducts(contributor: String?, page: Int) =
        rawApi.getInfoAddedProducts(contributor, page)

    fun getIncompleteProducts(page: Int) =
        rawApi.getIncompleteProducts(page, fieldsToFetchFacets)

    fun getProductsByStates(state: String?, page: Int) =
        rawApi.getProductsByState(state, page, fieldsToFetchFacets)

    companion object {
        val MIME_TEXT: MediaType = MediaType.get("text/plain")
        const val PNG_EXT = ".png"

        suspend fun HistoryProductDao.addToHistory(newProd: OfflineSavedProduct): Unit = withContext(IO) {
            val savedProduct: HistoryProduct? = queryBuilder()
                .where(HistoryProductDao.Properties.Barcode.eq(newProd.barcode))
                .unique()

            val details = newProd.productDetails
            val hp = HistoryProduct(
                newProd.name,
                details[Keys.ADD_BRANDS],
                newProd.imageFrontLocalUrl,
                newProd.barcode,
                details[Keys.QUANTITY],
                details[Keys.NUTRITION_GRADE_FR],
                details[Keys.ECOSCORE],
                details[Keys.NOVA_GROUPS
                ]
            )
            if (savedProduct != null) hp.id = savedProduct.id
            insertOrReplace(hp)

            return@withContext
        }

        /**
         * Add a product to ScanHistory synchronously
         */
        suspend fun HistoryProductDao.addToHistory(product: Product, language: String): Unit =
            withContext(IO) {

                val savedProduct: HistoryProduct? = queryBuilder()
                    .where(HistoryProductDao.Properties.Barcode.eq(product.code))
                    .unique()

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
     * Fill the given [Map] with user info (username, password, comment)
     *
     * @param imgMap The map to fill
     *
     */
    @Deprecated("Use the += operator with getUserInfo()")
    private fun addUserInfo(imgMap: MutableMap<String, String> = mutableMapOf()): Map<String, String> {
        val settings = context.getLoginPreferences()

        settings.getString("user", null)?.let {
            imgMap[Keys.USER_COMMENT] = getCommentToUpload(it)
            if (it.isNotBlank()) imgMap[Keys.USER_ID] = it
        }

        settings.getString("pass", null)?.let {
            if (it.isNotBlank()) imgMap[Keys.USER_PASS] = it
        }

        return imgMap
    }

    private fun getUserInfo() = addUserInfo()

    /**
     * Uploads comment by users
     *
     * @param login the username
     */
    fun getCommentToUpload(login: String? = null): String {
        val comment = when (BuildConfig.FLAVOR) {
            OBF -> StringBuilder("Official Open Beauty Facts Android app")
            OPFF -> StringBuilder("Official Open Pet Food Facts Android app")
            OPF -> StringBuilder("Official Open Products Facts Android app")
            OFF -> StringBuilder("Official Open Food Facts Android app")
            else -> StringBuilder("Official Open Food Facts Android app")
        }
        comment.append(" ").append(context.getVersionName())
        if (login.isNullOrEmpty()) {
            comment.append(" (Added by ").append(InstallationUtils.id(context)).append(")")
        }
        return comment.toString()
    }

    val localeProductNameField get() = "product_name_${localeManager.getLanguage()}"

    private val fieldsToFetchFacets
        get() = Keys.PRODUCT_SEARCH_FIELDS.toMutableList().apply {
            add(localeProductNameField)
        }.joinToString(",")

    suspend fun getEMBCodeSuggestions(term: String) = rawApi.getSuggestions("emb_codes", term)
    suspend fun getPeriodAfterOpeningSuggestions(term: String) = rawApi.getSuggestions("periods_after_opening", term)
}
