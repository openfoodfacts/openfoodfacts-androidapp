package openfoodfacts.github.scrachx.openfood.category.mapper

import openfoodfacts.github.scrachx.openfood.category.model.Category
import openfoodfacts.github.scrachx.openfood.category.network.CategoryResponse
import javax.inject.Inject

/**
 * Class used to map tag name with the corresponding categories
 */
class CategoryMapper @Inject constructor() {
    /**
     * Returns list of Category objects using the tags
     * @param  tags List of CategoryResponse.Tag object
     */
    fun fromNetwork(tags: List<CategoryResponse.Tag>) =
        tags.map { Category(it.id, it.name, it.url, it.products) }
            .sortedBy { it.name }
}