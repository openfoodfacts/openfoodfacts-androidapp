package openfoodfacts.github.scrachx.openfood.models.entities.allergen

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse

/**
 * Intermediate class between [AllergensWrapper] and [Allergen]
 *
 * @author Lobster 2018-03-04
 * @author ross-holloway94 2018-03-14
 */
class AllergenResponse(
        private val uniqueAllergenID: String,
        private val names: Map<String, String>,
        private var wikiDataCode: String? = null
) : EntityResponse<Allergen> {

    /**
     * Converts an AllergenResponse object into a new Allergen object.
     *
     * @return The newly constructed Allergen object.
     */
    override fun map() = if (wikiDataCode != null) {
        Allergen(uniqueAllergenID, arrayListOf(), wikiDataCode).also {
            names.forEach { (lc, name) -> it.names += AllergenName(it.tag, lc, name, wikiDataCode) }
        }
    } else {
        Allergen(uniqueAllergenID, arrayListOf()).also {
            names.forEach { (lc, name) -> it.names += AllergenName(it.tag, lc, name) }
        }
    }
}