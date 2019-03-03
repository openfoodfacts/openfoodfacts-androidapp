
package openfoodfacts.github.scrachx.openfood.network.deserializers;

import android.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.models.IngredientResponse;
import openfoodfacts.github.scrachx.openfood.models.IngredientsWrapper;

/**
 * Custom deserializer for {@link IngredientsWrapper IngredientsWrapper}
 *
 * @author dobriseb 2018-12-21 inspired by AllergensWrapperDeserializer
 */

public class IngredientsWrapperDeserializer extends StdDeserializer<IngredientsWrapper> {


    private static final String NAMES_KEY = "name";
    private static final String WIKIDATA_KEY = "wikidata";
    private static final String PARENTS_KEY = "parents";
    private static final String CHILDREN_KEY = "children";

    public IngredientsWrapperDeserializer() {
        super(IngredientsWrapper.class);
    }

    @Override
    public IngredientsWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<IngredientResponse> ingredients = new ArrayList<>();

        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            JsonNode namesNode = subNode.getValue().get(NAMES_KEY);
            Boolean isWikiNodePresent = subNode.getValue().has(WIKIDATA_KEY);

            if (namesNode != null) {
                Map<String, String> names =
                        new HashMap<>();  /* Entry<Language Code, Product Name> */
                Iterator<Map.Entry<String, JsonNode>> nameNodeIterator = namesNode.fields();
                while (nameNodeIterator.hasNext()) {
                    Map.Entry<String, JsonNode> nameNode = nameNodeIterator.next();
                    String name = nameNode.getValue().asText();
                    names.put(nameNode.getKey(), name);
                }

                Map<String, String> parents = new HashMap<>();  /* Entry<parentTag> */
                JsonNode parentsNode = subNode.getValue().get(PARENTS_KEY);
                if (parentsNode != null) {
                    Iterator<JsonNode> parentsElementsIterator = parentsNode.elements();
                    while (parentsElementsIterator.hasNext()) {
                        JsonNode parentNode = parentsElementsIterator.next();
                        String parent = parentNode.asText();
                        parents.put(subNode.getKey(), parent);
                    }
                }
                Map<String, String> children = new HashMap<>();  /* Entry<parentTag> */
                JsonNode childrenNode = subNode.getValue().get(CHILDREN_KEY);
                if (childrenNode != null) {
                    Iterator<JsonNode> childElementIterator = childrenNode.elements();
                    while (childElementIterator.hasNext()) {
                        JsonNode childNode = childElementIterator.next();
                        String child = childNode.asText();
                        children.put(subNode.getKey(), child);
                    }
                }

                String wikiData = isWikiNodePresent ? subNode.getValue().get(WIKIDATA_KEY).toString() : null;
                ingredients.add(new IngredientResponse(subNode.getKey(), names, parents, children, wikiData));
            }
        }


        IngredientsWrapper wrapper = new IngredientsWrapper();
        wrapper.setIngredients(ingredients);

        return wrapper;
    }
}