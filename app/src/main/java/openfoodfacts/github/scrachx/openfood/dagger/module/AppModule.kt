package openfoodfacts.github.scrachx.openfood.dagger.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.category.CategoryRepository
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService
import openfoodfacts.github.scrachx.openfood.models.DaoMaster
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.wikidataApi
import openfoodfacts.github.scrachx.openfood.utils.OFFDatabaseHelper
import openfoodfacts.github.scrachx.openfood.utils.Utils.defaultHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(httpClient: OkHttpClient): Retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.OFWEBSITE)
            .client(httpClient)
            .addConverterFactory(JacksonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()

    @Provides
    @Singleton
    fun provideDaoSession(@ApplicationContext context: Context): DaoSession {
        // Use only during development: DaoMaster.DevOpenHelper (Drops all table on Upgrade!)
        // Use only during production: OFFDatabaseHelper (see on Upgrade!)
        val dbName = when (BuildConfig.FLAVOR) {
            AppFlavors.OPFF -> "open_pet_food_facts"
            AppFlavors.OBF -> "open_beauty_facts"
            AppFlavors.OPF -> "open_products_facts"
            AppFlavors.OFF -> "open_food_facts"
            else -> "open_food_facts"
        }
        return DaoMaster(OFFDatabaseHelper(context, dbName).writableDb).newSession()
    }

    @Provides
    fun provideCategoryNetworkService(retrofit: Retrofit): CategoryNetworkService =
            retrofit.create(CategoryNetworkService::class.java)

    @Provides
    @Singleton
    fun provideCategoryRepository(networkService: CategoryNetworkService,
                                  mapper: CategoryMapper): CategoryRepository =
            CategoryRepository(networkService, mapper)


    @Provides
    @Singleton
    fun provideHttpClient() = defaultHttpClient


    // APIs

    @Provides
    @Singleton
    fun provideProductsAPI() = productsApi

    @Provides
    @Singleton
    fun provideWikiDataAPI() = wikidataApi
}