package openfoodfacts.github.scrachx.openfood.category.network;

import io.reactivex.Single;
import retrofit2.http.GET;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
@FunctionalInterface
public interface CategoryNetworkService {

    @GET("categories.json")
    Single<CategoryResponse> getCategories();
}
