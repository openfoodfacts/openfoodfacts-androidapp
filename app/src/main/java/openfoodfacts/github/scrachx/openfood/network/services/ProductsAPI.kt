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
import okhttp3.RequestBody
import okhttp3.ResponseBody
import openfoodfacts.github.scrachx.openfood.BuildConfig.FLAVOR_versionCode
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.Search
import openfoodfacts.github.scrachx.openfood.models.TagLineLanguage
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


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
        private const val API_P = "api/$API_VER"
    }

    @GET("$API_P/product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String,
        @Query("lc") locale: String,
        @Header("User-Agent") header: String
    ): ProductState

    /**
     * @param barcodes String of comma separated barcodes
     */
    @GET("$API_P/search")
    suspend fun getProductsByBarcode(
        @Query("code") barcodes: String,
        @Query("fields") fields: String,
        @Header("User-Agent") header: String
    ): Search

    @FormUrlEncoded
    @POST("cgi/product_jqm2.pl")
    suspend fun saveProduct(
        @Field(ApiFields.Keys.BARCODE) code: String?,
        @FieldMap parameters: Map<String?, @JvmSuppressWildcards String?>?,
        @Field(ApiFields.Keys.USER_COMMENT) comment: String?
    ): ProductState

    @GET("cgi/search.pl?search_simple=1&json=1&action=process")
    suspend fun searchProductByName(
        @Query(ApiFields.Keys.SEARCH_TERMS) name: String,
        @Query("fields") fields: String,
        @Query("page") page: Int
    ): Search

    @FormUrlEncoded
    @POST("/cgi/session.pl")
    suspend fun signIn(
        @Field(ApiFields.Keys.USER_ID) login: String?,
        @Field(ApiFields.Keys.USER_PASS) password: String?,
        @Field(".submit") submit: String?
    ): Response<ResponseBody>

    @GET("$API_P/product/{barcode}.json?fields=ingredients")
    suspend fun getIngredientsByBarcode(@Path("barcode") barcode: String?): JsonNode

    @Multipart
    @POST("/cgi/product_image_upload.pl")
    suspend fun saveImage(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody?>
    ): JsonNode

    @GET("/cgi/product_image_crop.pl")
    suspend fun editImage(
        @Query(ApiFields.Keys.BARCODE) code: String,
        @QueryMap fields: Map<String, @JvmSuppressWildcards String?>
    ): JsonNode

    @GET("/cgi/ingredients.pl?process_image=1&ocr_engine=google_cloud_vision")
    suspend fun performOCR(
        @Query(ApiFields.Keys.BARCODE) code: String,
        @Query("id") imgId: String
    ): JsonNode

    @GET("cgi/suggest.pl")
    suspend fun getSuggestions(
        @Query("tagtype") tagType: String,
        @Query("term") term: String
    ): ArrayList<String>


    @GET("brand/{brand}/{page}.json")
    suspend fun getProductByBrands(
        @Path("brand") brand: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    /**
     * call API service to return products using Additives
     *
     * @param additive search query for products
     * @param page number of pages
     */
    @GET("additive/{additive}/{page}.json")
    suspend fun getProductsByAdditive(
        @Path("additive") additive: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("allergen/{allergen}/{page}.json")
    suspend fun getProductsByAllergen(
        @Path("allergen") allergen: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("country/{country}/{page}.json")
    suspend fun getProductsByCountry(
        @Path("country") country: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("origin/{origin}/{page}.json")
    suspend fun getProductsByOrigin(
        @Path("origin") origin: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("manufacturing-place/{manufacturing-place}/{page}.json")
    suspend fun getProductsByManufacturingPlace(
        @Path("manufacturing-place") manufacturingPlace: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("store/{store}/{page}.json")
    suspend fun getProductByStores(
        @Path("store") store: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("packaging/{packaging}/{page}.json")
    suspend fun getProductsByPackaging(
        @Path("packaging") packaging: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("label/{label}/{page}.json")
    suspend fun getProductsByLabel(
        @Path("label") label: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("category/{category}/{page}.json")
    suspend fun getProductByCategory(
        @Path("category") category: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("contributor/{contributor}/{page}.json?nocache=1")
    suspend fun getProductsByContributor(
        @Path("contributor") contributor: String,
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    @GET("language/{language}.json")
    suspend fun getProductsByLanguage(@Path("language") language: String): Search

    @GET("label/{label}.json")
    suspend fun getProductsByLabel(@Path("label") label: String): Search

    @GET("category/{category}.json")
    suspend fun getProductsByCategory(@Path("category") category: String): Search

    @GET("state/{state}.json")
    suspend fun getProductsByState(
        @Path("state") state: String,
        @Query("fields") fields: String
    ): Search

    @GET("packaging/{packaging}.json")
    suspend fun getProductsByPackaging(@Path("packaging") packaging: String): Search

    @GET("brand/{brand}.json")
    suspend fun getProductsByBrand(@Path("brand") brand: String): Search

    @GET("purchase-place/{purchasePlace}.json")
    suspend fun getProductsByPurchasePlace(@Path("purchasePlace") purchasePlace: String): Search

    @GET("store/{store}.json")
    suspend fun getProductsByStore(@Path("store") store: String): Search

    @GET("country/{country}.json")
    suspend fun byCountry(@Path("country") country: String): Search

    @GET("trace/{trace}.json")
    suspend fun getProductsByTrace(@Path("trace") trace: String): Search

    @GET("packager-code/{packager_code}.json")
    suspend fun getProductsByPackagerCode(@Path("packager_code") packagerCode: String): Search

    @GET("city/{city}.json")
    suspend fun getProductsByCity(@Path("city") city: String): Search

    @GET("nutrition-grade/{nutriscore}.json")
    suspend fun getProductsByNutriScore(@Path("nutriscore") nutritionGrade: String): Search

    @GET("nutrient-level/{nutrient_level}.json")
    suspend fun byNutrientLevel(@Path("nutrient_level") nutrientLevel: String): Search

    @GET("contributor/{contributor}.json?nocache=1")
    suspend fun byContributor(@Path("contributor") contributor: String): Search

    @GET("contributor/{contributor}/state/to-be-completed/{page}.json?nocache=1")
    suspend fun getToBeCompletedProductsByContributor(
        @Path("contributor") contributor: String,
        @Path("page") page: Int
    ): Search

    @GET("/photographer/{contributor}/{page}.json?nocache=1")
    suspend fun getPicturesContributedProducts(
        @Path("contributor") contributor: String,
        @Path("page") page: Int
    ): Search

    @GET("photographer/{Photographer}.json?nocache=1")
    suspend fun getProductsByPhotographer(@Path("Photographer") photographer: String): Search

    @GET("photographer/{contributor}/state/to-be-completed/{page}.json?nocache=1")
    suspend fun getPicturesContributedIncompleteProducts(
        @Path("contributor") contributor: String?,
        @Path("page") page: Int
    ): Search

    @GET("informer/{informer}.json?nocache=1")
    suspend fun getProductsByInformer(@Path("informer") informer: String?): Search

    @GET("informer/{contributor}/{page}.json?nocache=1")
    suspend fun getInfoAddedProducts(
        @Path("contributor") contributor: String?,
        @Path("page") page: Int
    ): Search

    @GET("informer/{contributor}/state/to-be-completed/{page}.json?nocache=1")
    suspend fun getInfoAddedIncompleteProducts(
        @Path("contributor") contributor: String,
        @Path("page") page: Int
    ): Search

    @GET("last-edit-date/{LastEditDate}.json")
    suspend fun getProductsByLastEditDate(@Path("LastEditDate") lastEditDate: String): Search

    @GET("entry-dates/{EntryDates}.json")
    suspend fun getProductsByEntryDates(@Path("EntryDates") entryDates: String): Search

    @GET("unknown-nutrient/{UnknownNutrient}.json")
    suspend fun getProductsByUnknownNutrient(@Path("UnknownNutrient") unknownNutrient: String): Search

    @GET("additive/{Additive}.json")
    suspend fun getProductsByAdditive(
        @Path("Additive") additive: String?,
        @Query("fields") fields: String?
    ): Search

    @GET("code/{Code}.json")
    suspend fun getProductsByBarcode(@Path("Code") code: String): Search

    @GET("state/{State}/{page}.json")
    suspend fun getProductsByState(
        @Path("State") state: String?,
        @Path("page") page: Int,
        @Query("fields") fields: String?
    ): Search

    /*
     * Open Beauty Facts experimental and specific APIs
     */
    @Deprecated("")
    @GET("period-after-opening/{PeriodAfterOpening}.json")
    fun getProductsByPeriodAfterOpening(@Path("PeriodAfterOpening") periodAfterOpening: String): Call<Search>

    @GET("ingredient/{ingredient}.json")
    suspend fun getProductsByIngredient(@Path("ingredient") ingredient: String): Search

    /**
     * This method gives a list of incomplete products
     */
    @GET("state/to-be-completed/{page}.json?nocache=1")
    suspend fun getIncompleteProducts(
        @Path("page") page: Int,
        @Query("fields") fields: String
    ): Search

    /**
     * This method is used to get the number of products on Open X Facts
     */
    @GET("/1.json?fields=null")
    suspend fun getTotalProductCount(@Header("User-Agent") header: String): Search

    /**
     * This method gives the news in all languages
     */
    @GET("/files/tagline/tagline-$FLAVOR_versionCode.json")
    suspend fun getTagline(@Header("User-Agent") header: String): ArrayList<TagLineLanguage>

    /**
     * Returns images for the current product
     *
     * @param barcode barcode for the current product
     */
    @GET("$API_P/product/{barcode}.json?fields=images")
    suspend fun getProductImages(@Path("barcode") barcode: String): ObjectNode

    /**
     * This method is to crop images server side
     *
     */
    @GET("/cgi/product_image_crop.pl")
    suspend fun editImages(
        @Query(ApiFields.Keys.BARCODE) code: String,
        @QueryMap fields: Map<String, @JvmSuppressWildcards String?>?
    ): ObjectNode

    /**
     * @param code
     * @param fields
     */
    @GET("/cgi/product_image_unselect.pl")
    suspend fun unSelectImage(
        @Query(ApiFields.Keys.BARCODE) code: String,
        @QueryMap fields: Map<String, @JvmSuppressWildcards String?>?
    ): String

    @GET
    suspend fun downloadFile(@Url fileUrl: String): ResponseBody

}