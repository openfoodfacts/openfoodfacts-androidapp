package openfoodfacts.github.scrachx.openfood.category.network

import retrofit2.http.GET

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
interface CategoryNetworkService {
    @GET("categories.json")
    suspend fun getCategories(): CategoryResponse
}