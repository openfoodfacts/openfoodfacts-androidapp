package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import com.fasterxml.jackson.databind.JsonNode
import java.util.*

object DeserializerHelper {
    const val NAMES_KEY = "name"
    const val COUNTRY_CODE_2_KEY = "country_code_2"
    const val COUNTRY_CODE_3_KEY = "country_code_3"
    const val WIKIDATA_KEY = "wikidata"
    const val EN_KEY = "en"
    const val PARENTS_KEY = "parents"
    const val CHILDREN_KEY = "children"
    const val SHOW_INGREDIENTS_KEY = "show_ingredients"
    const val TYPE_KEY = "type"
    const val ICON_KEY = "icon"
    const val COLOR_KEY = "color"

    /**
     * Extracts names form the names node in the Json Response
     *
     * @param namesNode namesNode in Json response
     */
    @JvmStatic
    fun extractMapFromJsonNode(namesNode: JsonNode): Map<String, String> {
        val names: MutableMap<String, String> = hashMapOf()
        val nameNodeIterator = namesNode.fields()
        while (nameNodeIterator.hasNext()) {
            val nameNode = nameNodeIterator.next()
            names[nameNode.key] = nameNode.value.asText()
        }
        return names
    }

    private val LOG_TAG = DeserializerHelper::class.simpleName!!

    /**
     * Extracts child nodes from a map of subnodes
     *
     * @param subNode map of subnodes
     * @param key get the JsonNode for the given key
     */
    @JvmStatic
    fun extractChildNodeAsText(subNode: Map.Entry<String?, JsonNode>, key: String?): List<String> {
        return subNode.value[key]?.toList()?.map { parentNode: JsonNode ->
            if (Log.isLoggable(LOG_TAG, Log.INFO)) {
                Log.i(LOG_TAG, "extractChildNodeAsText, ajout de ${parentNode.asText()}")
            }
            return@map parentNode.asText()
        } ?: listOf()
    }

}