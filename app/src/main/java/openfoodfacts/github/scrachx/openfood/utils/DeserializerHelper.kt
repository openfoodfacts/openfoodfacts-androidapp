package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import com.fasterxml.jackson.databind.JsonNode

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
    fun extractMapFromJsonNode(namesNode: JsonNode): Map<String, String> {
        val names = hashMapOf<String, String>()
        namesNode.fields().forEach { names[it.key] = it.value.asText() }
        return names
    }

    /**
     * Extracts child nodes from a map of subnodes
     *
     * @param subNode map of subnodes
     * @param key get the JsonNode for the given key
     */
    fun extractChildNodeAsText(subNode: Map.Entry<String?, JsonNode>, key: String?) =
            subNode.value[key]?.toList()?.map {
                if (Log.isLoggable(LOG_TAG, Log.INFO)) Log.i(LOG_TAG, "ExtractChildNodeAsText, ajout de ${it.asText()}")
                it.asText()
            } ?: listOf()

    private val LOG_TAG = DeserializerHelper::class.simpleName!!
}