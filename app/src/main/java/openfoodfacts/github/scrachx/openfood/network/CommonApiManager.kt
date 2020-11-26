package openfoodfacts.github.scrachx.openfood.network;

import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.network.services.RobotoffAPI;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
/*
 * Created by Lobster on 03.03.18.
 */

/**
 * Initializes all the required API Services
 */
public class CommonApiManager {
    private static CommonApiManager instance;
    private AnalysisDataAPI analysisDataApi;
    private ProductsAPI productsApi;
    private RobotoffAPI robotoffApi;
    private final JacksonConverterFactory jacksonConverterFactory;

    public static CommonApiManager getInstance() {
        if (instance == null) {
            instance = new CommonApiManager();
        }

        return instance;
    }

    private CommonApiManager() {
        jacksonConverterFactory = JacksonConverterFactory.create();
    }

    /**
     * Defines and returns ProductAPIService
     */
    public AnalysisDataAPI getAnalysisDataApi() {
        if (analysisDataApi == null) {
            analysisDataApi = createProductApiService();
        }

        return analysisDataApi;
    }

    /**
     * Defines and returns getOpenFoodApiService
     */
    public ProductsAPI getProductsApi() {
        if (productsApi == null) {
            productsApi = createOpenFoodApiService();
        }

        return productsApi;
    }

    /**
     * Defines and returns getRobotoffApiService
     */
    public RobotoffAPI getRobotoffApi() {
        if (robotoffApi == null) {
            robotoffApi = createRobotoffApiService();
        }

        return robotoffApi;
    }

    /**
     * Initialising ProductApiService using Retrofit
     */
    private AnalysisDataAPI createProductApiService() {
        analysisDataApi = new Retrofit.Builder()
            .baseUrl(BuildConfig.HOST)
            .client(Utils.httpClientBuilder())
            .addConverterFactory(jacksonConverterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(AnalysisDataAPI.class);

        return analysisDataApi;
    }

    /**
     * Initialising RobotoffAPIService using Retrofit
     */
    private RobotoffAPI createRobotoffApiService() {
        robotoffApi = new Retrofit.Builder()
            .baseUrl("https://robotoff.openfoodfacts.org")
            .client(Utils.httpClientBuilder())
            .addConverterFactory(jacksonConverterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(RobotoffAPI.class);

        return robotoffApi;
    }

    /**
     * Initialising OpenFoodAPIService using Retrofit
     */
    private ProductsAPI createOpenFoodApiService() {
        productsApi = new Retrofit.Builder()
            .baseUrl(BuildConfig.HOST)
            .client(Utils.httpClientBuilder())
            .addConverterFactory(jacksonConverterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(ProductsAPI.class);

        return productsApi;
    }
}
