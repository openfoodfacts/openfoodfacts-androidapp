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
 * Created by Shubham Vishwakarma on 14.03.18.
 */

public class WikidataApiClient {

    private static OkHttpClient httpClient = Utils.HttpClientBuilder();

    private final WikidataApiService wikidataApiService;
    private JacksonConverterFactory jacksonConverterFactory;

    public WikidataApiClient() {
        this(BuildConfig.WIKIDATA);
    }

    public WikidataApiClient(String apiUrl) {
        wikidataApiService = new Retrofit.Builder()
                .baseUrl(apiUrl)
                .client(httpClient)
                .addConverterFactory(jacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(WikidataApiService.class);
    }

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
                    e.printStackTrace();
                } catch (JSONException e) {
                    onWikiResponse.onresponse(false, null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                onWikiResponse.onresponse(false, null);
                Log.i("wikidataApiClient", "failure");
            }
        });
    }

    public interface OnWikiResponse {

        void onresponse(boolean value, JSONObject result);
    }


}
