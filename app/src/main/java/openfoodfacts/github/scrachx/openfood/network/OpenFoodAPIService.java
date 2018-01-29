package openfoodfacts.github.scrachx.openfood.network;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.models.AllergenRestResponse;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.State;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Define our Open Food Facts API endpoints.
 * All REST methods such as GET, POST, PUT, UPDATE, DELETE can be stated in here.
 */
public interface OpenFoodAPIService {

    String PRODUCT_API_COMMENT = "new android app";


    @GET("api/v0/produit/{barcode}.json")
    Call<State> getProductByBarcode(@Path("barcode") String barcode);

    @GET("cgi/search.pl?search_simple=1&json=1&action=process")
    Call<Search> searchProductByName(@Query("search_terms") String name, @Query("page") int page);

    @FormUrlEncoded
    @POST("/cgi/session.pl")
    Call<ResponseBody> signIn(@Field("user_id") String login, @Field("password") String password, @Field(".submit") String submit);

    @POST("/cgi/product_jqm2.pl")
    Call<State> saveProduct(@Body SendProduct product);

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

    @Multipart
    @POST("/cgi/product_image_upload.pl")
    Call<JsonNode> saveImage(@PartMap Map<String, RequestBody> fields);

    @GET("allergens.json")
    Call<AllergenRestResponse> getAllergens();

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
    
    @GET("photographer/{Photographer}.json")
    Call<Search> byPhotographer(@Path("Photographer") String Photographer);
    
    @GET("informer/{Informer}.json")
    Call<Search> byInformer(@Path("Informer") String Informer);

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

    /**
     * Open Beauty Facts experimental and specific APIs
     */

    @GET("period-after-opening/{PeriodAfterOpening}.json")
    Call<Search> byPeriodAfterOpening(@Path("PeriodAfterOpening") String PeriodAfterOpening);

    @GET("ingredient/{ingredient}.json")
    Call<Search> byIngredient(@Path("ingredient") String ingredient);
}
