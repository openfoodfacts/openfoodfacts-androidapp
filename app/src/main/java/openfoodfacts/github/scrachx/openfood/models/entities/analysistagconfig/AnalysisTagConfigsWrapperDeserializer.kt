package openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagsWrapper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import java.io.IOException
import java.util.*

/**
 * Custom deserializer for [AnalysisTagsWrapper][AnalysisTagConfigsWrapper]
 *
 * @author Rares
 */
class AnalysisTagConfigsWrapperDeserializer : StdDeserializer<AnalysisTagConfigsWrapper>(AnalysisTagsWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): AnalysisTagConfigsWrapper {
        val analysisTagConfigs: MutableList<AnalysisTagConfig> = ArrayList()
        val mainNode = jp.codec.readTree<JsonNode>(jp)
        val mainNodeIterator = mainNode.fields()
        while (mainNodeIterator.hasNext()) {
            val subNode = mainNodeIterator.next()
            val type = subNode.value[DeserializerHelper.TYPE_KEY].asText()
            val icon = subNode.value[DeserializerHelper.ICON_KEY].asText()
            val color = subNode.value[DeserializerHelper.COLOR_KEY].asText()
            analysisTagConfigs.add(AnalysisTagConfig(subNode.key, type, icon, color))
        }
        return AnalysisTagConfigsWrapper(analysisTagConfigs)
    }
}