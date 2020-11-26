package openfoodfacts.github.scrachx.openfood.network

import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.network.services.RobotoffAPI
import openfoodfacts.github.scrachx.openfood.utils.Utils.httpClientBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

/*
* Created by Lobster on 03.03.18.
*/
/**
 * Initializes all the required API Services
 */
object CommonApiManager {
    val analysisDataApi: AnalysisDataAPI by lazy {createProductApiService()}
    val productsApi: ProductsAPI by lazy { createOpenFoodApiService() }
    val robotoffApi: RobotoffAPI by lazy { createRobotoffApiService() }
    private val jacksonConverterFactory: JacksonConverterFactory = JacksonConverterFactory.create()


    /**
     * Initialising ProductApiService using Retrofit
     */
    private fun createProductApiService() = Retrofit.Builder()
            .baseUrl(BuildConfig.HOST)
            .client(httpClientBuilder())
            .addConverterFactory(jacksonConverterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(AnalysisDataAPI::class.java)

    /**
     * Initialising RobotoffAPIService using Retrofit
     */
    private fun createRobotoffApiService() = Retrofit.Builder()
            .baseUrl("https://robotoff.openfoodfacts.org")
            .client(httpClientBuilder())
            .addConverterFactory(jacksonConverterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(RobotoffAPI::class.java)

    /**
     * Initialising OpenFoodAPIService using Retrofit
     */
    private fun createOpenFoodApiService() = Retrofit.Builder()
            .baseUrl(BuildConfig.HOST)
            .client(httpClientBuilder())
            .addConverterFactory(jacksonConverterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .build()
            .create(ProductsAPI::class.java)

}