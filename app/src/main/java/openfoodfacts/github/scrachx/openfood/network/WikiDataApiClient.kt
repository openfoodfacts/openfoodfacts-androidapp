package openfoodfacts.github.scrachx.openfood.network

import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.network.services.WikidataAPI
import openfoodfacts.github.scrachx.openfood.utils.Utils
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

/**
 * API client to recieve data from WikiData APIs
 *
 * @author Shubham Vishwakarma
 * @since 14.03.18
 */
class WikiDataApiClient(customEndpointUrl: String? = null) {
    val wikidataAPI: WikidataAPI = if (customEndpointUrl == null) CommonApiManager.wikidataApi
    else {
        Retrofit.Builder()
                .baseUrl(customEndpointUrl)
                .client(Utils.defaultHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .build()
                .create(WikidataAPI::class.java)
    }

    /**
     * Get json response of the WikiData for additive/ingredient/category/label using their WikiDataID
     *
     * @param code WikiData ID of additive/ingredient/category/label
     */
    fun doSomeThing(code: String) = wikidataAPI.getWikiCategory(code).map { it["entities"][code] }

}