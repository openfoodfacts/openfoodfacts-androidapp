package openfoodfacts.github.scrachx.openfood.network;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Map;

import io.reactivex.Single;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.models.TaglineLanguageModel;
import openfoodfacts.github.scrachx.openfood.models.TagsWrapper;
import retrofit2.Call;
import retrofit2.http.Body;
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

/**
 * Define our Open Food Facts API endpoints.
 * All REST methods such as GET, POST, PUT, UPDATE, DELETE can be stated in here.
 */
public interface OpenFoodAPIService {

    String PRODUCT_API_COMMENT = "Official Android app";

    @GET("api/v0/product/{barcode}.json?fields=image_small_url,vitamins_tags,minerals_tags,amino_acids_tags,other_nutritional_substances_tags,image_front_url,image_ingredients_url,image_nutrition_url,url,code,traces_tags,ingredients_that_may_be_from_palm_oil_tags,additives_tags,allergens_hierarchy,manufacturing_places,nutriments,ingredients_from_palm_oil_tags,brands_tags,traces,categories_tags,ingredients_text,product_name,generic_name,ingredients_from_or_that_may_be_from_palm_oil_n,serving_size,allergens_tags,allergens,origins,stores,nutrition_grade_fr,nutrient_levels,countries,countries_tags,brands,packaging,labels_tags,labels_hierarchy,cities_tags,quantity,ingredients_from_palm_oil_n,image_url,link,emb_codes_tags,states_tags,creator,created_t,last_modified_t,last_modified_by,editors_tags,nova_groups,lang,purchase_places,nutrition_data_per,no_nutrition_data,other,other_information,conservation_conditions,recycling_instructions_to_discard,recycling_instructions_to_recycle,warning,customer_service,environment_infocard,environment_impact_level_tags")
    Call<State> getFullProductByBarcode(@Path("barcode") String barcode,
                                        @Header("User-Agent") String header);

    @GET("api/v0/product/{barcode}.json")
    Call<JsonNode> getFieldByLangCode(@Path("barcode") String barcode,
                                          @Query("fields") String fieldQuery);

    @GET("api/v0/product/{barcode}.json")
    Single<State> getExistingProductDetails(@Path("barcode") String barcode,
                                            @Query("fields") String fields,
                                            @Header("User-Agent") String header);

    @GET("api/v0/product/{barcode}.json?fields=image_small_url,vitamins_tags,minerals_tags,amino_acids_tags,other_nutritional_substances_tags,image_front_url,image_ingredients_url,image_nutrition_url,url,code,traces_tags,ingredients_that_may_be_from_palm_oil_tags,additives_tags,allergens_hierarchy,manufacturing_places,nutriments,ingredients_from_palm_oil_tags,brands_tags,traces,categories_tags,ingredients_text,product_name,generic_name,ingredients_from_or_that_may_be_from_palm_oil_n,serving_size,allergens_tags,allergens,origins,stores,nutrition_grade_fr,nutrient_levels,countries,countries_tags,brands,packaging,labels_tags,labels_hierarchy,cities_tags,quantity,ingredients_from_palm_oil_n,image_url,link,emb_codes_tags,states_tags,creator,created_t,last_modified_t,last_modified_by,editors_tags,nova_groups,lang,purchase_places,nutrition_data_per,no_nutrition_data,other_information,conservation_conditions,recycling_instructions_to_discard,recycling_instructions_to_recycle,warning,customer_service,environment_infocard,environment_impact_level_tags")
    Single<State> getFullProductByBarcodeSingle(@Path("barcode") String barcode,
                                                @Header("User-Agent") String header);

    @FormUrlEncoded
    @POST("cgi/product_jqm2.pl")
    Single<State> saveProductSingle(@Field("code") String code,
                                    @FieldMap Map<String, String> parameters,
                                    @Field("comment") String comment);

    @GET("api/v0/product/{barcode}.json?fields=image_small_url,product_name,brands,quantity,image_url,nutrition_grade_fr,code")
    Call<State> getShortProductByBarcode(@Path("barcode") String barcode,
                                         @Header("User-Agent") String header);

    @GET("cgi/search.pl?search_simple=1&json=1&action=process&fields=image_small_url,product_name,brands,quantity,code,nutrition_grade_fr")
    Call<Search> searchProductByName(@Query("search_terms") String name, @Query("page") int page);

    @FormUrlEncoded
    @POST("/cgi/session.pl")
    Call<ResponseBody> signIn(@Field("user_id") String login, @Field("password") String password, @Field(".submit") String submit);

    @POST("/cgi/product_jqm2.pl")
    Call<State> saveProduct(@Body SendProduct product);


    @GET("api/v0/product/{barcode}.json?fields=ingredients")
    Call<JsonNode> getIngredientsByBarcode(@Path("barcode") String barcode);

    /**
     * waiting https://github.com/openfoodfacts/openfoodfacts-server/issues/510 to use saveProduct(SendProduct)
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProduct(@Query("code") String code,
                            @Query("lang") String lang,
                            @Query("product_name") String name,
                            @Query("brands") String brands,
                            @Query("quantity") String quantity,
                            @Query("user_id") String login,
                            @Query("password") String password,
                            @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Name of the product.
     * here name query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutName(@Query("code") String code,
                                       @Query("lang") String lang,
                                       @Query("brands") String brands,
                                       @Query("quantity") String quantity,
                                       @Query("user_id") String login,
                                       @Query("password") String password,
                                       @Query("comment") String comment);


    /**
     * This method is used to upload those products which
     * does not contain Brands of the product.
     * here Brands query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutBrands(@Query("code") String code,
                                         @Query("lang") String lang,
                                         @Query("product_name") String name,
                                         @Query("quantity") String quantity,
                                         @Query("user_id") String login,
                                         @Query("password") String password,
                                         @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Quantity of the product.
     * here Quantity query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutQuantity(@Query("code") String code,
                                           @Query("lang") String lang,
                                           @Query("product_name") String name,
                                           @Query("brands") String brands,
                                           @Query("user_id") String login,
                                           @Query("password") String password,
                                           @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Name and Brands of the product.
     * here Name and Brands query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutNameAndBrands(@Query("code") String code,
                                                @Query("lang") String lang,
                                                @Query("quantity") String quantity,
                                                @Query("user_id") String login,
                                                @Query("password") String password,
                                                @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Name and Quantity of the product.
     * here Name and Quantity query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutNameAndQuantity(@Query("code") String code,
                                                  @Query("lang") String lang,
                                                  @Query("brands") String brands,
                                                  @Query("user_id") String login,
                                                  @Query("password") String password,
                                                  @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Brands and Quantity of the product.
     * here Brands and Quantity query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutBrandsAndQuantity(@Query("code") String code,
                                                    @Query("lang") String lang,
                                                    @Query("product_name") String name,
                                                    @Query("user_id") String login,
                                                    @Query("password") String password,
                                                    @Query("comment") String comment);

    /**
     * This method is used to upload those products which
     * does not contain Brands, Name and Quantity of the product.
     * here Brands, Name and Quantity query is not present to make sure if the product is already present
     * then the server would not assume to delete it.
     */
    @Deprecated
    @GET("/cgi/product_jqm2.pl")
    Call<State> saveProductWithoutNameBrandsAndQuantity(@Query("code") String code,
                                                        @Query("lang") String lang,
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
    Single<JsonNode> editImageSingle(@Query("code") String code,
                                     @QueryMap Map<String, String> fields);

    @GET("/cgi/ingredients.pl?process_image=1&ocr_engine=google_cloud_vision")
    Single<JsonNode> getIngredients(@Query("code") String code,
                                    @Query("id") String id);

    @GET("cgi/suggest.pl?tagtype=emb_codes")
    Single<ArrayList<String>> getEMBCodeSuggestions(@Query("term") String term);

    @GET("/cgi/suggest.pl?tagtype=periods_after_opening")
    Single<ArrayList<String>> getPeriodAfterOpeningSuggestions(@Query("term") String term);

    @GET("brand/{brand}/{page}.json")
    Call<Search> getProductByBrands(@Path("brand") String brand, @Path("page") int page);

    @GET("additive/{additive}/{page}.json")
    Call<Search> getProductsByAdditive(@Path("additive") String additive, @Path("page") int page);

    @GET("allergen/{allergen}/{page}.json")
    Call<Search> getProductsByAllergen(@Path("allergen") String allergen, @Path("page") int page);

    @GET("country/{country}/{page}.json")
    Call<Search> getProductsByCountry(@Path("country") String country, @Path("page") int page);

    @GET("origin/{origin}/{page}.json")
    Call<Search> getProductsByOrigin(@Path("origin") String origin, @Path("page") int page);

    @GET("manufacturing-place/{manufacturing-place}/{page}.json")
    Call<Search> getProductsByManufacturingPlace(@Path("manufacturing-place") String manufacturingPlace, @Path("page") int page);

    @GET("store/{store}/{page}.json")
    Call<Search> getProductByStores(@Path("store") String store, @Path("page") int page);

    @GET("packaging/{packaging}/{page}.json")
    Call<Search> getProductByPackaging(@Path("packaging") String packaging, @Path("page") int page);

    @GET("label/{label}/{page}.json")
    Call<Search> getProductByLabel(@Path("label") String label, @Path("page") int page);

    @GET("category/{category}/{page}.json?fields=product_name,brands,quantity,image_small_url,nutrition_grade_fr,code")
    Call<Search> getProductByCategory(@Path("category") String category, @Path("page") int page);

    @GET("contributor/{Contributor}/{page}.json")
    Call<Search> searchProductsByContributor(@Path("Contributor") String Contributor, @Path("page") int page);

    @GET("language/{language}.json")
    Call<Search> byLanguage(@Path("language") String language);

    @GET("label/{label}.json")
    Call<Search> byLabel(@Path("label") String label);

    @GET("category/{category}.json")
    Call<Search> byCategory(@Path("category") String category);

    @GET("state/{state}.json")
    Call<Search> byState(@Path("state") String state);

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
    Call<Search> byPackagerCode(@Path("PackagerCode") String PackagerCode);

    @GET("city/{City}.json")
    Call<Search> byCity(@Path("City") String City);

    @GET("nutrition-grade/{NutritionGrade}.json")
    Call<Search> byNutritionGrade(@Path("NutritionGrade") String NutritionGrade);

    @GET("nutrient-level/{NutrientLevel}.json")
    Call<Search> byNutrientLevel(@Path("NutrientLevel") String NutrientLevel);

    @GET("contributor/{Contributor}.json")
    Call<Search> byContributor(@Path("Contributor") String Contributor);

    @GET("contributor/{Contributor}/state/to-be-completed/{page}.json")
    Call<Search> getToBeCompletedProductsByContributor(@Path("Contributor") String Contributor, @Path("page") int page);

    @GET("/photographer/{Contributor}/{page}.json")
    Call<Search> getPicturesContributedProducts(@Path("Contributor") String Contributor, @Path("page") int page);

    @GET("photographer/{Photographer}.json")
    Call<Search> byPhotographer(@Path("Photographer") String Photographer);

    @GET("photographer/{Contributor}/state/to-be-completed/{page}.json")
    Call<Search> getPicturesContributedIncompleteProducts(@Path("Contributor") String Contributor, @Path("page") int page);

    @GET("informer/{Informer}.json")
    Call<Search> byInformer(@Path("Informer") String Informer);

    @GET("informer/{Contributor}/{page}.json")
    Call<Search> getInfoAddedProducts(@Path("Contributor") String Contributor, @Path("page") int page);

    @GET("informer/{Contributor}/state/to-be-completed/{page}.json")
    Call<Search> getInfoAddedIncompleteProducts(@Path("Contributor") String Contributor, @Path("page") int page);


    @GET("last-edit-date/{LastEditDate}.json")
    Call<Search> byLastEditDate(@Path("LastEditDate") String LastEditDate);


    @GET("entry-dates/{EntryDates}.json")
    Call<Search> byEntryDates(@Path("EntryDates") String EntryDates);

    @GET("unknown-nutrient/{UnknownNutrient}.json")
    Call<Search> byUnknownNutrient(@Path("UnknownNutrient") String UnknownNutrient);

    @GET("additive/{Additive}.json")
    Call<Search> byAdditive(@Path("Additive") String Additive);

    @GET("code/{Code}.json")
    Call<Search> byCode(@Path("Code") String Code);

    @GET("packager-codes.json")
    Single<TagsWrapper> getTags();

    @GET("state/{State}/{page}.json")
    Call<Search> getProductsByState(@Path("State") String state, @Path("page") int page);

    /**
     * Open Beauty Facts experimental and specific APIs
     */

    @GET("period-after-opening/{PeriodAfterOpening}.json")
    Call<Search> byPeriodAfterOpening(@Path("PeriodAfterOpening") String PeriodAfterOpening);

    @GET("ingredient/{ingredient}.json")
    Call<Search> byIngredient(@Path("ingredient") String ingredient);

    /**
     * This method gives a list of incomplete products
     */
    @GET("state/to-be-completed/{page}.json")
    Call<Search> getIncompleteProducts(@Path("page") int page);

    @GET("/1.json?fields=null")
    Single<Search> getTotalProductCount();

    @GET("/files/tagline/tagline-"+ BuildConfig.FLAVOR+".json")
    Call<ArrayList<TaglineLanguageModel>> getTagline();


    @GET("api/v0/product/{barcode}.json?fields=images")
    Call<String> getProductImages(@Path("barcode") String barcode);


    @GET("/cgi/product_image_crop.pl")
    Call<String> editImages(@Query("code") String code,
                            @QueryMap Map<String, String> fields);

}
