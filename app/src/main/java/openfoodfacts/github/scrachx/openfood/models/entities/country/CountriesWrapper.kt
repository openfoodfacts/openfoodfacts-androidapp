package openfoodfacts.github.scrachx.openfood.models.entities.country

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
import openfoodfacts.github.scrachx.openfood.models.entities.EntityWrapper

/**
 * Created by Lobster on 04.03.18.
 */
@JsonDeserialize(using = CountriesWrapperDeserializer::class)
class CountriesWrapper(responses: List<EntityResponse<Country>>) : EntityWrapper<Country>(responses)