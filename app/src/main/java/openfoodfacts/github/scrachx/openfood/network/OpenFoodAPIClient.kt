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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
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
        private val rawApi: ProductsAPI,
        private val sentryAnalytics: SentryAnalytics,
        private val localeManager: LocaleManager
) {
    private var historySyncDisp = CompositeDisposable()

    fun getProductStateFull(
            barcode: String,
            fields: String = getAllFields(),
            userAgent: String = Utils.HEADER_USER_AGENT_SEARCH
    ): Single<ProductState> {
        sentryAnalytics.setBarcode(barcode)
        return rawApi.getProductByBarcode(barcode, fields, localeManager.getLanguage(), getUserAgent(userAgent))
    }

    fun getProductsByBarcode(
            codes: List<String>,
            customHeader: String = Utils.HEADER_USER_AGENT_SEARCH
    ): Single<List<SearchProduct>> {
        return rawApi.getProductsByBarcode(codes.joinToString(","), getAllFields(), customHeader)
                .map { it.products }
    }

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
    fun getProductImages(barcode: String): Single<ProductState> {
        val fields = Keys.PRODUCT_IMAGES_FIELDS.toMutableSet().also {
            it += Keys.lcProductNameKey(localeManager.getLanguage())
        }.joinToString(",")
        return rawApi.getProductByBarcode(
                barcode,
                fields,
                localeManager.getLanguage(),
                getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
        )
    }

    // TODO: This is not part of the client, move it to another class (preferably a utility class)
    /**
     * Open the product in [ProductViewActivity] if the barcode exist.
     * Also add it in the history if the product exist.
     *
     * @param barcode product barcode
     * @param activity
     */
    fun openProduct(barcode: String, activity: Activity): Disposable =
            rawApi.getProductByBarcode(
                    barcode,
                    getAllFields(),
                    localeManager.getLanguage(),
                    getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
            ).doOnError {
                if (it is IOException) {
                    Toast.makeText(activity, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                } else {
                    productNotFoundDialogBuilder(activity, barcode).show()
                }
            }.subscribe { state ->
                if (state.status == 0L) {
                    productNotFoundDialogBuilder(activity, barcode)
                            .onNegative { _, _ -> activity.onBackPressed() }
                            .show()
                } else {
                    addToHistory(state.product!!).subscribe()
                    startProductViewActivity(activity, state)
                }
            }

    fun getIngredients(product: Product) = getIngredients(product.code)


    fun searchProductsByName(name: String, page: Int) =
            rawApi.searchProductByName(name, fieldsToFetchFacets, page)

    /**
     * @param barcode
     * @return a single containing a list of product ingredients (can be empty)
     */
    // TODO: This or the field inside Product.kt?
    fun getIngredients(barcode: String?) = rawApi.getIngredientsByBarcode(barcode).map { productState ->
        productState["product"][Keys.INGREDIENTS]?.map {
            ProductIngredient(
                    it["id"].asText(),
                    it["text"].asText(),
                    it["rank"]?.asLong(-1)!!
            )
        } ?: emptyList()
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
        val imgMap = hashMapOf(PRODUCT_BARCODE to image.codeBody, "imagefield" to image.fieldBody)
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
        addUserInfo().forEach { (key, value) ->
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
    fun addToHistory(product: Product) = Completable.fromAction { daoSession.historyProductDao.addToHistorySync(product, localeManager.getLanguage()) }

    fun getProductsByContributor(contributor: String, page: Int) =
            rawApi.getProductsByContributor(contributor, page, fieldsToFetchFacets).subscribeOn(Schedulers.io())

    /**
     * upload images in offline mode
     *
     * @return ListenableFuture
     */
    fun uploadOfflineImages() = Single.fromCallable {
        daoSession.toUploadProductDao.queryBuilder()
                .where(ToUploadProductDao.Properties.Uploaded.eq(false))
                .list()
                .mapNotNull { product ->
                    val imageFile = try {
                        File(product.imageFilePath)
                    } catch (e: Exception) {
                        Log.e("OfflineUploadingTask", "doInBackground", e)
                        return@mapNotNull null
                    }
                    val productImage = ProductImage(product.barcode, product.productField, imageFile, localeManager.getLanguage())
                    return@mapNotNull rawApi.saveImage(getUploadableMap(productImage))
                            .flatMapCompletable { jsonNode: JsonNode? ->
                                if (jsonNode != null) {
                                    Log.d("onResponse", jsonNode.toString())
                                    if (!jsonNode.isObject) {
                                        return@flatMapCompletable Completable.error(IOException("jsonNode is not an object"))
                                    } else if (jsonNode[Keys.STATUS].asText().contains(ApiFields.Defaults.STATUS_NOT_OK)) {
                                        daoSession.toUploadProductDao.delete(product)
                                        return@flatMapCompletable Completable.error(IOException(ApiFields.Defaults.STATUS_NOT_OK))
                                    } else {
                                        daoSession.toUploadProductDao.delete(product)
                                        return@flatMapCompletable Completable.complete()
                                    }
                                } else {
                                    return@flatMapCompletable Completable.error(IOException("jsonNode is null"))
                                }
                            }
                }
    }.flatMapCompletable { Completable.merge(it) }

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
    fun getProductsByBrand(brand: String, page: Int): Single<Search> =
            rawApi.getProductByBrands(brand, page, fieldsToFetchFacets)

    fun postImg(image: ProductImage, setAsDefault: Boolean = false): Completable {
        return rawApi.saveImage(getUploadableMap(image))
                .flatMapCompletable { body: JsonNode ->
                    if (!body.isObject) {
                        throw IOException("body is not an object")
                    } else {
                        if (body[Keys.STATUS].asText().contains(ApiFields.Defaults.STATUS_NOT_OK)) {
                            throw IOException(body["error"].asText())
                        } else {
                            if (setAsDefault) {
                                setDefaultImageFromServerResponse(body, image)
                            } else {
                                Completable.complete()
                            }
                        }
                    }
                }.doOnError {
                    daoSession.toUploadProductDao.insertOrReplace(ToUploadProduct(
                            image.barcode,
                            image.filePath,
                            image.imageField.toString()
                    ))
                }
    }

    private fun setDefaultImageFromServerResponse(body: JsonNode, image: ProductImage): Completable {
        val queryMap = hashMapOf(
                IMG_ID to body["image"][IMG_ID].asText(),
                "id" to body["imagefield"].asText()
        )
        return rawApi.editImage(image.barcode, addUserInfo(queryMap))
                .flatMapCompletable { jsonNode: JsonNode ->
                    if ("status ok" == jsonNode[Keys.STATUS].asText()) {
                        return@flatMapCompletable Completable.complete()
                    } else {
                        throw IOException(jsonNode["error"].asText())
                    }
                }
    }

    fun editImage(code: String, imgMap: MutableMap<String, String>) = rawApi.editImages(code, addUserInfo(imgMap))

    /**
     * Unselect the image from the product code.
     *
     * @param code code of the product
     */
    fun unSelectImage(code: String, field: ProductImageField, language: String): Single<String> {
        val imgMap = hashMapOf(IMAGE_STRING_ID to getImageStringKey(field, language))
        return rawApi.unSelectImage(code, addUserInfo(imgMap))
    }

    fun getProductsByOrigin(origin: String, page: Int) =
            rawApi.getProductsByOrigin(origin, page, fieldsToFetchFacets)

    fun syncOldHistory() {
        val fields = listOf(
                Keys.IMAGE_SMALL_URL,
                Keys.PRODUCT_NAME,
                Keys.BRANDS,
                Keys.QUANTITY,
                IMAGE_URL,
                Keys.NUTRITION_GRADE_FR,
                Keys.BARCODE
        ).joinToString(",")
        historySyncDisp.clear()

        Single.fromCallable { daoSession.historyProductDao.loadAll() }
                .flatMapObservable { it.toObservable() }
                .flatMapCompletable { historyProduct ->
                    rawApi.getProductByBarcode(
                            historyProduct.barcode,
                            fields,
                            localeManager.getLanguage(),
                            getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)
                    ).flatMapCompletable { state ->
                        if (state.status != 0L) {
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
                            Completable.complete()
                        } else Completable.error(IOException("Could not sync history. Error with product ${state.code} "))

                    }
                }.subscribe {
                    context.getSharedPreferences("prefs", 0).edit {
                        putBoolean("is_old_history_data_synced", true)
                    }
                }.addTo(historySyncDisp)
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
        fun HistoryProductDao.addToHistorySync(product: OfflineSavedProduct) {
            val savedProduct = queryBuilder().where(HistoryProductDao.Properties.Barcode.eq(product.barcode)).uniqueOrThrow()
            val details = product.productDetails
            val hp = HistoryProduct(
                    product.name,
                    details[Keys.ADD_BRANDS],
                    product.imageFrontLocalUrl,
                    product.barcode,
                    details[Keys.QUANTITY],
                    null,
                    null,
                    null
            )
            if (savedProduct != null) hp.id = savedProduct.id
            insertOrReplace(hp)
        }

        /**
         * Add a product to ScanHistory synchronously
         */
        fun HistoryProductDao.addToHistorySync(product: Product, language: String) {
            val historyProducts = queryBuilder()
                    .where(HistoryProductDao.Properties.Barcode.eq(product.code))
                    .uniqueOrThrow()
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
            if (historyProducts != null) hp.id = historyProducts.id
            insertOrReplace(hp)
        }
    }

    /**
     * Fill the given [Map] with user info (username, password, comment)
     *
     * @param imgMap The map to fill
     */
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
}
