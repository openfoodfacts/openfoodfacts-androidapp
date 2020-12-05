package openfoodfacts.github.scrachx.openfood.models.entities.country

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractMapFromJsonNode
import java.io.IOException
import java.util.*

/**
 * custom deserializer for CountriesWrapper
 */
class CountriesWrapperDeserializer : StdDeserializer<CountriesWrapper>(CountriesWrapper::class.java) {

    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): CountriesWrapper {
        val countries: MutableList<CountryResponse> = ArrayList()
        val mainNode = parser.codec.readTree<JsonNode>(parser)
        val mainNodeIterator = mainNode.fields()
        while (mainNodeIterator.hasNext()) {
            val countryNode = mainNodeIterator.next()
            val namesNode = countryNode.value[DeserializerHelper.NAMES_KEY]
            val cc2Node = countryNode.value[DeserializerHelper.COUNTRY_CODE_2_KEY]
            val cc3Node = countryNode.value[DeserializerHelper.COUNTRY_CODE_3_KEY]
            var names: Map<String, String>? = null
            var cc2: Map<String, String>? = null
            var cc3: Map<String, String>? = null
            if (namesNode != null) {
                names = extractMapFromJsonNode(namesNode)
            }
            if (cc2Node != null) {
                cc2 = extractMapFromJsonNode(cc2Node)
            }
            if (cc3Node != null) {
                cc3 = extractMapFromJsonNode(cc3Node)
            }
            if (names != null && cc2 != null && cc3 != null) {
                countries.add(CountryResponse(countryNode.key, names, cc2, cc3))
            }
        }
        return CountriesWrapper(countries)
    }
}