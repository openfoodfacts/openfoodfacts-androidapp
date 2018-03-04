package openfoodfacts.github.scrachx.openfood.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.models.AdditivesWrapper;
import openfoodfacts.github.scrachx.openfood.models.AllergensWrapper;
import openfoodfacts.github.scrachx.openfood.models.CategoriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.CountriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.LabelsWrapper;
import openfoodfacts.github.scrachx.openfood.network.deserializers.AdditivesWrapperDeserializer;
import openfoodfacts.github.scrachx.openfood.network.deserializers.AllergensWrapperDeserializer;
import openfoodfacts.github.scrachx.openfood.network.deserializers.CategoriesWrapperDeserializer;
import openfoodfacts.github.scrachx.openfood.network.deserializers.CountriesWrapperDeserializer;
import openfoodfacts.github.scrachx.openfood.network.deserializers.LabelsWrapperDeserializer;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Lobster on 03.03.18.
 */

public class CommonApiManager implements ICommonApiManager {

    private static CommonApiManager instance;
    private ProductApiService productApiService;
    private OpenFoodAPIService openFoodApiService;

    public static ICommonApiManager getInstance() {
        if (instance == null) {
            instance = new CommonApiManager();
        }

        return instance;
    }

    private CommonApiManager() {

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
                .addConverterFactory(createGsonConverter())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ProductApiService.class);

        return productApiService;
    }

    private OpenFoodAPIService createOpenFoodApiService() {
        openFoodApiService = new Retrofit.Builder()
                .baseUrl(BuildConfig.HOST)
                .client(Utils.HttpClientBuilder())
                .addConverterFactory(createGsonConverter())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(OpenFoodAPIService.class);

        return openFoodApiService;
    }

    private Converter.Factory createGsonConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LabelsWrapper.class, new LabelsWrapperDeserializer());
        gsonBuilder.registerTypeAdapter(AllergensWrapper.class, new AllergensWrapperDeserializer());
        gsonBuilder.registerTypeAdapter(CountriesWrapper.class, new CountriesWrapperDeserializer());
        gsonBuilder.registerTypeAdapter(AdditivesWrapper.class, new AdditivesWrapperDeserializer());
        gsonBuilder.registerTypeAdapter(CategoriesWrapper.class, new CategoriesWrapperDeserializer());
        Gson gson = gsonBuilder.create();
        return GsonConverterFactory.create(gson);
    }


}
