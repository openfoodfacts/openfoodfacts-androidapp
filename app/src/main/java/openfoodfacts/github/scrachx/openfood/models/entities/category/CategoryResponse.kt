package openfoodfacts.github.scrachx.openfood.models.entities.category

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse

/**
 * Created by Lobster on 04.03.18.
 */
class CategoryResponse(
        private val code: String,
        private val names: Map<String, String>,
        private var wikiDataCode: String? = null
) : EntityResponse<Category> {

    override fun map(): Category {
        val category: Category
        if (wikiDataCode != null) {
            category = Category(code, arrayListOf(), wikiDataCode)
            names.forEach { (key, value) ->
                category.names.add(CategoryName(category.tag, key, value, wikiDataCode))
            }
        } else {
            category = Category(code, arrayListOf())
            names.forEach { (key, value) ->
                category.names.add(CategoryName(category.tag, key, value))
            }
        }
        return category
    }
}