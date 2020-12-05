package openfoodfacts.github.scrachx.openfood.category

import android.util.Log
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper
import openfoodfacts.github.scrachx.openfood.category.model.Category
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService
import java.util.concurrent.atomic.AtomicReference

/**
 * This class receives list of all categories using CategoryNetworkService
 */
class CategoryRepository(private val networkService: CategoryNetworkService, private val mapper: CategoryMapper) {
    private val memoryCache: AtomicReference<List<Category>?> = AtomicReference()

    /**
     * Calling this function retrieves list of all categories from NetworkService
     */
    fun retrieveAll(): Single<List<Category>?> {
        return if (memoryCache.get() != null) {
            Single.just(memoryCache.get())
        } else networkService.getCategories()
                .map { categoryResponse -> mapper.fromNetwork(categoryResponse.tags) }
                .doOnSuccess { newValue -> memoryCache.set(newValue) }
                .doOnError { throwable -> Log.w(CategoryRepository::class.java.simpleName, "Can't get categories", throwable) }
                .subscribeOn(Schedulers.io())
    }

}