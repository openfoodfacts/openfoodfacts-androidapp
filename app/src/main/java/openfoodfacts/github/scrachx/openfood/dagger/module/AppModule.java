package openfoodfacts.github.scrachx.openfood.dagger.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.category.CategoryRepository;
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper;
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static openfoodfacts.github.scrachx.openfood.dagger.Qualifiers.ForApplication;

@Module
public class AppModule {
    private static final OkHttpClient httpClient = Utils.httpClientBuilder();
    private final OFFApplication application;

    public AppModule(OFFApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    OFFApplication provideTrainLineApplication() {
        return application;
    }

    @Provides
    @ForApplication
    @Singleton
    Context provideApplicationContext() {
        return application;
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.OFWEBSITE)
                .client(httpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build();
    }

    @Provides
    CategoryNetworkService provideCategoryNetworkService(Retrofit retrofit) {
        return retrofit.create(CategoryNetworkService.class);
    }

    @Provides
    @Singleton
    CategoryRepository provideCategoryRepository(CategoryNetworkService networkService, CategoryMapper mapper) {
        return new CategoryRepository(networkService, mapper);
    }

    @Provides
    @Singleton
    ProductsAPI provideOpenFactsApiClient() {
        return new Retrofit.Builder()
            .baseUrl(BuildConfig.HOST)
            .client(httpClient)
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(ProductsAPI.class);
    }
}
