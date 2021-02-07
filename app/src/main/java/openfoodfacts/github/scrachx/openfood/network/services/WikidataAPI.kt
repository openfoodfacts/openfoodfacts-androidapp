package openfoodfacts.github.scrachx.openfood.network.services

import com.fasterxml.jackson.databind.node.ObjectNode
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Define our WikiData API endpoints.
 * Get method to get json response from wikidata server.
 */
interface WikidataAPI {
    @GET("{code}.json")
    fun getWikiCategory(@Path("code") code: String): Single<ObjectNode>
}