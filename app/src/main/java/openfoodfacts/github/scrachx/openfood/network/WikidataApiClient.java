package openfoodfacts.github.scrachx.openfood.network;


import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * API client to recieve data from WikiData APIs
 */

public class WikidataApiClient {

    private static OkHttpClient httpClient = Utils.HttpClientBuilder();

    private final WikidataApiService wikidataApiService;
    private JacksonConverterFactory jacksonConverterFactory;

    public WikidataApiClient() {
        this(BuildConfig.WIKIDATA);
    }

    /**
     * Initializing the object of WikidataApiService using the apiUrl
     * @param apiUrl Url of the WikiData API
     * */

    public WikidataApiClient(String apiUrl) {
        wikidataApiService = new Retrofit.Builder()
                .baseUrl(apiUrl)
                .client(httpClient)
                .addConverterFactory(jacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(WikidataApiService.class);
    }

    /**
     * Get json response of the WikiData for Additive/Ingredient/Category/label using their WikiDataId
     * @param code WikiData Id of Additive/Ingredient/Category/label
     * @param onWikiResponse object of class OnWikiResponse
     * */

    public void doSomeThing(String code, OnWikiResponse onWikiResponse) {
        wikidataApiService.getWikiCategory(code).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String jsonInString = mapper.writeValueAsString(response.body());
                    JSONObject jsonObject = new JSONObject(jsonInString);
                    onWikiResponse.onresponse(true, jsonObject);
                } catch (JsonProcessingException e) {
                    onWikiResponse.onresponse(false, null);
                    Log.e("WikidataApiClient","doSomeThing",e);
                } catch (JSONException e) {
                    onWikiResponse.onresponse(false, null);
                    Log.e("WikidataApiClient","doSomeThing",e);
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                onWikiResponse.onresponse(false, null);
                Log.i("wikidataApiClient", "failure",t);
            }
        });
    }

    /**
     * Interface to call the function onresponse
     * */

    public interface OnWikiResponse {

        void onresponse(boolean value, JSONObject result);
    }


}
