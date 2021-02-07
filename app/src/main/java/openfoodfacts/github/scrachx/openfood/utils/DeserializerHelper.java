package openfoodfacts.github.scrachx.openfood.utils;

import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DeserializerHelper {
    public static final String NAMES_KEY = "name";
    public static final String COUNTRY_CODE_2_KEY = "country_code_2";
    public static final String COUNTRY_CODE_3_KEY = "country_code_3";
    public static final String WIKIDATA_KEY = "wikidata";
    public static final String EN_KEY = "en";
    public static final String PARENTS_KEY = "parents";
    public static final String CHILDREN_KEY = "children";
    public static final String SHOW_INGREDIENTS_KEY = "show_ingredients";
    public static final String TYPE_KEY = "type";
    public static final String ICON_KEY = "icon";
    public static final String COLOR_KEY = "color";

    private DeserializerHelper() {
        //helper class.
    }

    /**
     * Extracts names form the names node in the Json Response
     *
     * @param namesNode namesNode in Json response
     */
    public static Map<String, String> extractMapFromJsonNode(JsonNode namesNode) {
        Map<String, String> names = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> nameNodeIterator = namesNode.fields();
        while (nameNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> nameNode = nameNodeIterator.next();
            names.put(nameNode.getKey(), nameNode.getValue().asText());
        }
        return names;
    }

    /**
     * Extracts child nodes from a map of subnodes
     *
     * @param subNode map of subnodes
     * @param key get the JsonNode for the given key
     */
    public static List<String> extractChildNodeAsText(Map.Entry<String, JsonNode> subNode, String key) {
        List<String> stringList = new ArrayList<>();
        JsonNode jsonNode = subNode.getValue().get(key);
        if (jsonNode != null) {
            Iterator<JsonNode> parentsElementsIterator = jsonNode.elements();
            while (parentsElementsIterator.hasNext()) {
                JsonNode parentNode = parentsElementsIterator.next();
                stringList.add(parentNode.asText());
                if (Log.isLoggable(DeserializerHelper.class.getSimpleName(), Log.INFO)) {
                    Log.i(DeserializerHelper.class.getSimpleName(), "extractChildNodeAsText, ajout de " + parentNode.asText());
                }
            }
        }
        return stringList;
    }
}
