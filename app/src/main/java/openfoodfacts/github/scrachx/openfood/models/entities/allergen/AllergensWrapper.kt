package openfoodfacts.github.scrachx.openfood.models.entities.allergen

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
import openfoodfacts.github.scrachx.openfood.models.entities.EntityWrapper

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/data/taxonomies/allergens.json (top 14 allergens and substances)
 *
 * @author Lobster
 * @author ross-holloway94 2018-03-14
 */
@JsonDeserialize(using = AllergensWrapperDeserializer::class)
class AllergensWrapper(allergens: List<EntityResponse<Allergen>>) : EntityWrapper<Allergen>(allergens)