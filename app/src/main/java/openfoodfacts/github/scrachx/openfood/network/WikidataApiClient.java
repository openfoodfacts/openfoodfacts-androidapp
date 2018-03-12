package openfoodfacts.github.scrachx.openfood.network;


import com.google.gson.Gson;

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
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Shubham Vishwakarma on 14.03.18.
 */

public class WikidataApiClient {

    private static OkHttpClient httpClient = Utils.HttpClientBuilder();

    private final WikidataApiService wikidataApiService;

    public WikidataApiClient() {
        this(BuildConfig.WIKIDATA);
    }

    public WikidataApiClient(String apiUrl) {
        wikidataApiService = new Retrofit.Builder()
                .baseUrl(apiUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(WikidataApiService.class);
    }


    public void doSomeThing(String code, OnWikiResponse onWikiResponse) {
        wikidataApiService.getWikiCategory(code).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                JSONObject jsonObject;

                try {
                    jsonObject = new JSONObject(new Gson().toJson(response.body()));
                    onWikiResponse.onresponse(true, jsonObject);
                } catch (JSONException e) {
                    onWikiResponse.onresponse(false, null);
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {
                onWikiResponse.onresponse(false, null);

            }
        });
    }

    public interface OnWikiResponse {

        void onresponse(boolean value, JSONObject result);
    }


}
