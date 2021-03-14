package openfoodfacts.github.scrachx.openfood.category

import android.util.Log
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper
import openfoodfacts.github.scrachx.openfood.category.model.Category
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class receives list of all categories using CategoryNetworkService
 */
@Singleton
class CategoryRepository @Inject constructor(
        private val networkService: CategoryNetworkService,
        private val mapper: CategoryMapper
) {
    private val memoryCache = AtomicReference<List<Category>?>()

    /**
     * Calling this function retrieves list of all categories from NetworkService
     */
    fun retrieveAll() = if (memoryCache.get() != null) {
        Single.just(memoryCache.get())
    } else {
        networkService.getCategories()
                .map { mapper.fromNetwork(it.tags) }
                .doOnSuccess { memoryCache.set(it) }
                .doOnError { Log.w(LOG_TAG, "Can't get categories", it) }
                .subscribeOn(Schedulers.io())
    }

    companion object {
        val LOG_TAG = CategoryRepository::class.simpleName
    }

}