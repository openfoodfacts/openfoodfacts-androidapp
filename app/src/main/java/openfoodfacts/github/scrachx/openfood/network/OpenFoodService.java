package openfoodfacts.github.scrachx.openfood.network;

import openfoodfacts.github.scrachx.openfood.models.Search;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

public interface OpenFoodService {

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

    @GET("ingredient/{ingredient}.json")
    Call<Search> byIngredient(@Path("ingredient") String ingredient);
}
