package openfoodfacts.github.scrachx.openfood.models.entities.ingredient

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
import openfoodfacts.github.scrachx.openfood.models.entities.EntityWrapper

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/data/taxonomies/ingredients.json
 *
 * @author dobriseb 2018-12-21 inspired by AllergensWrapper
 */
@JsonDeserialize(using = IngredientsWrapperDeserializer::class)
class IngredientsWrapper(ingredients: List<EntityResponse<Ingredient>>) : EntityWrapper<Ingredient>(ingredients)