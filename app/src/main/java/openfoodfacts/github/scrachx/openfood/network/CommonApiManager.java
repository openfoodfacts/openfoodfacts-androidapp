package openfoodfacts.github.scrachx.openfood.network;


import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by Lobster on 03.03.18.
 */

public class CommonApiManager implements ICommonApiManager {

    private static CommonApiManager instance;
    private ProductApiService productApiService;
    private OpenFoodAPIService openFoodApiService;
    private JacksonConverterFactory jacksonConverterFactory;

    public static ICommonApiManager getInstance() {
        if (instance == null) {
            instance = new CommonApiManager();
        }

        return instance;
    }

    private CommonApiManager() {
            jacksonConverterFactory = JacksonConverterFactory.create();
    }

    @Override
    public ProductApiService getProductApiService() {
        if (productApiService == null) {
            productApiService = createProductApiService();
        }

        return productApiService;
    }

    @Override
    public OpenFoodAPIService getOpenFoodApiService() {
        if (openFoodApiService == null) {
            openFoodApiService = createOpenFoodApiService();
        }

        return openFoodApiService;
    }

    private ProductApiService createProductApiService() {
        productApiService = new Retrofit.Builder()
                .baseUrl(BuildConfig.HOST)
                .client(Utils.HttpClientBuilder())
                .addConverterFactory(jacksonConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ProductApiService.class);

        return productApiService;
    }

    private OpenFoodAPIService createOpenFoodApiService() {
        openFoodApiService = new Retrofit.Builder()
                .baseUrl(BuildConfig.HOST)
                .client(Utils.HttpClientBuilder())
                .addConverterFactory(jacksonConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(OpenFoodAPIService.class);

        return openFoodApiService;
    }
}
