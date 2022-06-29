package openfoodfacts.github.scrachx.openfood.models.entities.analysistag

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractMapFromJsonNode
import java.io.IOException

/**
 * Custom deserializer for [AnalysisTagsWrapper]
 *
 * @author Rares
 */
class AnalysisTagsWrapperDeserializer : StdDeserializer<AnalysisTagsWrapper>(AnalysisTagsWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): AnalysisTagsWrapper {
        val analysisTags = ArrayList<AnalysisTagResponse>()

        jp.codec.readTree<JsonNode>(jp).fields().forEach { (key, value) ->
            val namesNode = value[DeserializerHelper.NAMES_KEY]
            if (namesNode != null) {
                val names = extractMapFromJsonNode(namesNode)
                var showIngredients: Map<String, String> = HashMap()
                val showIngredientsNode = value[DeserializerHelper.SHOW_INGREDIENTS_KEY]
                if (showIngredientsNode != null) {
                    showIngredients = extractMapFromJsonNode(showIngredientsNode)
                }
                analysisTags.add(AnalysisTagResponse(key, names, showIngredients))
            }
        }
        return AnalysisTagsWrapper(analysisTags)
    }
}