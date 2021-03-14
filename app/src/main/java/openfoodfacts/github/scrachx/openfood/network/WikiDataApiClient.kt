package openfoodfacts.github.scrachx.openfood.network

import openfoodfacts.github.scrachx.openfood.network.services.WikidataAPI
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API client to recieve data from WikiData APIs
 *
 * @author Shubham Vishwakarma
 * @since 14.03.18
 */
@Singleton
class WikiDataApiClient @Inject constructor() {

    @Inject
    lateinit var wikidataAPI: WikidataAPI

    /**
     * Get json response of the WikiData for additive/ingredient/category/label using their WikiDataID
     *
     * @param code WikiData ID of additive/ingredient/category/label
     */
    fun doSomeThing(code: String) = wikidataAPI.getWikiCategory(code).map { it["entities"][code] }

}