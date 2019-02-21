package openfoodfacts.github.scrachx.openfood.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Define our WikiData API endpoints.
 * Get method to get json response from wikidata server.
 */

public interface WikidataApiService {


    @GET("{code}.json")
    Call<Object> getWikiCategory(@Path("code") String code);
}
