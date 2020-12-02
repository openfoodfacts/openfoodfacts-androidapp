package openfoodfacts.github.scrachx.openfood.models.entities.country

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * Created by Lobster on 04.03.18.
 */
@JsonDeserialize(using = CountriesWrapperDeserializer::class)
class CountriesWrapper(var responses: List<CountryResponse>) {
    fun map() = responses.map { it.map() }
}