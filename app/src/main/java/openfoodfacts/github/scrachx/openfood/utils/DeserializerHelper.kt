package openfoodfacts.github.scrachx.openfood.utils

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
    fun extractMapFromJsonNode(namesNode: JsonNode) = namesNode.fields()
        .asSequence()
        .map { it.key to it.value.asText() }
        .toMap()

    /**
     * Extracts child nodes from a map of subnodes
     *
     * @param subNode map of subnodes
     * @param key get the JsonNode for the given key
     */
    fun extractChildNodeAsText(subNode: Map.Entry<String?, JsonNode>, key: String?) =
        subNode.value[key]?.toList()?.map { it.asText() } ?: listOf()

}