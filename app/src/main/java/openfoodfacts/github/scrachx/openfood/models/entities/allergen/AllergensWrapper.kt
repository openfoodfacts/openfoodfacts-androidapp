package openfoodfacts.github.scrachx.openfood.models.entities.allergen

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/data/taxonomies/allergens.json (top 14 allergens and substances)
 *
 * @author Lobster
 * @author ross-holloway94 2018-03-14
 */
@JsonDeserialize(using = AllergensWrapperDeserializer::class)
class AllergensWrapper(var allergens: List<AllergenResponse>) {

    /**
     * @return A list of Allergen objects
     */
    fun map() = allergens.map { it.map() }
}