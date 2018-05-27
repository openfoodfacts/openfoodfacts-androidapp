package org.openfoodfacts.scanner.dagger.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import org.openfoodfacts.scanner.BuildConfig;
import org.openfoodfacts.scanner.category.CategoryRepository;
import org.openfoodfacts.scanner.category.mapper.CategoryMapper;
import org.openfoodfacts.scanner.category.network.CategoryNetworkService;
import org.openfoodfacts.scanner.dagger.Qualifiers;
import org.openfoodfacts.scanner.utils.Utils;
import org.openfoodfacts.scanner.views.OFFApplication;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Module
public class AppModule {
    private OFFApplication application;

    public AppModule(OFFApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    OFFApplication provideTrainLineApplication() {
        return application;
    }

    @Provides
    @Qualifiers.ForApplication
    @Singleton
    Context provideApplicationContext() {
        return application;
    }

    private final static OkHttpClient httpClient = Utils.HttpClientBuilder();

    @Provides
    @Singleton
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.OFWEBSITE)
                .client(httpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
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
}