package openfoodfacts.github.scrachx.openfood.models.entities.label

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractMapFromJsonNode
import java.io.IOException
import java.util.*

/*
 * Created by Lobster on 03.03.18.
 */
/**
 * Custom deserializer for LabelsWrapper
 */
class LabelsWrapperDeserializer : StdDeserializer<LabelsWrapper>(LabelsWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): LabelsWrapper {
        val labels = ArrayList<LabelResponse>()
        val mainNode = jp.codec.readTree<JsonNode>(jp)
        val mainNodeIterator = mainNode.fields()
        while (mainNodeIterator.hasNext()) {
            val subNode = mainNodeIterator.next()
            val namesNode = subNode.value[DeserializerHelper.NAMES_KEY]
            if (namesNode != null) {
                val names = extractMapFromJsonNode(namesNode)
                if (subNode.value.has(DeserializerHelper.WIKIDATA_KEY)) {
                    labels.add(LabelResponse(subNode.key, names, subNode.value[DeserializerHelper.WIKIDATA_KEY].toString()))
                } else {
                    labels.add(LabelResponse(subNode.key, names))
                }
            }
        }
        return LabelsWrapper(labels)
    }
}