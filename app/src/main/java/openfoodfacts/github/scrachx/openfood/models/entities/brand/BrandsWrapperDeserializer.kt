package openfoodfacts.github.scrachx.openfood.models.entities.brand

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import java.io.IOException

class BrandsWrapperDeserializer : StdDeserializer<BrandsWrapper>(BrandsWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): BrandsWrapper {
        val brands = ArrayList<BrandResponse>()
        jp.codec.readTree<JsonNode>(jp).fields().forEach { (key, value) ->
            val namesNode = value[DeserializerHelper.NAMES_KEY]
            if (namesNode != null) {
                val names = DeserializerHelper.extractMapFromJsonNode(namesNode)
                if (value.has(DeserializerHelper.WIKIDATA_KEY)) {
                    brands.add(BrandResponse(key, names, value[DeserializerHelper.WIKIDATA_KEY].toString()))
                } else {
                    brands.add(BrandResponse(key, names))
                }
            }
        }
        return BrandsWrapper(brands)
    }
}