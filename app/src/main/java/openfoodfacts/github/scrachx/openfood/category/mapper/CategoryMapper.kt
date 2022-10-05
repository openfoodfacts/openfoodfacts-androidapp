package openfoodfacts.github.scrachx.openfood.category.mapper

import openfoodfacts.github.scrachx.openfood.category.model.Category
import openfoodfacts.github.scrachx.openfood.category.network.CategoryResponse
import javax.inject.Inject

/**
 * Class used to map tag name with the corresponding categories
 */
class CategoryMapper @Inject constructor() {
    /**
     * @param tags a [List] of [CategoryResponse.Tag] for mapping.
     * @return a list of [Category] ordered by name.
     */
    fun fromNetwork(tags: List<CategoryResponse.Tag>): List<Category> {
        return tags
            .map { Category(it.id, it.name, it.url, it.products) }
            .sortedBy { it.name }
    }
}
