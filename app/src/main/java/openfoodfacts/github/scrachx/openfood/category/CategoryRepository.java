package openfoodfacts.github.scrachx.openfood.category;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.category.mapper.CategoryMapper;
import openfoodfacts.github.scrachx.openfood.category.model.Category;
import openfoodfacts.github.scrachx.openfood.category.network.CategoryNetworkService;

public class CategoryRepository {
    private final CategoryNetworkService networkService;
    private final CategoryMapper mapper;
    private final AtomicReference<List<Category>> memoryCache;

    public CategoryRepository(CategoryNetworkService networkService, CategoryMapper mapper) {
        this.networkService = networkService;
        this.mapper = mapper;
        memoryCache = new AtomicReference<>();
    }

    public Single<List<Category>> retrieveAll() {
        if (memoryCache.get() != null) {
            return Single.just(memoryCache.get());
        }
        return networkService.getCategories()
                .map(categoryResponse -> mapper.fromNetwork(categoryResponse.getTags()))
                .doOnSuccess(memoryCache::set)
                .subscribeOn(Schedulers.io());
    }
}
