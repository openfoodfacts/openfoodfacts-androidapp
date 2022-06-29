package openfoodfacts.github.scrachx.openfood.models.entities.ingredient

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractChildNodeAsText
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper.extractMapFromJsonNode
import java.io.IOException

/**
 * Custom deserializer for [IngredientsWrapper]
 *
 * @author dobriseb 2018-12-21 inspired by AllergensWrapperDeserializer
 */
class IngredientsWrapperDeserializer : StdDeserializer<IngredientsWrapper>(IngredientsWrapper::class.java) {
    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): IngredientsWrapper {
        val ingredients = ArrayList<IngredientResponse>()
        jp.codec.readTree<JsonNode>(jp).fields().forEach {
            val namesNode = it.value[DeserializerHelper.NAMES_KEY]
            if (namesNode != null) {
                val names = extractMapFromJsonNode(namesNode)
                val parents = extractChildNodeAsText(it, DeserializerHelper.PARENTS_KEY)
                val children = extractChildNodeAsText(it, DeserializerHelper.CHILDREN_KEY)
                val wikiData = if (it.value.has(DeserializerHelper.WIKIDATA_KEY)) it.value[DeserializerHelper.WIKIDATA_KEY].toString() else null
                ingredients.add(IngredientResponse(it.key!!, names, parents, children, wikiData))
            }
        }
        return IngredientsWrapper(ingredients)
    }
}