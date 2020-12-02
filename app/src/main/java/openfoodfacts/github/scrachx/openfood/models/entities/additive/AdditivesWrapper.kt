package openfoodfacts.github.scrachx.openfood.models.entities.additive

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.*

/**
 * Created by Lobster on 04.03.18.
 */
@JsonDeserialize(using = AdditivesWrapperDeserializer::class)
class AdditivesWrapper(var additives: List<AdditiveResponse>? = null) {
    fun map(): List<Additive> {
        val entityLabels: MutableList<Additive> = ArrayList()
        for (additive in additives!!) {
            entityLabels.add(additive.map())
        }
        return entityLabels
    }
}