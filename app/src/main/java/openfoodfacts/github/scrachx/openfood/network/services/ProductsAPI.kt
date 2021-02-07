/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.network.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import openfoodfacts.github.scrachx.openfood.BuildConfig.FLAVOR
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.Search
import openfoodfacts.github.scrachx.openfood.models.TagLineLanguage
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.util.*


/**
 * Define our Open Food Facts API endpoints.
 * All REST methods such as GET, POST, PUT, UPDATE, DELETE can be stated in here.
 */
interface ProductsAPI {

    companion object {
        private const val API_VER = "v0"

        /**
         * The api prefix URL
         */
        private const val API_P = "/api/$API_VER"
    }

    @GET("$API_P/product/{barcode}.json")
    fun getProductByBarcode(
            @Path("barcode") barcode: String,
            @Query("fields") fields: String,
            @Header("User-Agent") header: String
    ): Single<ProductState>

    @FormUrlEncoded
    @POST("cgi/product_jqm2.pl")
    fun saveProduct(
            @Field(ApiFields.Keys.BARCODE) code: String?,
            @FieldMap parameters: Map<String?, @JvmSuppressWildcards String?>?,
            @Field(ApiFields.Keys.USER_COMMENT) comment: String?
    ): Single<ProductState>

    @GET("cgi/search.pl?search_simple=1&json=1&action=process")
    fun searchProductByName(
            @Query("search_terms") name: String,
            @Query("fields") fields: String,
            @Query("page") page: Int
    ): Single<Search>

    @FormUrlEncoded
    @POST("/cgi/session.pl")
    fun signIn(
            @Field(ApiFields.Keys.USER_ID) login: String?,
            @Field(ApiFields.Keys.USER_PASS) password: String?,
            @Field(".submit") submit: String?
    ): Single<Response<ResponseBody>>

    @GET("$API_P/product/{barcode}.json?fields=ingredients")
    fun getIngredientsByBarcode(@Path("barcode") barcode: String?): Single<JsonNode>

    @Deprecated("")
    @Multipart
    @POST("/cgi/product_image_upload.pl")
    fun saveImage(@PartMap fields: Map<String?, @JvmSuppressWildcards RequestBody?>?): Call<JsonNode>

    @Multipart
    @POST("/cgi/product_image_upload.pl")
    fun saveImageSingle(@PartMap fields: Map<String, @JvmSuppressWildcards RequestBody?>?): Single<JsonNode>

    @GET("/cgi/product_image_crop.pl")
    fun editImageSingle(
            @Query(ApiFields.Keys.BARCODE) code: String?,
            @QueryMap fields: Map<String, @JvmSuppressWildcards String?>?
    ): Single<JsonNode>

    @GET("/cgi/ingredients.pl?process_image=1&ocr_engine=google_cloud_vision")
    fun performOCR(
            @Query(ApiFields.Keys.BARCODE) code: String,
            @Query("id") imgId: String
    ): Single<JsonNode>

    @GET("cgi/suggest.pl?tagtype=emb_codes")
    fun getEMBCodeSuggestions(@Query("term") term: String?): Single<ArrayList<String>>

    @GET("/cgi/suggest.pl?tagtype=periods_after_opening")
    fun getPeriodAfterOpeningSuggestions(@Query("term") term: String?): Single<ArrayList<String>>

    @GET("brand/{brand}/{page}.json")
    fun getProductByBrands(
            @Path("brand") brand: String,
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    /**
     * call API service to return products using Additives
     *
     * @param additive search query for products
     * @param page number of pages
     */
    @GET("additive/{additive}/{page}.json")
    fun getProductsByAdditive(
            @Path("additive") additive: String,
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    @GET("allergen/{allergen}/{page}.json")
    fun getProductsByAllergen(
            @Path("allergen") allergen: String,
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    @GET("country/{country}/{page}.json")
    fun getProductsByCountry(
            @Path("country") country: String,
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    @GET("origin/{origin}/{page}.json")
    fun getProductsByOrigin(
            @Path("origin") origin: String,
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    @GET("manufacturing-place/{manufacturing-place}/{page}.json")
    fun getProductsByManufacturingPlace(
            @Path("manufacturing-place") manufacturingPlace: String,
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    @GET("store/{store}/{page}.json")
    fun getProductByStores(
            @Path("store") store: String,
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    @GET("packaging/{packaging}/{page}.json")
    fun getProductsByPackaging(
            @Path("packaging") packaging: String,
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    @GET("label/{label}/{page}.json")
    fun getProductsByLabel(
            @Path("label") label: String,
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    @GET("category/{category}/{page}.json?fields=product_name,brands,quantity,image_small_url,nutrition_grade_fr,code")
    fun getProductByCategory(
            @Path("category") category: String,
            @Path("page") page: Int
    ): Single<Search>

    @GET("contributor/{Contributor}/{page}.json?nocache=1")
    fun getProductsByContributor(
            @Path("Contributor") contributor: String,
            @Path("page") page: Int
    ): Single<Search>

    @GET("language/{language}.json")
    fun getProductsByLanguage(@Path("language") language: String): Single<Search>

    @GET("label/{label}.json")
    fun getProductsByLabel(@Path("label") label: String): Single<Search>

    @GET("category/{category}.json")
    fun getProductsByCategory(@Path("category") category: String): Single<Search>

    @GET("state/{state}.json")
    fun getProductsByState(
            @Path("state") state: String,
            @Query("fields") fields: String
    ): Single<Search>

    @GET("packaging/{packaging}.json")
    fun getProductsByPackaging(@Path("packaging") packaging: String): Single<Search>

    @GET("brand/{brand}.json")
    fun getProductsByBrand(@Path("brand") brand: String): Single<Search>

    @GET("purchase-place/{purchasePlace}.json")
    fun getProductsByPurchasePlace(@Path("purchasePlace") purchasePlace: String): Single<Search>

    @GET("store/{store}.json")
    fun getProductsByStore(@Path("store") store: String): Single<Search>

    @GET("country/{country}.json")
    fun byCountry(@Path("country") country: String): Single<Search>

    @GET("trace/{trace}.json")
    fun getProductsByTrace(@Path("trace") trace: String): Single<Search>

    @GET("packager-code/{PackagerCode}.json")
    fun getProductsByPackagerCode(@Path("PackagerCode") packagerCode: String): Single<Search>

    @GET("city/{City}.json")
    fun getProducsByCity(@Path("City") city: String): Single<Search>

    @GET("nutrition-grade/{nutriscore}.json")
    fun getProductsByNutriScore(@Path("nutriscore") nutritionGrade: String): Single<Search>

    @GET("nutrient-level/{NutrientLevel}.json")
    fun byNutrientLevel(@Path("NutrientLevel") nutrientLevel: String): Single<Search>

    @GET("contributor/{Contributor}.json?nocache=1")
    fun byContributor(@Path("Contributor") contributor: String): Single<Search>

    @GET("contributor/{Contributor}/state/to-be-completed/{page}.json?nocache=1")
    fun getToBeCompletedProductsByContributor(
            @Path("Contributor") contributor: String,
            @Path("page") page: Int
    ): Single<Search>

    @GET("/photographer/{Contributor}/{page}.json?nocache=1")
    fun getPicturesContributedProducts(
            @Path("Contributor") contributor: String,
            @Path("page") page: Int
    ): Single<Search>

    @GET("photographer/{Photographer}.json?nocache=1")
    fun getProductsByPhotographer(@Path("Photographer") photographer: String): Single<Search>

    @GET("photographer/{Contributor}/state/to-be-completed/{page}.json?nocache=1")
    fun getPicturesContributedIncompleteProducts(
            @Path("Contributor") contributor: String?,
            @Path("page") page: Int
    ): Single<Search>

    @GET("informer/{Informer}.json?nocache=1")
    fun getProductsByInformer(@Path("Informer") informer: String?): Single<Search>

    @GET("informer/{Contributor}/{page}.json?nocache=1")
    fun getInfoAddedProducts(@Path("Contributor") contributor: String?, @Path("page") page: Int): Single<Search>

    @GET("informer/{Contributor}/state/to-be-completed/{page}.json?nocache=1")
    fun getInfoAddedIncompleteProductsSingle(
            @Path("Contributor") contributor: String,
            @Path("page") page: Int
    ): Single<Search>

    @GET("last-edit-date/{LastEditDate}.json")
    fun getProductsByLastEditDate(@Path("LastEditDate") lastEditDate: String): Single<Search>

    @GET("entry-dates/{EntryDates}.json")
    fun getProductsByEntryDates(@Path("EntryDates") entryDates: String): Single<Search>

    @GET("unknown-nutrient/{UnknownNutrient}.json")
    fun getProductsByUnknownNutrient(@Path("UnknownNutrient") unknownNutrient: String): Single<Search>

    @GET("additive/{Additive}.json")
    fun getProductsByAdditive(
            @Path("Additive") additive: String?,
            @Query("fields") fields: String?
    ): Single<Search>

    @GET("code/{Code}.json")
    fun getProductsByBarcode(@Path("Code") code: String): Single<Search>

    @GET("state/{State}/{page}.json")
    fun getProductsByState(
            @Path("State") state: String?,
            @Path("page") page: Int,
            @Query("fields") fields: String?
    ): Single<Search>

    /*
     * Open Beauty Facts experimental and specific APIs
     */
    @Deprecated("")
    @GET("period-after-opening/{PeriodAfterOpening}.json")
    fun getProductsByPeriodAfterOpening(@Path("PeriodAfterOpening") periodAfterOpening: String): Call<Search>

    @GET("ingredient/{ingredient}.json")
    fun getProductsByIngredient(@Path("ingredient") ingredient: String): Single<Search>

    /**
     * This method gives a list of incomplete products
     */
    @GET("state/to-be-completed/{page}.json?nocache=1")
    fun getIncompleteProducts(
            @Path("page") page: Int,
            @Query("fields") fields: String
    ): Single<Search>

    /**
     * This method is used to get the number of products on Open X Facts
     */
    @GET("/1.json?fields=null")
    fun getTotalProductCount(@Header("User-Agent") header: String): Single<Search>

    /**
     * This method gives the news in all languages
     */
    @GET("/files/tagline/tagline-$FLAVOR.json")
    fun getTagline(@Header("User-Agent") header: String): Single<ArrayList<TagLineLanguage>>

    /**
     * Returns images for the current product
     *
     * @param barcode barcode for the current product
     */
    @GET("$API_P/product/{barcode}.json?fields=images")
    fun getProductImages(@Path("barcode") barcode: String): Single<ObjectNode>

    /**
     * This method is to crop images server side
     *
     */
    @GET("/cgi/product_image_crop.pl")
    fun editImages(
            @Query(ApiFields.Keys.BARCODE) code: String,
            @QueryMap fields: Map<String, @JvmSuppressWildcards String?>?
    ): Single<String>

    /**
     * This method is to crop images server side
     */
    @Deprecated("")
    @GET("/cgi/product_image_crop.pl")
    fun editImagesSingle(
            @Query(ApiFields.Keys.BARCODE) code: String,
            @QueryMap fields: Map<String?, @JvmSuppressWildcards String?>?
    ): Single<String>

    /**
     * @param code
     * @param fields
     */
    @GET("/cgi/product_image_unselect.pl")
    fun unSelectImage(
            @Query(ApiFields.Keys.BARCODE) code: String,
            @QueryMap fields: Map<String, @JvmSuppressWildcards String?>?
    ): Single<String>

    @GET
    fun downloadFile(@Url fileUrl: String): Single<ResponseBody>

}