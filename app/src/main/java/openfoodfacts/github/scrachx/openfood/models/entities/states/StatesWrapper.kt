package openfoodfacts.github.scrachx.openfood.models.entities.states

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
import openfoodfacts.github.scrachx.openfood.models.entities.EntityWrapper


@JsonDeserialize(using = StatesWrapperDeserializer::class)
class StatesWrapper (responses: List<EntityResponse<States>>) : EntityWrapper<States>(responses)
