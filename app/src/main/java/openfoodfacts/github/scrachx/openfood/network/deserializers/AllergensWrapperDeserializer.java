
package openfoodfacts.github.scrachx.openfood.network.deserializers;

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

import openfoodfacts.github.scrachx.openfood.models.AllergenResponse;
import openfoodfacts.github.scrachx.openfood.models.AllergensWrapper;

/**
 * Custom deserializer for {@link openfoodfacts.github.scrachx.openfood.models.AllergensWrapper AllergensWrapper}
 *
 * @author Lobster 2018-03-04
 * @author ross-holloway94 2018-03-14
 */

public class AllergensWrapperDeserializer extends StdDeserializer<AllergensWrapper> {


    private static final String NAMES_KEY = "name";

    public AllergensWrapperDeserializer() {
        super(AllergensWrapper.class);
    }

    @Override
    public AllergensWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<AllergenResponse> allergens = new ArrayList<>();

        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            JsonNode namesNode = subNode.getValue().get(NAMES_KEY);
            if (namesNode != null) {
                Map<String, String> names = new HashMap<>();  /* Entry<Language Code, Product Name> */
                Iterator<Map.Entry<String, JsonNode>> nameNodeIterator = namesNode.fields();
                while (nameNodeIterator.hasNext()) {
                    Map.Entry<String, JsonNode> nameNode = nameNodeIterator.next();
                    String name = nameNode.getValue().asText();
                    names.put(nameNode.getKey(), name);

                }

                allergens.add(new AllergenResponse(subNode.getKey(), names));
            }
        }


        AllergensWrapper wrapper = new AllergensWrapper();
        wrapper.setAllergens(allergens);

        return wrapper;
    }
}