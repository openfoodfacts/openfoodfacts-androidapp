package openfoodfacts.github.scrachx.openfood.dagger.module

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
import openfoodfacts.github.scrachx.openfood.utils.Utils.defaultHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton

@Module
class AppModule(private val application: OFFApplication) {
    @Provides
    @Singleton
    fun provideTrainLineApplication() = application

    @Provides
    @ForApplication
    @Singleton
    fun provideApplicationContext() = application

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.OFWEBSITE)
            .client(httpClient)
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

    @Provides
    fun provideCategoryNetworkService(retrofit: Retrofit): CategoryNetworkService =
            retrofit.create(CategoryNetworkService::class.java)

    @Provides
    @Singleton
    fun provideCategoryRepository(networkService: CategoryNetworkService, mapper: CategoryMapper): CategoryRepository =
            CategoryRepository(networkService, mapper)

    @Provides
    @Singleton
    fun provideOpenFactsApiClient() = productsApi

    companion object {
        private val httpClient = defaultHttpClient
    }
}