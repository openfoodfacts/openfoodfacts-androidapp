package openfoodfacts.github.scrachx.openfood.network

import com.fasterxml.jackson.databind.JsonNode
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
class WikiDataApiClient @Inject constructor(
    private val wikidataAPI: WikidataAPI
) {
    /**
     * Get json response of the WikiData for additive/ingredient/category/label using their WikiDataID
     *
     * @param entityId WikiData ID of additive/ingredient/category/label
     */
    suspend fun getEntityData(entityId: String): JsonNode {
        require(entityId[0] == 'Q') { "Entity ID should start with 'Q'. Got: $entityId" }
        return wikidataAPI.getEntity(entityId)["entities"][entityId]
    }
}