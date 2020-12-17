package openfoodfacts.github.scrachx.openfood.network

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.network.services.RobotoffAPI
import openfoodfacts.github.scrachx.openfood.network.services.WikidataAPI
import openfoodfacts.github.scrachx.openfood.utils.Utils.httpClientBuilder
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

/*
* Created by  on
*/
/**
 * Initializes all the required API Services
 * @author Lobster
 * @since 03.03.18
 */
object CommonApiManager {
    val analysisDataApi: AnalysisDataAPI by lazy {
        Retrofit.Builder()
                .baseUrl(BuildConfig.HOST)
                .client(httpClientBuilder())
                .addConverterFactory(jacksonConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(AnalysisDataAPI::class.java)
    }
    val productsApi: ProductsAPI by lazy {
        Retrofit.Builder()
                .baseUrl(BuildConfig.HOST)
                .client(httpClientBuilder())
                .addConverterFactory(jacksonConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(ProductsAPI::class.java)
    }
    val robotoffApi: RobotoffAPI by lazy {
        Retrofit.Builder()
                .baseUrl("https://robotoff.openfoodfacts.org")
                .client(httpClientBuilder())
                .addConverterFactory(jacksonConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(RobotoffAPI::class.java)
    }
    val wikidataApi: WikidataAPI by lazy {
        Retrofit.Builder()
                .baseUrl(BuildConfig.WIKIDATA)
                .client(httpClientBuilder())
                .addConverterFactory(jacksonConverterFactory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(WikidataAPI::class.java)
    }
    private val jacksonConverterFactory = JacksonConverterFactory.create(jacksonObjectMapper())


}