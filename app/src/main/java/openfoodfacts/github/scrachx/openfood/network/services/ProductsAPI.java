package openfoodfacts.github.scrachx.openfood.network.services;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.models.TaglineLanguageModel;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Define our Open Food Facts API endpoints.
 * All REST methods such as GET, POST, PUT, UPDATE, DELETE can be stated in here.
 */
public interface ProductsAPI {
    @GET("api/v0/product/{barcode}.json")
    Call<State> getProductByBarcode(@Path("barcode") String barcode,
                                    @Query("fields") String fields,
                                    @Header("User-Agent") String header);

    @GET("api/v0/product/{barcode}.json")
    Single<State> getProductByBarcodeSingle(@Path("barcode") String barcode,
                                            @Query("fields") String fields,
                                            @Header("User-Agent") String header);

    @FormUrlEncoded
    @POST("cgi/product_jqm2.pl")
    Single<State> saveProductSingle(@Field(ApiFields.Keys.BARCODE) String code,
                                    @FieldMap Map<String, String> parameters,
                                    @Field("comment") String comment);

    @GET("api/v0/product/{barcode}.json?fields=image_small_url,product_name,brands,quantity,image_url,nutrition_grade_fr,code")
    Call<State> getShortProductByBarcode(@Path("barcode") String barcode,
                                         @Header("User-Agent") String header);

    @GET("cgi/search.pl?search_simple=1&json=1&action=process")
    Call<Search> searchProductByName(@Query("fields") String fields, @Query("search_terms") String name, @Query("page") int page);

    @FormUrlEncoded
    @POST("/cgi/session.pl")
    Call<ResponseBody> signIn(@Field("user_id") String login, @Field("password") String password, @Field(".submit") String submit);

    @GET("api/v0/product/{barcode}.json?fields=ingredients")
    Single<JsonNode> getIngredientsByBarcode(@Path("barcode") String barcode);

    /**
     * waiting https://github.com/openfoodfacts/openfoodfacts-server/issues/510 to use saveProduct(SendProduct)
     *
     * @deprecated
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProduct(@Query(ApiFields.Keys.BARCODE) String code,
                            @Query(ApiFields.Keys.LANG) String lang,
                            @Query(ApiFields.Keys.PRODUCT_NAME) String name,
                            @Query(ApiFields.Keys.BRANDS) String brands,
                            @Query(ApiFields.Keys.QUANTITY) String quantity,
                            @Query(ApiFields.Keys.USER_ID) String login,
                            @Query(ApiFields.Keys.USER_PASS) String password,
                            @Query(ApiFields.Keys.USER_COMMENT) String comment);

    /**
     * This method is used to upload those products which
     * does not contain Name of the product.
     * here name query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     *
     * @deprecated
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutName(@Query(ApiFields.Keys.BARCODE) String code,
                                       @Query(ApiFields.Keys.LANG) String lang,
                                       @Query(ApiFields.Keys.BRANDS) String brands,
                                       @Query(ApiFields.Keys.QUANTITY) String quantity,
                                       @Query(ApiFields.Keys.USER_ID) String login,
                                       @Query(ApiFields.Keys.USER_PASS) String password,
                                       @Query(ApiFields.Keys.USER_COMMENT) String comment);

    /**
     * This method is used to upload those products which
     * does not contain Brands of the product.
     * here Brands query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     *
     * @deprecated
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutBrands(@Query(ApiFields.Keys.BARCODE) String code,
                                         @Query(ApiFields.Keys.LANG) String lang,
                                         @Query(ApiFields.Keys.PRODUCT_NAME) String name,
                                         @Query(ApiFields.Keys.QUANTITY) String quantity,
                                         @Query("user_id") String login,
                                         @Query("password") String password,
                                         @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Quantity of the product.
     * here Quantity query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     *
     * @deprecated
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutQuantity(@Query(ApiFields.Keys.BARCODE) String code,
                                           @Query(ApiFields.Keys.LANG) String lang,
                                           @Query(ApiFields.Keys.PRODUCT_NAME) String name,
                                           @Query(ApiFields.Keys.BRANDS) String brands,
                                           @Query("user_id") String login,
                                           @Query("password") String password,
                                           @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Name and Brands of the product.
     * here Name and Brands query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     *
     * @deprecated
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutNameAndBrands(@Query(ApiFields.Keys.BARCODE) String code,
                                                @Query(ApiFields.Keys.LANG) String lang,
                                                @Query(ApiFields.Keys.QUANTITY) String quantity,
                                                @Query("user_id") String login,
                                                @Query("password") String password,
                                                @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Name and Quantity of the product.
     * here Name and Quantity query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     *
     * @deprecated
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutNameAndQuantity(@Query(ApiFields.Keys.BARCODE) String code,
                                                  @Query(ApiFields.Keys.LANG) String lang,
                                                  @Query(ApiFields.Keys.BRANDS) String brands,
                                                  @Query("user_id") String login,
                                                  @Query("password") String password,
                                                  @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Brands and Quantity of the product.
     * here Brands and Quantity query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     *
     * @deprecated
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutBrandsAndQuantity(@Query(ApiFields.Keys.BARCODE) String code,
                                                    @Query(ApiFields.Keys.LANG) String lang,
                                                    @Query(ApiFields.Keys.PRODUCT_NAME) String name,
                                                    @Query("user_id") String login,
                                                    @Query("password") String password,
                                                    @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Brands, Name and Quantity of the product.
     * here Brands, Name and Quantity query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     *
     * @deprecated
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutNameBrandsAndQuantity(@Query(ApiFields.Keys.BARCODE) String code,
                                                        @Query(ApiFields.Keys.LANG) String lang,
                                                        @Query("user_id") String login,
                                                        @Query("password") String password,
                                                        @Query("comment") String comment);

    @Multipart
    @POST("/cgi/product_image_upload.pl")
    Call<JsonNode> saveImage(@PartMap Map<String, RequestBody> fields);

    @Multipart
    @POST("/cgi/product_image_upload.pl")
    Single<JsonNode> saveImageSingle(@PartMap Map<String, RequestBody> fields);

    @GET("/cgi/product_image_crop.pl")
    Single<JsonNode> editImageSingle(@Query(ApiFields.Keys.BARCODE) String code,
                                     @QueryMap Map<String, String> fields);

    @GET("/cgi/ingredients.pl?process_image=1&ocr_engine=google_cloud_vision")
    Single<JsonNode> getIngredients(@Query(ApiFields.Keys.BARCODE) String code,
                                    @Query("id") String id);

    @GET("cgi/suggest.pl?tagtype=emb_codes")
    Single<ArrayList<String>> getEMBCodeSuggestions(@Query("term") String term);

    @GET("/cgi/suggest.pl?tagtype=periods_after_opening")
    Single<ArrayList<String>> getPeriodAfterOpeningSuggestions(@Query("term") String term);

    @GET("brand/{brand}/{page}.json")
    Call<Search> getProductByBrands(@Path("brand") String brand,
                                    @Path("page") int page,
                                    @Query("fields") String fields);

    @GET("brand/{brand}/{page}.json")
    Single<Search> getProductByBrandsSingle(@Path("brand") String brand,
                                            @Path("page") int page,
                                            @Query("fields") String fields);

    @GET("additive/{additive}/{page}.json")
    Call<Search> getProductsByAdditive(@Path("additive") String additive,
                                       @Path("page") int page,
                                       @Query("fields") String fields);

    @GET("allergen/{allergen}/{page}.json")
    Call<Search> getProductsByAllergen(@Path("allergen") String allergen,
                                       @Path("page") int page,
                                       @Query("fields") String fields);

    @GET("country/{country}/{page}.json")
    Call<Search> getProductsByCountry(@Path("country") String country,
                                      @Path("page") int page,
                                      @Query("fields") String fields);

    @GET("origin/{origin}/{page}.json")
    Call<Search> getProductsByOrigin(@Path("origin") String origin,
                                     @Path("page") int page,
                                     @Query("fields") String fields);

    @GET("manufacturing-place/{manufacturing-place}/{page}.json")
    Call<Search> getProductsByManufacturingPlace(@Path("manufacturing-place") String manufacturingPlace,
                                                 @Path("page") int page,
                                                 @Query("fields") String fields);

    @GET("store/{store}/{page}.json")
    Call<Search> getProductByStores(@Path("store") String store,
                                    @Path("page") int page,
                                    @Query("fields") String fields);

    @GET("packaging/{packaging}/{page}.json")
    Call<Search> getProductByPackaging(@Path("packaging") String packaging,
                                       @Path("page") int page,
                                       @Query("fields") String fields);

    @GET("label/{label}/{page}.json")
    Call<Search> getProductByLabel(@Path("label") String label,
                                   @Path("page") int page,
                                   @Query("fields") String fields);

    @GET("category/{category}/{page}.json?fields=product_name,brands,quantity,image_small_url,nutrition_grade_fr,code")
    Call<Search> getProductByCategory(@Path("category") String category,
                                      @Path("page") int page);

    @GET("contributor/{Contributor}/{page}.json?nocache=1")
    Call<Search> searchProductsByContributor(@Path("Contributor") String contributor,
                                             @Path("page") int page);

    @GET("language/{language}.json")
    Call<Search> byLanguage(@Path("language") String language);

    @GET("label/{label}.json")
    Call<Search> byLabel(@Path("label") String label);

    @GET("category/{category}.json")
    Call<Search> byCategory(@Path("category") String category);

    @GET("state/{state}.json")
    Call<Search> byState(@Path("state") String state, @Query("fields") String fields);

    @GET("packaging/{packaging}.json")
    Call<Search> byPackaging(@Path("packaging") String packaging);

    @GET("brand/{brand}.json")
    Call<Search> byBrand(@Path("brand") String brand);

    @GET("purchase-place/{purchasePlace}.json")
    Call<Search> byPurchasePlace(@Path("purchasePlace") String purchasePlace);

    @GET("store/{store}.json")
    Call<Search> byStore(@Path("store") String store);

    @GET("country/{country}.json")
    Call<Search> byCountry(@Path("country") String country);

    @GET("trace/{trace}.json")
    Call<Search> byTrace(@Path("trace") String trace);

    @GET("packager-code/{PackagerCode}.json")
    Call<Search> byPackagerCode(@Path("PackagerCode") String packagerCode);

    @GET("city/{City}.json")
    Call<Search> byCity(@Path("City") String city);

    @GET("nutrition-grade/{NutritionGrade}.json")
    Call<Search> byNutritionGrade(@Path("NutritionGrade") String nutritionGrade);

    @GET("nutrient-level/{NutrientLevel}.json")
    Call<Search> byNutrientLevel(@Path("NutrientLevel") String nutrientLevel);

    @GET("contributor/{Contributor}.json?nocache=1")
    Call<Search> byContributor(@Path("Contributor") String contributor);

    @GET("contributor/{Contributor}/state/to-be-completed/{page}.json?nocache=1")
    Call<Search> getToBeCompletedProductsByContributor(@Path("Contributor") String contributor, @Path("page") int page);

    @GET("/photographer/{Contributor}/{page}.json?nocache=1")
    Call<Search> getPicturesContributedProducts(@Path("Contributor") String contributor, @Path("page") int page);

    @GET("photographer/{Photographer}.json?nocache=1")
    Call<Search> byPhotographer(@Path("Photographer") String photographer);

    @GET("photographer/{Contributor}/state/to-be-completed/{page}.json?nocache=1")
    Call<Search> getPicturesContributedIncompleteProducts(@Path("Contributor") String contributor,
                                                          @Path("page") int page);

    @GET("informer/{Informer}.json?nocache=1")
    Call<Search> byInformer(@Path("Informer") String informer);

    @GET("informer/{Contributor}/{page}.json?nocache=1")
    Call<Search> getInfoAddedProducts(@Path("Contributor") String contributor, @Path("page") int page);

    @GET("informer/{Contributor}/state/to-be-completed/{page}.json?nocache=1")
    Single<Search> getInfoAddedIncompleteProductsSingle(@Path("Contributor") String contributor, @Path("page") int page);

    @GET("last-edit-date/{LastEditDate}.json")
    Call<Search> byLastEditDate(@Path("LastEditDate") String lastEditDate);

    @GET("entry-dates/{EntryDates}.json")
    Call<Search> byEntryDates(@Path("EntryDates") String entryDates);

    @GET("unknown-nutrient/{UnknownNutrient}.json")
    Call<Search> byUnknownNutrient(@Path("UnknownNutrient") String unknownNutrient);

    @GET("additive/{Additive}.json")
    Call<Search> byAdditive(@Path("Additive") String additive, @Query("fields") String fields);

    @GET("code/{Code}.json")
    Call<Search> byCode(@Path("Code") String code);

    @GET("state/{State}/{page}.json")
    Single<Search> getProductsByState(@Path("State") String state,
                                      @Path("page") int page,
                                      @Query("fields") String fields);

    /**
     * Open Beauty Facts experimental and specific APIs
     */
    @GET("period-after-opening/{PeriodAfterOpening}.json")
    Call<Search> byPeriodAfterOpening(@Path("PeriodAfterOpening") String periodAfterOpening);

    @GET("ingredient/{ingredient}.json")
    Call<Search> byIngredient(@Path("ingredient") String ingredient);

    /**
     * This method gives a list of incomplete products
     */
    @GET("state/to-be-completed/{page}.json?nocache=1")
    Single<Search> getIncompleteProducts(@Path("page") int page, @Query("fields") String fields);

    /**
     * This method gives the # of products on Open Food Facts
     */
    @GET("/1.json?fields=null")
    Single<Search> getTotalProductCount(@Header("User-Agent") String header);

    /**
     * This method gives the news in all languages
     */
    @GET("/files/tagline/tagline-" + BuildConfig.FLAVOR + ".json")
    Single<ArrayList<TaglineLanguageModel>> getTaglineSingle(@Header("User-Agent") String header);

    /**
     * This method gives the image fields of a product
     */
    @GET("api/v0/product/{barcode}.json?fields=images")
    Call<String> getProductImages(@Path("barcode") String barcode);

    /**
     * This method is to crop images server side
     */
    @GET("/cgi/product_image_crop.pl")
    Call<String> editImages(@Query(ApiFields.Keys.BARCODE) String code,
                            @QueryMap Map<String, String> fields);

    @GET("/cgi/product_image_unselect.pl")
    Call<String> unselectImage(@Query(ApiFields.Keys.BARCODE) String code,
                               @QueryMap Map<String, String> fields);

    @GET
    Single<ResponseBody> downloadFileSingle(@Url String fileUrl);
}
