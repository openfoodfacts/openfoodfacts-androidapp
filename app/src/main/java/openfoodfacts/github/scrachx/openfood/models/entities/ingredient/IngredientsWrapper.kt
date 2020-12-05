package openfoodfacts.github.scrachx.openfood.models.entities.ingredient

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/data/taxonomies/ingredients.json
 *
 * @author dobriseb 2018-12-21 inspired by AllergensWrapper
 */
@JsonDeserialize(using = IngredientsWrapperDeserializer::class)
class IngredientsWrapper(var ingredients: List<IngredientResponse>) {

    /**
     * @return A list of Ingredient objects
     */
    fun map() = ingredients.map { it.map() }
}