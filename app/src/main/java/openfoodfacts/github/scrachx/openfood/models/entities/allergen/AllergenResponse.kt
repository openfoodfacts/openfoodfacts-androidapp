package openfoodfacts.github.scrachx.openfood.models.entities.allergen

/**
 * Intermediate class between [AllergensWrapper] and [Allergen]
 *
 * @author Lobster 2018-03-04
 * @author ross-holloway94 2018-03-14
 */
class AllergenResponse {
    var uniqueAllergenID: String
    var names: Map<String, String>
    private var wikiDataCode: String? = null
    private val isWikiDataIdPresent: Boolean

    /**
     * Constructor.
     *
     * @param uniqueAllergenId Unique ID of the allergen
     * @param names            Map of key: Country code, value: Translated name of allergen.
     * @param wikiDataCode     Code to look up allergen in wikidata
     */
    constructor(uniqueAllergenId: String, names: Map<String, String>, wikiDataCode: String?) {
        uniqueAllergenID = uniqueAllergenId
        this.names = names
        this.wikiDataCode = wikiDataCode
        isWikiDataIdPresent = true
    }

    /**
     * Constructor.
     *
     * @param uniqueAllergenId Unique ID of the allergen
     * @param names            Map of key: Country code, value: Translated name of allergen.
     */
    constructor(uniqueAllergenId: String, names: Map<String, String>) {
        uniqueAllergenID = uniqueAllergenId
        this.names = names
        isWikiDataIdPresent = false
    }

    /**
     * Converts an AllergenResponse object into a new Allergen object.
     *
     * @return The newly constructed Allergen object.
     */
    fun map() = if (isWikiDataIdPresent) {
        Allergen(uniqueAllergenID, arrayListOf(), wikiDataCode).also {
            names.forEach { (key, value) ->
                it.names.add(AllergenName(it.tag, key, value, wikiDataCode))
            }
        }

    } else {
        Allergen(uniqueAllergenID, arrayListOf()).also {
            names.forEach { (key, value) ->
                it.names.add(AllergenName(it.tag, key, value))
            }
        }

    }
}