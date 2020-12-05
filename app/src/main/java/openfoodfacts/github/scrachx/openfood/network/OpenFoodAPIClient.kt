package openfoodfacts.github.scrachx.openfood.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.fasterxml.jackson.databind.JsonNode
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.RequestBody
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.images.IMAGE_STRING_ID
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.images.getImageStringKey
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ToUploadProductDao
import openfoodfacts.github.scrachx.openfood.network.ApiCallbacks.*
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.InstallationUtils
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession
import openfoodfacts.github.scrachx.openfood.utils.Utils.getUserAgent
import openfoodfacts.github.scrachx.openfood.utils.Utils.getVersionName
import openfoodfacts.github.scrachx.openfood.utils.Utils.httpClientBuilder
import org.jetbrains.annotations.Contract
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.Companion.start as startProductViewActivity

/**
 * API Client for all API callbacks
 */
class OpenFoodAPIClient @JvmOverloads constructor(private val context: Context, customApiUrl: String? = null) {
    private var historySyncDisp: Disposable? = null
    private val mHistoryProductDao = daoSession.historyProductDao
    private val mToUploadProductDao = daoSession.toUploadProductDao

    /**
     * @return This api service gets products of provided brand.
     */
    var rawAPI: ProductsAPI = if (customApiUrl != null) {
        Retrofit.Builder()
                .baseUrl(customApiUrl)
                .client(httpClientBuilder())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(ProductsAPI::class.java)
    } else {
        productsApi
    }

    fun getProductStateFull(barcode: String?, customHeader: String?) =
            rawAPI.getProductByBarcodeSingle(barcode, allFields, getUserAgent(customHeader!!))

    fun getProductStateFull(barcode: String?) =
            rawAPI.getProductByBarcodeSingle(barcode, allFields, getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))

    private val allFields: String
        get() {
            val allFields = context.resources.getStringArray(R.array.product_all_fields_array)
            val fieldsToLocalize = context.resources.getStringArray(R.array.fields_array)
            val fields: MutableSet<String?> = allFields.toMutableSet()
            val langCode = getLanguage(OFFApplication.instance.applicationContext)
            for (fieldToLocalize in fieldsToLocalize) {
                fields.add("${fieldToLocalize}_$langCode")
                fields.add("${fieldToLocalize}_en")
            }
            return fields.joinToString(",")
        }

    fun productNotFoundDialogBuilder(activity: Activity, barcode: String): MaterialDialog.Builder {
        return MaterialDialog.Builder(activity)
                .title(R.string.txtDialogsTitle)
                .content(R.string.txtDialogsContent)
                .positiveText(R.string.txtYes)
                .negativeText(R.string.txtNo)
                .onPositive { _: MaterialDialog?, _: DialogAction? ->
                    if (!activity.isFinishing) {
                        val intent = Intent(activity, ProductEditActivity::class.java)
                        val st = ProductState()
                        val pd = Product()
                        pd.code = barcode
                        st.product = pd
                        intent.putExtra("state", st)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                }
    }

    /**
     * Open the product activity if the barcode exist.
     * Also add it in the history if the product exist.
     *
     * @param barcode product barcode
     */
    fun getProductImages(barcode: String): Single<ProductState> {
        val allFieldsArray = OFFApplication.instance.resources.getStringArray(R.array.product_images_fields_array)
        val fields = allFieldsArray.toMutableSet()
        val langCode = getLanguage(OFFApplication.instance.applicationContext)
        fields += "product_name_$langCode"
        return rawAPI.getProductByBarcodeSingle(
                barcode,
                fields.joinToString(","),
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
    @JvmOverloads
    fun openProduct(barcode: String, activity: Activity, callback: Consumer<ProductState?>? = null) {
        rawAPI.getProductByBarcode(barcode, allFields, getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)).enqueue(object : Callback<ProductState?> {
            override fun onResponse(call: Call<ProductState?>, response: Response<ProductState?>) {
                if (activity.isFinishing) {
                    return
                }
                val productState = response.body()
                if (productState == null) {
                    Toast.makeText(activity, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
                    return
                }
                if (productState.status == 0L) {
                    productNotFoundDialogBuilder(activity, barcode)
                            .onNegative { _, _ -> activity.onBackPressed() }
                            .show()
                } else {
                    addToHistory(productState.product!!).subscribe()
                    if (callback != null) {
                        callback.accept(productState)
                    } else {
                        startProductViewActivity(activity, productState)
                    }
                }
            }

            override fun onFailure(call: Call<ProductState?>, t: Throwable) {
                if (activity.isFinishing) {
                    return
                }
                val isNetwork = t is IOException
                if (callback != null) {
                    val res = ProductState()
                    res.status = 0
                    res.statusVerbose = if (isNetwork) activity.resources.getString(R.string.errorWeb) else t.message
                    callback.accept(res)
                }
                if (!isNetwork) {
                    productNotFoundDialogBuilder(activity, barcode).show()
                }
            }
        })
    }

    fun getIngredients(product: Product): Single<List<ProductIngredient>> {
        return getIngredients(product.code)
    }

    fun searchProductsByName(name: String?, page: Int): Single<Search> {
        val productNameLocale = localeProductNameField
        val fields = "selected_images,image_small_url,product_name,brands,quantity,code,nutrition_grade_fr,$productNameLocale"
        return rawAPI.searchProductByName(fields, name, page)
    }

    /**
     * @param barcode
     * @return a single containing a list of product ingredients (can be empty)
     */
    fun getIngredients(barcode: String?): Single<List<ProductIngredient>> {
        return rawAPI.getIngredientsByBarcode(barcode).map { node: JsonNode ->
            val ingredientsJsonNode = node.findValue("ingredients") ?: return@map emptyList()
            val productIngredients = ArrayList<ProductIngredient>()

            // add ingredients to list from json
            for (ingredient in ingredientsJsonNode) {
                val productIngredient = ProductIngredient()
                productIngredient.id = ingredientsJsonNode.findValue("id").toString()
                productIngredient.text = ingredientsJsonNode.findValue("text").toString()
                val rankNode = ingredientsJsonNode.findValue("rank")
                if (rankNode == null) {
                    productIngredient.rank = -1
                } else {
                    productIngredient.rank = rankNode.toString().toLong()
                }
                productIngredients.add(productIngredient)
            }
            productIngredients
        }
    }

    fun getProductsByCountry(country: String?, page: Int): Single<Search> {
        return rawAPI.getProductsByCountry(country, page, FIELDS_TO_FETCH_FACETS).subscribeOn(Schedulers.io())
    }

    /**
     * Returns a map for images uploaded for product/ingredients/nutrition/other images
     *
     * @param image object of ProductImage
     */
    private fun getUploadableMap(image: ProductImage): Map<String, RequestBody?> {
        val lang = image.language
        val imgMap: MutableMap<String, RequestBody?> = HashMap()
        imgMap["code"] = image.code
        imgMap["imagefield"] = image.field
        if (image.imgFront != null) {
            imgMap["""imgupload_front"; filename="front_$lang$PNG_EXT"""] = image.imgFront
        }
        if (image.imgIngredients != null) {
            imgMap["""imgupload_ingredients"; filename="ingredients_$lang$PNG_EXT"""] = image.imgIngredients
        }
        if (image.imgNutrition != null) {
            imgMap["""imgupload_nutrition"; filename="nutrition_$lang$PNG_EXT"""] = image.imgNutrition
        }
        if (image.imgPackaging != null) {
            imgMap["""imgupload_packaging"; filename="packaging_$lang$PNG_EXT"""] = image.imgPackaging
        }
        if (image.imgOther != null) {
            imgMap["""imgupload_other"; filename="other_$lang$PNG_EXT"""] = image.imgOther
        }

        // Attribute the upload to the connected user
        fillWithUserLoginInfo(imgMap)
        return imgMap
    }

    fun getProductsByCategory(category: String?, page: Int): Single<Search> {
        return rawAPI.getProductByCategory(category, page).subscribeOn(Schedulers.io())
    }

    fun getProductsByLabel(label: String?, page: Int): Single<Search> {
        return rawAPI.getProductByLabel(label, page, FIELDS_TO_FETCH_FACETS).subscribeOn(Schedulers.io())
    }

    /**
     * Add a product to ScanHistory asynchronously
     */
    fun addToHistory(product: Product): Completable {
        return Completable.fromAction { addToHistorySync(mHistoryProductDao, product) }
    }

    fun getProductsByContributor(contributor: String?, page: Int): Single<Search> {
        return rawAPI.searchProductsByContributor(contributor, page).subscribeOn(Schedulers.io())
    }

    /**
     * upload images in offline mode
     *
     * @return ListenableFuture
     */
    fun uploadOfflineImages(): Completable {
        return Single.fromCallable<List<Completable>> {
            val toUploadProductList = mToUploadProductDao.queryBuilder()
                    .where(ToUploadProductDao.Properties.Uploaded.eq(false))
                    .list()
            val totalSize = toUploadProductList.size
            val imagesUploading: MutableList<Completable> = ArrayList()
            for (i in 0 until totalSize) {
                val uploadProduct = toUploadProductList[i]
                var imageFile: File
                imageFile = try {
                    File(uploadProduct.imageFilePath)
                } catch (e: Exception) {
                    Log.e("OfflineUploadingTask", "doInBackground", e)
                    continue
                }
                val productImage = ProductImage(uploadProduct.barcode,
                        uploadProduct.productField, imageFile)
                imagesUploading.add(rawAPI.saveImageSingle(getUploadableMap(productImage))
                        .flatMapCompletable { jsonNode: JsonNode? ->
                            if (jsonNode != null) {
                                Log.d("onResponse", jsonNode.toString())
                                if (!jsonNode.isObject) {
                                    return@flatMapCompletable Completable.error(IOException("jsonNode is not an object"))
                                } else if (jsonNode[ApiFields.Keys.STATUS].asText().contains(ApiFields.Defaults.STATUS_NOT_OK)) {
                                    mToUploadProductDao.delete(uploadProduct)
                                    return@flatMapCompletable Completable.error(IOException(ApiFields.Defaults.STATUS_NOT_OK))
                                } else {
                                    mToUploadProductDao.delete(uploadProduct)
                                    return@flatMapCompletable Completable.complete()
                                }
                            } else {
                                return@flatMapCompletable Completable.error(IOException("jsonNode is null"))
                            }
                        })
            }
            imagesUploading
        }.flatMapCompletable { Completable.merge(it) }
    }

    fun getProductsByPackaging(packaging: String?, page: Int): Single<Search> {
        return rawAPI.getProductByPackaging(packaging, page, FIELDS_TO_FETCH_FACETS).subscribeOn(Schedulers.io())
    }

    fun getProductsByStore(store: String?, page: Int): Single<Search> {
        return rawAPI.getProductByStores(store, page, FIELDS_TO_FETCH_FACETS).subscribeOn(Schedulers.io())
    }

    /**
     * Search for products using bran name
     *
     * @param brand search query for product
     * @param page page numbers
     */
    fun getProductsByBrand(brand: String?, page: Int): Single<Search> {
        return rawAPI.getProductByBrandsSingle(brand, page, FIELDS_TO_FETCH_FACETS).subscribeOn(Schedulers.io())
    }

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
                    mToUploadProductDao.insertOrReplace(product)
                }
    }

    private fun setDefaultImageFromServerResponse(body: JsonNode, image: ProductImage): Completable {
        val queryMap: MutableMap<String, String?> = HashMap()
        queryMap["imgid"] = body["image"]["imgid"].asText()
        queryMap["id"] = body["imagefield"].asText()
        return rawAPI.editImageSingle(image.barcode, addUserInfo(queryMap))
                .flatMapCompletable { jsonNode: JsonNode ->
                    if ("status ok" == jsonNode[ApiFields.Keys.STATUS].asText()) {
                        return@flatMapCompletable Completable.complete()
                    } else {
                        throw IOException(jsonNode["error"].asText())
                    }
                }
    }

    fun editImage(code: String?, imgMap: MutableMap<String, String?>, onEditImageCallback: OnEditImageCallback) {
        rawAPI.editImages(code, addUserInfo(imgMap)).enqueue(createCallback(onEditImageCallback))
    }

    /**
     * Unselect the image from the product code.
     *
     * @param code code of the product
     * @param onEditImageCallback
     */
    fun unSelectImage(code: String?, field: ProductImageField?, language: String?): Single<String> {
        val imgMap: MutableMap<String, String?> = HashMap()
        imgMap[IMAGE_STRING_ID] = getImageStringKey(field!!, language!!)
        return rawAPI.unSelectImage(code, addUserInfo(imgMap))
    }

    @Contract(value = "_ -> new", pure = true)
    private fun createCallback(onEditImageCallback: OnEditImageCallback): Callback<String?> {
        return object : Callback<String?> {
            override fun onResponse(call: Call<String?>, response: Response<String?>) {
                onEditImageCallback.onEditResponse(true, response.body())
            }

            override fun onFailure(call: Call<String?>, t: Throwable) {
                onEditImageCallback.onEditResponse(false, null)
            }
        }
    }

    fun getProductsByOrigin(origin: String?, page: Int): Single<Search> {
        return rawAPI.getProductsByOrigin(origin, page, FIELDS_TO_FETCH_FACETS)
    }

    fun syncOldHistory() {
        historySyncDisp?.dispose()
        historySyncDisp = Completable.fromAction {
            val historyProducts = mHistoryProductDao.loadAll()
            val size = historyProducts.size
            for (i in 0 until size) {
                val historyProduct = historyProducts[i]
                rawAPI.getShortProductByBarcode(historyProduct.barcode, getUserAgent(Utils.HEADER_USER_AGENT_SEARCH)).enqueue(object : Callback<ProductState?> {
                    override fun onResponse(call: Call<ProductState?>, response: Response<ProductState?>) {
                        val s = response.body()
                        if (s != null && s.status != 0L) {
                            val product = s.product!!
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
                            mHistoryProductDao.insertOrReplace(hp)
                        }
                        context.getSharedPreferences("prefs", 0).edit().putBoolean("is_old_history_data_synced", true).apply()
                    }

                    override fun onFailure(call: Call<ProductState?>, t: Throwable) {
                        // ignored
                    }
                })
            }
        }.subscribe()
    }

    fun getInfoAddedIncompleteProductsSingle(contributor: String?, page: Int): Single<Search> {
        return rawAPI.getInfoAddedIncompleteProductsSingle(contributor, page)
    }

    fun getProductsByManufacturingPlace(manufacturingPlace: String?, page: Int): Single<Search> {
        return rawAPI.getProductsByManufacturingPlace(manufacturingPlace, page, FIELDS_TO_FETCH_FACETS)
    }

    /**
     * call API service to return products using Additives
     *
     * @param additive search query for products
     * @param page number of pages
     */
    fun getProductsByAdditive(additive: String?, page: Int): Single<Search> {
        return rawAPI.getProductsByAdditive(additive, page, FIELDS_TO_FETCH_FACETS)
    }

    fun getProductsByAllergen(allergen: String?, page: Int): Single<Search> {
        return rawAPI.getProductsByAllergen(allergen, page, FIELDS_TO_FETCH_FACETS)
    }

    fun getToBeCompletedProductsByContributor(contributor: String?, page: Int): Single<Search> {
        return rawAPI.getToBeCompletedProductsByContributor(contributor, page)
    }

    fun getPicturesContributedProducts(contributor: String?, page: Int): Single<Search> {
        return rawAPI.getPicturesContributedProducts(contributor, page)
    }

    fun getPicturesContributedIncompleteProducts(contributor: String?, page: Int) =
            rawAPI.getPicturesContributedIncompleteProducts(contributor, page)

    fun getInfoAddedProducts(contributor: String?, page: Int) = rawAPI.getInfoAddedProducts(contributor, page)

    fun getIncompleteProducts(page: Int) = rawAPI.getIncompleteProducts(page, FIELDS_TO_FETCH_FACETS)

    fun getProductsByStates(state: String?, page: Int) = rawAPI.getProductsByState(state, page, FIELDS_TO_FETCH_FACETS)

    companion object {
        const val MIME_TEXT = "text/plain"
        const val PNG_EXT = ".png\""
        private val FIELDS_TO_FETCH_FACETS = "brands,$localeProductNameField,product_name,image_small_url,quantity,nutrition_grades_tags,code"

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
            val instance = OFFApplication.instance
            comment.append(" ").append(getVersionName(instance))
            if (login.isNullOrEmpty()) {
                comment.append(" (Added by ").append(InstallationUtils.id(instance)).append(")")
            }
            return comment.toString()
        }

        val commentToUpload: String
            get() = getCommentToUpload("")

        @JvmStatic
        val localeProductNameField: String
            get() {
                val locale = getLanguage(OFFApplication.instance)
                return "product_name_$locale"
            }

        /**
         * Add a product to ScanHistory synchronously
         *
         * @param mHistoryProductDao
         * @param product
         */
        fun addToHistorySync(mHistoryProductDao: HistoryProductDao, product: Product) {
            val historyProducts = mHistoryProductDao.queryBuilder().where(HistoryProductDao.Properties.Barcode.eq(product.code)).list()
            val hp = HistoryProduct(product.productName,
                    product.brands,
                    product.getImageSmallUrl(getLanguage(OFFApplication.instance)),
                    product.code,
                    product.quantity,
                    product.nutritionGradeFr)
            if (historyProducts.isNotEmpty()) {
                hp.id = historyProducts[0].id
            }
            mHistoryProductDao.insertOrReplace(hp)
        }

        @JvmStatic
        fun addToHistorySync(mHistoryProductDao: HistoryProductDao, offlineSavedProduct: OfflineSavedProduct) {
            val historyProducts = mHistoryProductDao.queryBuilder().where(HistoryProductDao.Properties.Barcode.eq(offlineSavedProduct.barcode)).list()
            val productDetails = offlineSavedProduct.productDetailsMap
            val hp = HistoryProduct(offlineSavedProduct.name,
                    productDetails[ApiFields.Keys.ADD_BRANDS],
                    offlineSavedProduct.imageFrontLocalUrl,
                    offlineSavedProduct.barcode,
                    productDetails[ApiFields.Keys.QUANTITY],
                    null)
            if (historyProducts.isNotEmpty()) {
                hp.id = historyProducts[0].id
            }
            mHistoryProductDao.insertOrReplace(hp)
        }

        // TODO: Move it to utility class
        fun fillWithUserLoginInfo(imgMap: MutableMap<String, RequestBody?>): String? {
            val values = addUserInfo(hashMapOf())
            values.forEach { (key, value) ->
                imgMap[key] = RequestBody.create(MediaType.parse(MIME_TEXT), value)
            }
            return values[ApiFields.Keys.USER_ID]
        }

        /**
         * Fill the given [Map] with user info (username, password, comment)
         *
         * @param imgMap The map to fill
         */
        fun addUserInfo(imgMap: MutableMap<String, String?>): Map<String, String?> {
            val settings = OFFApplication.instance.getSharedPreferences("login", 0)

            settings.getString("user", "")?.let {
                imgMap[ApiFields.Keys.USER_COMMENT] = getCommentToUpload(it)
                if (it.isNotBlank()) imgMap[ApiFields.Keys.USER_ID] = it
            }

            settings.getString("pass", "")?.let {
                if (it.isNotBlank()) imgMap[ApiFields.Keys.USER_PASS] = it
            }

            return imgMap
        }
    }

}