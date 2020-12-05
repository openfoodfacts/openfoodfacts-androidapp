package openfoodfacts.github.scrachx.openfood.models.entities.country

import openfoodfacts.github.scrachx.openfood.network.ApiFields

/**
 * Created by Lobster on 04.03.18.
 */
// MUST represent the entire JSON object as given by the API
class CountryResponse(
        private val tag: String,
        private val names: Map<String, String>,
        private val cc2: Map<String, String>,
        private val cc3: Map<String, String>
) {
    fun map(): Country {
        val country = Country(tag,
                arrayListOf(),
                cc2[ApiFields.Defaults.DEFAULT_TAXO_PREFIX],
                cc3[ApiFields.Defaults.DEFAULT_TAXO_PREFIX])
        names.forEach { (key, value) ->
            country.names.add(CountryName(country.tag, key, value))
        }
        return country
    }
}