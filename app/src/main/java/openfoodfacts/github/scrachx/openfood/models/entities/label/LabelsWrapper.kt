package openfoodfacts.github.scrachx.openfood.models.entities.label

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
import openfoodfacts.github.scrachx.openfood.models.entities.EntityWrapper

/**
 * Created by Lobster on 03.03.18.
 */
@JsonDeserialize(using = LabelsWrapperDeserializer::class)
class LabelsWrapper(labels: List<EntityResponse<Label>>) : EntityWrapper<Label>(labels)