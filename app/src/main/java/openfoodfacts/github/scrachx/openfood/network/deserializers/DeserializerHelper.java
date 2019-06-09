package openfoodfacts.github.scrachx.openfood.network.deserializers;

import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

class DeserializerHelper {
    static final String NAMES_KEY = "name";
    static final String WIKIDATA_KEY = "wikidata";
    static final String EN_KEY = "en";
    static final String PARENTS_KEY = "parents";
    static final String CHILDREN_KEY = "children";

    private DeserializerHelper() {
        //helper class.
    }

    /**
     * Extracts names form the names node in the Json Response
     * @param namesNode namesNode in Json response
     * */

    static Map<String, String> extractNames(JsonNode namesNode) {
        Map<String, String> names = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> nameNodeIterator = namesNode.fields();
        while (nameNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> nameNode = nameNodeIterator.next();
            String name = nameNode.getValue().asText();
            names.put(nameNode.getKey(), name);
        }
        return names;
    }

    /**
     * extracts child nodes from a map of subnodes
     * @param subNode map of subnodes
     * @param key get the JsonNode for the given key
     * */

    static List<String> extractChildNodeAsText(Map.Entry<String, JsonNode> subNode, String key) {
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
