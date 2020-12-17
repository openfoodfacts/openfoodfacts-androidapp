package openfoodfacts.github.scrachx.openfood.models.entities.ingredient

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractChildNodeAsText
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractMapFromJsonNode
import java.io.IOException
import java.util.*

/**
 * Custom deserializer for [IngredientsWrapper]
 *
 * @author dobriseb 2018-12-21 inspired by AllergensWrapperDeserializer
 */
class IngredientsWrapperDeserializer : StdDeserializer<IngredientsWrapper>(IngredientsWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): IngredientsWrapper {
        val ingredients = ArrayList<IngredientResponse>()
        val mainNode = jp.codec.readTree<JsonNode>(jp)
        val mainNodeIterator = mainNode.fields()
        while (mainNodeIterator.hasNext()) {
            val subNode = mainNodeIterator.next()
            val namesNode = subNode.value[DeserializerHelper.NAMES_KEY]
            if (namesNode != null) {
                val names = extractMapFromJsonNode(namesNode)
                val parents = extractChildNodeAsText(subNode, DeserializerHelper.PARENTS_KEY)
                val children = extractChildNodeAsText(subNode, DeserializerHelper.CHILDREN_KEY)
                val wikiData = if (subNode.value.has(DeserializerHelper.WIKIDATA_KEY)) subNode.value[DeserializerHelper.WIKIDATA_KEY].toString() else null
                ingredients.add(IngredientResponse(subNode.key!!, names, parents, children, wikiData))
            }
        }
        return IngredientsWrapper(ingredients)
    }
}