package openfoodfacts.github.scrachx.openfood.models.entities.category

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractMapFromJsonNode
import java.io.IOException

/*
* Created by Lobster on 03.03.18.
*/
/**
 * custom deserializer for CategoriesWrapper
 */
class CategoriesWrapperDeserializer : StdDeserializer<CategoriesWrapper>(CategoriesWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): CategoriesWrapper {
        val categories = arrayListOf<CategoryResponse>()
        jp.codec.readTree<JsonNode>(jp).fields().forEach { (key, value) ->
            val namesNode = value[DeserializerHelper.NAMES_KEY]
            if (namesNode != null) {
                val names = extractMapFromJsonNode(namesNode)
                if (value.has(DeserializerHelper.WIKIDATA_KEY)) {
                    categories.add(CategoryResponse(key, names, value[DeserializerHelper.WIKIDATA_KEY].toString()))
                } else {
                    categories.add(CategoryResponse(key, names))
                }
            }
        }
        return CategoriesWrapper(categories)
    }
}