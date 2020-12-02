package openfoodfacts.github.scrachx.openfood.models.entities.category

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * Created by Lobster on 04.03.18.
 */
@JsonDeserialize(using = CategoriesWrapperDeserializer::class)
class CategoriesWrapper(var categories: List<CategoryResponse>) {
    fun map() = categories.map { it.map() }
}