package openfoodfacts.github.scrachx.openfood.models.entities.label

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * Created by Lobster on 03.03.18.
 */
@JsonDeserialize(using = LabelsWrapperDeserializer::class)
class LabelsWrapper(var labels: List<LabelResponse>) {
    fun map() = labels.map { it.map() }
}