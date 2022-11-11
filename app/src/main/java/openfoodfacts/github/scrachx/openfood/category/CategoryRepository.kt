package openfoodfacts.github.scrachx.openfood.category

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper
import openfoodfacts.github.scrachx.openfood.category.model.Category
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class receives list of all categories using [CategoryNetworkService]
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val networkService: CategoryNetworkService,
    private val mapper: CategoryMapper,
) {
    private val memoryCache = AtomicReference<List<Category>?>()

    /**
     * Calling this function retrieves list of all categories from NetworkService
     */
    suspend fun retrieveAll() = withContext(IO) {
        if (memoryCache.get() != null) {
            memoryCache.get()
        } else {
            networkService.getCategories()
                .let { mapper.fromNetwork(it.tags) }
                .also { memoryCache.set(it) }
        }
    }
}
