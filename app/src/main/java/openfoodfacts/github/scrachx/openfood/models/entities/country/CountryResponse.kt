package openfoodfacts.github.scrachx.openfood.models.entities.country

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
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
) : EntityResponse<Country> {
    override fun map() = Country(tag,
            names.map { CountryName(tag, it.key, it.value) },
            cc2[ApiFields.Defaults.DEFAULT_TAXO_PREFIX],
            cc3[ApiFields.Defaults.DEFAULT_TAXO_PREFIX])
}