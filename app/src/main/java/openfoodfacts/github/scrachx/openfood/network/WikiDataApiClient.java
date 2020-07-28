package openfoodfacts.github.scrachx.openfood.network;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.network.services.WikidataAPI;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * API client to recieve data from WikiData APIs
 *
 * @author Shubham Vishwakarma
 * @since 14.03.18
 */
public class WikiDataApiClient {
    private static final OkHttpClient httpClient = Utils.httpClientBuilder();
    private final WikidataAPI wikidataAPI;

    public WikiDataApiClient() {
        this(BuildConfig.WIKIDATA);
    }

    /**
     * Initializing the object of WikiDataApiService using the apiUrl
     *
     * @param apiUrl Url of the WikiData API
     */
    public WikiDataApiClient(String apiUrl) {
        wikidataAPI = new Retrofit.Builder()
            .baseUrl(apiUrl)
            .client(httpClient)
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(WikidataAPI.class);
    }

    /**
     * Get json response of the WikiData for additive/ingredient/category/label using their WikiDataID
     *
     * @param code WikiData ID of additive/ingredient/category/label
     * @param onWikiResponse object of class OnWikiResponse
     */
    public void doSomeThing(final String code, final OnWikiResponse onWikiResponse) {
        wikidataAPI.getWikiCategory(code).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(final @NonNull Call<Object> call, final @NonNull Response<Object> response) {
                final ObjectMapper mapper = new ObjectMapper();
                try {
                    String jsonInString = mapper.writeValueAsString(response.body());
                    JSONObject jsonObject = new JSONObject(jsonInString);
                    onWikiResponse.onResponse(jsonObject);
                } catch (JsonProcessingException | JSONException e) {
                    onWikiResponse.onResponse(null);
                    Log.e("WikiDataApiClient", "doSomeThing", e);
                }
            }

            @Override
            public void onFailure(final @NonNull Call<Object> call, final @NonNull Throwable t) {
                onWikiResponse.onResponse(null);
                Log.i("WikiDataApiClient", "failure", t);
            }
        });
    }

    /**
     * Interface to call the function {@link OnWikiResponse#onResponse(JSONObject)}
     */
    public interface OnWikiResponse {
        void onResponse(@Nullable JSONObject result);
    }
}