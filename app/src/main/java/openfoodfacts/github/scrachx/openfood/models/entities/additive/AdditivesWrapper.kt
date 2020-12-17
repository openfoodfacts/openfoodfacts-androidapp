package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
import openfoodfacts.github.scrachx.openfood.models.entities.EntityWrapper

/**
 * Created by Lobster on 04.03.18.
 */
@JsonDeserialize(using = AdditivesWrapperDeserializer::class)
class AdditivesWrapper(additives: List<EntityResponse<Additive>>) : EntityWrapper<Additive>(additives)