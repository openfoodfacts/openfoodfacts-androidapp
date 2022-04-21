package openfoodfacts.github.scrachx.openfood.models.entities.store

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import java.io.IOException


class StoresWrapperDeserializer  : StdDeserializer<StoresWrapper>(StoresWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): StoresWrapper {
        val stores = ArrayList<StoreResponse>()
        jp.codec.readTree<JsonNode>(jp).fields().forEach { (key, value) ->
            val namesNode = value[DeserializerHelper.NAMES_KEY]
            if (namesNode != null) {
                val names = DeserializerHelper.extractMapFromJsonNode(namesNode)
                if (value.has(DeserializerHelper.WIKIDATA_KEY)) {
                    stores.add(StoreResponse(key, names, value[DeserializerHelper.WIKIDATA_KEY].toString()))
                } else {
                    stores.add(StoreResponse(key, names))
                }
            }
        }
        return StoresWrapper(stores)
    }
}