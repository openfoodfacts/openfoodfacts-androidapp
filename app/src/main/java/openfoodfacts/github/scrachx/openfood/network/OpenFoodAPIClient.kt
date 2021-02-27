package openfoodfacts.github.scrachx.openfood.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.afollestad.materialdialogs.MaterialDialog
import com.fasterxml.jackson.databind.JsonNode
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.AnalyticsService
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_EDIT_PRODUCT
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.images.*
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProductDao
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession
import java.io.File
import java.io.IOException
import java.util.*
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.Companion.start as startProductViewActivity

/**
 * API Client for all API callbacks
 */
class OpenFoodAPIClient(private val context: Context) {
    private var historySyncDisp = CompositeDisposable()

    fun getProductStateFull(barcode: String, customHeader: String = Utils.HEADER_USER_AGENT_SEARCH): Single<ProductState> {
        AnalyticsService.setBarcode(barcode)
        return productsApi.getProductByBarcode(barcode, getAllFields(), getUserAgent(customHeader))
    }

    private fun getAllFields(): String {
        val allFields = ApiFields.Keys.PRODUCT_COMMON_FIELDS
        val fieldsToLocalize = ApiFields.Keys.PRODUCT_LOCAL_FIELDS

        val langCode = getLanguage(context)
        val fieldsSet = allFields.toMutableSet()
        fieldsToLocalize.forEach { (field, shouldAddEn) ->
            fieldsSet.add("${field}_$langCode")
            if (shouldAddEn) fieldsSet.add("${field}_en")
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
                                lang = getLanguage(activity)
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
        val fields = ApiFields.Keys.PRODUCT_IMAGES_FIELDS.toMutableSet().also {
            it += ApiFields.Keys.lcProductNameKey(getLanguage(context))
        }.joinToString(",")
        return productsApi.getProductByBarcode(
                barcode,
                fields,
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
            productsApi.getProductByBarcode(barcode, getAllFields(), getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
                    .doOnError {
                        if (it is IOException) {
                            Toast.makeText(activity, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                            return@doOnError
                        } else {
                            productNotFoundDialogBuilder(activity, barcode).show()
                        }
                    }
                    .subscribe { state ->
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
            productsApi.searchProductByName(name, getFieldsToFetchFacets(), page)

    /**
     * @param barcode
     * @return a single containing a list of product ingredients (can be empty)
     */
    // TODO: This or the field inside Product.kt?
    fun getIngredients(barcode: String?) = productsApi.getIngredientsByBarcode(barcode).map { productState ->
        productState["product"][ApiFields.Keys.INGREDIENTS]?.map {
            ProductIngredient(
                    it["id"].asText(),
                    it["text"].asText(),
                    it["rank"]?.asLong(-1)!!
            )
        } ?: emptyList()
    }

    fun getProductsByCountry(country: String, page: Int) =
            productsApi.getProductsByCountry(country, page, getFieldsToFetchFacets())

    /**
     * Returns a map for images uploaded for product/ingredients/nutrition/other images
     *
     * @param image object of ProductImage
     */
    private fun getUploadableMap(image: ProductImage): Map<String, RequestBody?> {
        val lang = image.language
        val imgMap = hashMapOf(PRODUCT_BARCODE to image.code, "imagefield" to image.field)
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
            imgMap[key] = RequestBody.create(MediaType.parse(MIME_TEXT), value)
        }
        return imgMap
    }

    fun getProductsByCategory(category: String, page: Int) =
            productsApi.getProductByCategory(category, page)

    fun getProductsByLabel(label: String, page: Int) =
            productsApi.getProductsByLabel(label, page, getFieldsToFetchFacets())

    /**
     * Add a product to ScanHistory asynchronously
     */
    fun addToHistory(product: Product) = Completable.fromAction { daoSession.historyProductDao.addToHistorySync(product) }

    fun getProductsByContributor(contributor: String, page: Int) =
            productsApi.getProductsByContributor(contributor, page).subscribeOn(Schedulers.io())

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
                    val productImage = ProductImage(product.barcode, product.productField, imageFile)
                    return@mapNotNull productsApi.saveImageSingle(getUploadableMap(productImage))
                            .flatMapCompletable { jsonNode: JsonNode? ->
                                if (jsonNode != null) {
                                    Log.d("onResponse", jsonNode.toString())
                                    if (!jsonNode.isObject) {
                                        return@flatMapCompletable Completable.error(IOException("jsonNode is not an object"))
                                    } else if (jsonNode[ApiFields.Keys.STATUS].asText().contains(ApiFields.Defaults.STATUS_NOT_OK)) {
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
            productsApi.getProductsByPackaging(packaging, page, getFieldsToFetchFacets())

    fun getProductsByStore(store: String, page: Int): Single<Search> =
            productsApi.getProductByStores(store, page, getFieldsToFetchFacets())

    /**
     * Search for products using bran name
     *
     * @param brand search query for product
     * @param page page numbers
     */
    fun getProductsByBrand(brand: String, page: Int): Single<Search> =
            productsApi.getProductByBrands(brand, page, getFieldsToFetchFacets())

    fun postImg(image: ProductImage, setAsDefault: Boolean = false): Completable {
        return productsApi.saveImageSingle(getUploadableMap(image))
                .flatMapCompletable { body: JsonNode ->
                    if (!body.isObject) {
                        throw IOException("body is not an object")
                    } else {
                        if (body[ApiFields.Keys.STATUS].asText().contains(ApiFields.Defaults.STATUS_NOT_OK)) {
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
        return productsApi.editImageSingle(image.barcode, addUserInfo(queryMap))
                .flatMapCompletable { jsonNode: JsonNode ->
                    if ("status ok" == jsonNode[ApiFields.Keys.STATUS].asText()) {
                        return@flatMapCompletable Completable.complete()
                    } else {
                        throw IOException(jsonNode["error"].asText())
                    }
                }
    }

    fun editImage(code: String, imgMap: MutableMap<String, String>) = productsApi.editImages(code, addUserInfo(imgMap))

    /**
     * Unselect the image from the product code.
     *
     * @param code code of the product
     */
    fun unSelectImage(code: String, field: ProductImageField, language: String): Single<String> {
        val imgMap = hashMapOf(IMAGE_STRING_ID to getImageStringKey(field, language))
        return productsApi.unSelectImage(code, addUserInfo(imgMap))
    }

    fun getProductsByOrigin(origin: String, page: Int) =
            productsApi.getProductsByOrigin(origin, page, getFieldsToFetchFacets())

    fun syncOldHistory() {
        val fields = "image_small_url,product_name,brands,quantity,image_url,nutrition_grade_fr,code"
        historySyncDisp.clear()
        daoSession.historyProductDao.loadAll().forEach { historyProduct ->
            productsApi.getProductByBarcode(historyProduct.barcode, fields, getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
                    .map { state ->
                        if (state.status != 0L) {
                            val product = state.product!!
                            val hp = HistoryProduct(
                                    product.productName,
                                    product.brands,
                                    product.getImageSmallUrl(getLanguage(context)),
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
                        context.getSharedPreferences("prefs", 0).edit {
                            putBoolean("is_old_history_data_synced", true)
                        }
                    }.ignoreElement().subscribe().addTo(historySyncDisp)
        }
    }

    fun getInfoAddedIncompleteProductsSingle(contributor: String, page: Int) =
            productsApi.getInfoAddedIncompleteProductsSingle(contributor, page)

    fun getProductsByManufacturingPlace(manufacturingPlace: String, page: Int) =
            productsApi.getProductsByManufacturingPlace(manufacturingPlace, page, getFieldsToFetchFacets())

    /**
     * call API service to return products using Additives
     *
     * @param additive search query for products
     * @param page number of pages
     */
    fun getProductsByAdditive(additive: String, page: Int) =
            productsApi.getProductsByAdditive(additive, page, getFieldsToFetchFacets())

    fun getProductsByAllergen(allergen: String, page: Int) =
            productsApi.getProductsByAllergen(allergen, page, getFieldsToFetchFacets())

    fun getToBeCompletedProductsByContributor(contributor: String, page: Int) =
            productsApi.getToBeCompletedProductsByContributor(contributor, page)

    fun getPicturesContributedProducts(contributor: String, page: Int) =
            productsApi.getPicturesContributedProducts(contributor, page)

    fun getPicturesContributedIncompleteProducts(contributor: String?, page: Int) =
            productsApi.getPicturesContributedIncompleteProducts(contributor, page)

    fun getInfoAddedProducts(contributor: String?, page: Int) = productsApi.getInfoAddedProducts(contributor, page)

    fun getIncompleteProducts(page: Int) = productsApi.getIncompleteProducts(page, getFieldsToFetchFacets())

    fun getProductsByStates(state: String?, page: Int) = productsApi.getProductsByState(state, page, getFieldsToFetchFacets())

    companion object {
        const val MIME_TEXT = "text/plain"
        const val PNG_EXT = ".png\""


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
            comment.append(" ").append(getVersionName(OFFApplication.instance))
            if (login.isNullOrEmpty()) {
                comment.append(" (Added by ").append(InstallationUtils.id(OFFApplication.instance)).append(")")
            }
            return comment.toString()
        }

        fun getLocaleProductNameField() = "product_name_${getLanguage(OFFApplication.instance)}"

        /**
         * Add a product to ScanHistory synchronously
         */
        fun HistoryProductDao.addToHistorySync(product: Product) {
            val historyProducts = queryBuilder()
                    .where(HistoryProductDao.Properties.Barcode.eq(product.code))
                    .list()
            val hp = HistoryProduct(
                    product.productName,
                    product.brands,
                    product.getImageSmallUrl(getLanguage(OFFApplication.instance)),
                    product.code,
                    product.quantity,
                    product.nutritionGradeFr,
                    product.ecoscore,
                    product.novaGroups
            )
            if (historyProducts.isNotEmpty()) hp.id = historyProducts[0].id
            insertOrReplace(hp)
        }

        fun HistoryProductDao.addToHistorySync(product: OfflineSavedProduct) {
            val historyProducts = queryBuilder().where(HistoryProductDao.Properties.Barcode.eq(product.barcode)).list()
            val productDetails = product.productDetails
            val hp = HistoryProduct(
                    product.name,
                    productDetails[ApiFields.Keys.ADD_BRANDS],
                    product.imageFrontLocalUrl,
                    product.barcode,
                    productDetails[ApiFields.Keys.QUANTITY],
                    null,
                    null,
                    null
            )
            if (historyProducts.isNotEmpty()) hp.id = historyProducts[0].id
            insertOrReplace(hp)
        }

        /**
         * Fill the given [Map] with user info (username, password, comment)
         *
         * @param imgMap The map to fill
         */
        fun addUserInfo(imgMap: MutableMap<String, String> = mutableMapOf()): Map<String, String> {
            val settings = OFFApplication.instance.getLoginPreferences()

            settings.getString("user", null)?.let {
                imgMap[ApiFields.Keys.USER_COMMENT] = getCommentToUpload(it)
                if (it.isNotBlank()) imgMap[ApiFields.Keys.USER_ID] = it
            }

            settings.getString("pass", null)?.let {
                if (it.isNotBlank()) imgMap[ApiFields.Keys.USER_PASS] = it
            }

            return imgMap
        }

        private fun getFieldsToFetchFacets() = listOf(
                ApiFields.Keys.BRANDS,
                ApiFields.Keys.PRODUCT_NAME,
                ApiFields.Keys.IMAGE_SMALL_URL,
                ApiFields.Keys.QUANTITY,
                ApiFields.Keys.NUTRITION_GRADE_FR,
                ApiFields.Keys.BARCODE,
                ApiFields.Keys.ECOSCORE,
                ApiFields.Keys.NOVA_GROUPS,
                getLocaleProductNameField()
        ).joinToString(",")
    }
}
