package openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagsWrapper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import java.io.IOException

/**
 * Custom deserializer for [AnalysisTagsWrapper][AnalysisTagConfigsWrapper]
 *
 * @author Rares
 */
class AnalysisTagConfigsWrapperDeserializer : StdDeserializer<AnalysisTagConfigsWrapper>(AnalysisTagsWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): AnalysisTagConfigsWrapper {
        val analysisTagConfigs: MutableList<AnalysisTagConfig> = ArrayList()
        jp.codec.readTree<JsonNode>(jp).fields().forEach { (key, value) ->
            val type = value[DeserializerHelper.TYPE_KEY].asText()
            val icon = value[DeserializerHelper.ICON_KEY].asText()
            val color = value[DeserializerHelper.COLOR_KEY].asText()
            analysisTagConfigs.add(AnalysisTagConfig(key, type, icon, color))
        }
        return AnalysisTagConfigsWrapper(analysisTagConfigs)
    }
}