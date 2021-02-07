package openfoodfacts.github.scrachx.openfood.models.entities.states

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import java.io.IOException


class StatesWrapperDeserializer: StdDeserializer<StatesWrapper>(StatesWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): StatesWrapper {
        val states = arrayListOf<StateResponse>()
        parser.codec.readTree<JsonNode>(parser).fields().forEach { (key, value) ->
            val namesNode = value[DeserializerHelper.NAMES_KEY]

            if (namesNode != null) {
                val names = DeserializerHelper.extractMapFromJsonNode(namesNode)
                    states.add(StateResponse(key, names))
                }
            }
        return StatesWrapper(states)
    }
}