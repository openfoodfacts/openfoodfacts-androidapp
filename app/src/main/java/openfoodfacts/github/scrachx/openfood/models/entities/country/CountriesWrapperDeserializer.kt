package openfoodfacts.github.scrachx.openfood.models.entities.country

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractMapFromJsonNode
import java.io.IOException

/**
 * custom deserializer for CountriesWrapper
 */
class CountriesWrapperDeserializer : StdDeserializer<CountriesWrapper>(CountriesWrapper::class.java) {

    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): CountriesWrapper {
        val countries = ArrayList<CountryResponse>()
        parser.codec.readTree<JsonNode>(parser).fields().forEach { (key, value) ->
            val namesNode = value[DeserializerHelper.NAMES_KEY]
            val cc2Node = value[DeserializerHelper.COUNTRY_CODE_2_KEY]
            val cc3Node = value[DeserializerHelper.COUNTRY_CODE_3_KEY]

            val names: Map<String, String>? = if (namesNode != null) extractMapFromJsonNode(namesNode) else null
            val cc2: Map<String, String>? = if (cc2Node != null) extractMapFromJsonNode(cc2Node) else null
            val cc3: Map<String, String>? = if (cc3Node != null) extractMapFromJsonNode(cc3Node) else null

            if (names != null && cc2 != null && cc3 != null) {
                countries.add(CountryResponse(key, names, cc2, cc3))
            }
        }
        return CountriesWrapper(countries)
    }
}