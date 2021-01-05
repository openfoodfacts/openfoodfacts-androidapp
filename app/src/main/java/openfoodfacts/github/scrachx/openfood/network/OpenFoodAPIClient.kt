package openfoodfacts.github.scrachx.openfood.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.edit
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.images.IMAGE_STRING_ID
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.images.getImageStringKey
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProductDao
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession
import openfoodfacts.github.scrachx.openfood.utils.Utils.httpClientBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.IOException
import java.util.*
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.Companion.start as startProductViewActivity

/**
 * API Client for all API callbacks
 */
class OpenFoodAPIClient @JvmOverloads constructor(
        private val context: Context,
        customEndpointUrl: String? = null
) {
    private var historySyncDisp = CompositeDisposable()

    /**
     * @return This api service gets products of provided brand.
     */
    val rawAPI: ProductsAPI = if (customEndpointUrl == null) productsApi
    else {
        Retrofit.Builder()
                .baseUrl(customEndpointUrl)
                .client(httpClientBuilder())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(ProductsAPI::class.java)
    }

    fun getProductStateFull(barcode: String, customHeader: String = Utils.HEADER_USER_AGENT_SEARCH) =
            rawAPI.getProductByBarcodeSingle(barcode, getAllFields(), getUserAgent(customHeader))

    private fun getAllFields(): String {
        val allFields = context.resources.getStringArray(R.array.product_all_fields_array)
        val fieldsToLocalize = context.resources.getStringArray(R.array.fields_array)
        val langCode = getLanguage(OFFApplication.instance.applicationContext)

        val fields = allFields.toMutableSet()
        fieldsToLocalize.forEach { fieldToLocalize ->
            fields.add("${fieldToLocalize}_$langCode")
            fields.add("${fieldToLocalize}_en")
        }
        return fields.joinToString(",")
    }

    fun productNotFoundDialogBuilder(activity: Activity, barcode: String): MaterialDialog.Builder =
            MaterialDialog.Builder(activity)
                    .title(R.string.txtDialogsTitle)
                    .content(R.string.txtDialogsContent)
                    .positiveText(R.string.txtYes)
                    .negativeText(R.string.txtNo)
                    .onPositive { _: MaterialDialog?, _: DialogAction? ->
                        activity.startActivity(Intent(activity, ProductEditActivity::class.java).apply {
                            putExtra(KEY_STATE, ProductState().apply {
                                product = Product().apply { code = barcode }
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
        val fieldsSet = OFFApplication.instance.resources.getStringArray(R.array.product_images_fields_array)
                .toMutableSet() + "product_name_${getLanguage(OFFApplication.instance.applicationContext)}"
        return rawAPI.getProductByBarcodeSingle(
                barcode,
                fieldsSet.joinToString(","),
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
            rawAPI.getProductByBarcodeSingle(barcode, getAllFields(), getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
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
                            addToHistory(state.product!!).blockingAwait()
                            startProductViewActivity(activity, state)
                        }
                    }

    fun getIngredients(product: Product) = getIngredients(product.code)

    fun searchProductsByName(name: String?, page: Int): Single<Search> {
        val fields = "selected_images,image_small_url,product_name,brands,quantity,code,nutrition_grade_fr,$localeProductNameField"
        return rawAPI.searchProductByName(fields, name, page)
    }

    /**
     * @param barcode
     * @return a single containing a list of product ingredients (can be empty)
     */
    fun getIngredients(barcode: String?): Single<List<ProductIngredient>> {
        return rawAPI.getIngredientsByBarcode(barcode).map { status: JsonNode ->
            status["product"]["ingredients"]?.map { ingredient ->
                ProductIngredient().apply {
                    id = ingredient["id"].asText()
                    text = ingredient["text"].asText()
                    val rankNode = ingredient["rank"]
                    rank = rankNode?.asLong(-1) ?: -1
                }
            } ?: emptyList()
        }
    }

    fun getProductsByCountry(country: String?, page: Int) =
            rawAPI.getProductsByCountry(country, page, fieldsToFetchFacets).subscribeOn(Schedulers.io())

    /**
     * Returns a map for images uploaded for product/ingredients/nutrition/other images
     *
     * @param image object of ProductImage
     */
    private fun getUploadableMap(image: ProductImage): Map<String, RequestBody?> {
        val lang = image.language
        val imgMap = hashMapOf("code" to image.code, "imagefield" to image.field)
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

    fun getProductsByCategory(category: String?, page: Int) =
            rawAPI.getProductByCategory(category, page)

    fun getProductsByLabel(label: String?, page: Int) =
            rawAPI.getProductByLabel(label, page, fieldsToFetchFacets)

    /**
     * Add a product to ScanHistory asynchronously
     */
    fun addToHistory(product: Product) = Completable.fromAction { addToHistorySync(daoSession.historyProductDao, product) }

    fun getProductsByContributor(contributor: String?, page: Int) =
            rawAPI.searchProductsByContributor(contributor, page).subscribeOn(Schedulers.io())

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
                    return@mapNotNull rawAPI.saveImageSingle(getUploadableMap(productImage))
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

    fun getProductsByPackaging(packaging: String?, page: Int): Single<Search> =
            rawAPI.getProductByPackaging(packaging, page, fieldsToFetchFacets)

    fun getProductsByStore(store: String?, page: Int): Single<Search> =
            rawAPI.getProductByStores(store, page, fieldsToFetchFacets)

    /**
     * Search for products using bran name
     *
     * @param brand search query for product
     * @param page page numbers
     */
    fun getProductsByBrand(brand: String?, page: Int): Single<Search> =
            rawAPI.getProductByBrandsSingle(brand, page, fieldsToFetchFacets)

    @JvmOverloads
    fun postImg(image: ProductImage, setAsDefault: Boolean = false): Completable {
        return rawAPI.saveImageSingle(getUploadableMap(image))
                .flatMapCompletable { body: JsonNode ->
                    if (body.isObject) {
                        if (!body[ApiFields.Keys.STATUS].asText().contains(ApiFields.Defaults.STATUS_NOT_OK)) {
                            if (setAsDefault) {
                                return@flatMapCompletable setDefaultImageFromServerResponse(body, image)
                            } else {
                                return@flatMapCompletable Completable.complete()
                            }
                        } else {
                            throw IOException(body["error"].asText())
                        }
                    } else {
                        throw IOException("body is not an object")
                    }
                }.doOnError {
                    val product = ToUploadProduct(image.barcode, image.filePath, image.imageField.toString())
                    daoSession.toUploadProductDao.insertOrReplace(product)
                }
    }

    private fun setDefaultImageFromServerResponse(body: JsonNode, image: ProductImage): Completable {
        val queryMap = hashMapOf(
                "imgid" to body["image"]["imgid"].asText(),
                "id" to body["imagefield"].asText()
        )
        return rawAPI.editImageSingle(image.barcode, addUserInfo(queryMap))
                .flatMapCompletable { jsonNode: JsonNode ->
                    if ("status ok" == jsonNode[ApiFields.Keys.STATUS].asText()) {
                        return@flatMapCompletable Completable.complete()
                    } else {
                        throw IOException(jsonNode["error"].asText())
                    }
                }
    }

    fun editImage(code: String?, imgMap: MutableMap<String, String?>): Single<String> {
        return rawAPI.editImages(code, addUserInfo(imgMap))
    }

    /**
     * Unselect the image from the product code.
     *
     * @param code code of the product
     */
    fun unSelectImage(code: String?, field: ProductImageField?, language: String?): Single<String> {
        val imgMap: MutableMap<String, String?> = HashMap()
        imgMap[IMAGE_STRING_ID] = getImageStringKey(field!!, language!!)
        return rawAPI.unSelectImage(code, addUserInfo(imgMap))
    }

    fun getProductsByOrigin(origin: String?, page: Int) =
            rawAPI.getProductsByOrigin(origin, page, fieldsToFetchFacets)

    fun syncOldHistory() {
        historySyncDisp.clear()
        daoSession.historyProductDao.loadAll().forEach { historyProduct ->
            rawAPI.getShortProductByBarcode(historyProduct.barcode, getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
                    .map { state ->
                        if (state.status != 0L) {
                            val product = state.product!!
                            val hp = HistoryProduct(
                                    product.productName,
                                    product.brands,
                                    product.getImageSmallUrl(getLanguage(OFFApplication.instance)),
                                    product.code,
                                    product.quantity,
                                    product.nutritionGradeFr
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

    fun getInfoAddedIncompleteProductsSingle(contributor: String?, page: Int) =
            rawAPI.getInfoAddedIncompleteProductsSingle(contributor, page)

    fun getProductsByManufacturingPlace(manufacturingPlace: String?, page: Int) =
            rawAPI.getProductsByManufacturingPlace(manufacturingPlace, page, fieldsToFetchFacets)

    /**
     * call API service to return products using Additives
     *
     * @param additive search query for products
     * @param page number of pages
     */
    fun getProductsByAdditive(additive: String?, page: Int) =
            rawAPI.getProductsByAdditive(additive, page, fieldsToFetchFacets)

    fun getProductsByAllergen(allergen: String?, page: Int) =
            rawAPI.getProductsByAllergen(allergen, page, fieldsToFetchFacets)

    fun getToBeCompletedProductsByContributor(contributor: String?, page: Int) =
            rawAPI.getToBeCompletedProductsByContributor(contributor, page)

    fun getPicturesContributedProducts(contributor: String?, page: Int) =
            rawAPI.getPicturesContributedProducts(contributor, page)

    fun getPicturesContributedIncompleteProducts(contributor: String?, page: Int) =
            rawAPI.getPicturesContributedIncompleteProducts(contributor, page)

    fun getInfoAddedProducts(contributor: String?, page: Int) = rawAPI.getInfoAddedProducts(contributor, page)

    fun getIncompleteProducts(page: Int) = rawAPI.getIncompleteProducts(page, fieldsToFetchFacets)

    fun getProductsByStates(state: String?, page: Int) = rawAPI.getProductsByState(state, page, fieldsToFetchFacets)

    companion object {
        const val MIME_TEXT = "text/plain"
        const val PNG_EXT = ".png\""

        private val fieldsToFetchFacets: String
            get() = "brands,$localeProductNameField,product_name,image_small_url,quantity,nutrition_grades_tags,code"

        /**
         * Uploads comment by users
         *
         * @param login the username
         */
        fun getCommentToUpload(login: String?): String {
            val comment = when (BuildConfig.FLAVOR) {
                AppFlavors.OBF -> StringBuilder("Official Open Beauty Facts Android app")
                AppFlavors.OPFF -> StringBuilder("Official Open Pet Food Facts Android app")
                AppFlavors.OPF -> StringBuilder("Official Open Products Facts Android app")
                AppFlavors.OFF -> StringBuilder("Official Open Food Facts Android app")
                else -> StringBuilder("Official Open Food Facts Android app")
            }
            comment.append(" ").append(getVersionName(OFFApplication.instance))
            if (login.isNullOrEmpty()) {
                comment.append(" (Added by ").append(InstallationUtils.id(OFFApplication.instance)).append(")")
            }
            return comment.toString()
        }

        val defaultCommentToUpload: String
            get() = getCommentToUpload("")

        val localeProductNameField: String
            get() = "product_name_${getLanguage(OFFApplication.instance)}"

        /**
         * Add a product to ScanHistory synchronously
         *
         * @param mHistoryProductDao
         * @param product
         */
        fun addToHistorySync(mHistoryProductDao: HistoryProductDao, product: Product) {
            val historyProducts = mHistoryProductDao.queryBuilder().where(HistoryProductDao.Properties.Barcode.eq(product.code)).list()
            val hp = HistoryProduct(
                    product.productName,
                    product.brands,
                    product.getImageSmallUrl(getLanguage(OFFApplication.instance)),
                    product.code,
                    product.quantity,
                    product.nutritionGradeFr
            )
            if (historyProducts.isNotEmpty()) hp.id = historyProducts[0].id

            mHistoryProductDao.insertOrReplace(hp)
        }

        @JvmStatic
        fun addToHistorySync(mHistoryProductDao: HistoryProductDao, offlineSavedProduct: OfflineSavedProduct) {
            val historyProducts = mHistoryProductDao.queryBuilder().where(HistoryProductDao.Properties.Barcode.eq(offlineSavedProduct.barcode)).list()
            val productDetails = offlineSavedProduct.productDetailsMap
            val hp = HistoryProduct(
                    offlineSavedProduct.name,
                    productDetails[ApiFields.Keys.ADD_BRANDS],
                    offlineSavedProduct.imageFrontLocalUrl,
                    offlineSavedProduct.barcode,
                    productDetails[ApiFields.Keys.QUANTITY],
                    null,
            )
            if (historyProducts.isNotEmpty()) {
                hp.id = historyProducts[0].id
            }
            mHistoryProductDao.insertOrReplace(hp)
        }

        /**
         * Fill the given [Map] with user info (username, password, comment)
         *
         * @param imgMap The map to fill
         */
        fun addUserInfo(imgMap: MutableMap<String, String?> = mutableMapOf()): Map<String, String?> {
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
    }

}