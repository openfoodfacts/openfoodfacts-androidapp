package openfoodfacts.github.scrachx.openfood.dagger.module

import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.category.CategoryRepository
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService
import openfoodfacts.github.scrachx.openfood.dagger.Qualifiers.ForApplication
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.Utils.httpClientBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton

@Module
class AppModule(private val application: OFFApplication) {
    @Provides
    @Singleton
    fun provideTrainLineApplication(): OFFApplication {
        return application
    }

    @Provides
    @ForApplication
    @Singleton
    fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BuildConfig.OFWEBSITE)
                .client(httpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
    }

    @Provides
    fun provideCategoryNetworkService(retrofit: Retrofit): CategoryNetworkService {
        return retrofit.create(CategoryNetworkService::class.java)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(networkService: CategoryNetworkService, mapper: CategoryMapper): CategoryRepository {
        return CategoryRepository(networkService, mapper)
    }

    @Provides
    @Singleton
    fun provideOpenFactsApiClient(): ProductsAPI {
        return productsApi
    }

    companion object {
        private val httpClient = httpClientBuilder()
    }
}