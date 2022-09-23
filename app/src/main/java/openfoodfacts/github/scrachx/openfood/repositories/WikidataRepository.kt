package openfoodfacts.github.scrachx.openfood.repositories

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.network.services.WikidataAPI
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API client to recieve data from WikiData APIs
 *
 * @author Shubham Vishwakarma
 * @since 14.03.18
 */
@Singleton
class WikidataRepository @Inject constructor(
    private val wikidataAPI: WikidataAPI,
    private val dispatchers: CoroutineDispatchers,
) {
    /**
     * Get json response of the WikiData for additive/ingredient/category/label using their WikiDataID
     *
     * @param entityId WikiData ID of additive/ingredient/category/label
     */
    suspend fun getEntityData(entityId: String): JsonNode? = withContext(dispatchers.IO) {
        require(entityId[0] == 'Q') {
            "Entity ID should start with 'Q'. Got: $entityId"
        }
        wikidataAPI.getEntity(entityId)?.get("entities")?.get(entityId)
    }
}