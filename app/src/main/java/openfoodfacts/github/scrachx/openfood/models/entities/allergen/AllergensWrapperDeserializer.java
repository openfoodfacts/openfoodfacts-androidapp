
package openfoodfacts.github.scrachx.openfood.models.entities.allergen;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper;

/**
 * Custom deserializer for {@link AllergensWrapper AllergensWrapper}
 *
 * @author Lobster 2018-03-04
 * @author ross-holloway94 2018-03-14
 */
public class AllergensWrapperDeserializer extends StdDeserializer<AllergensWrapper> {
    public AllergensWrapperDeserializer() {
        super(AllergensWrapper.class);
    }

    @Override
    public AllergensWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<AllergenResponse> allergens = new ArrayList<>();

        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
           final Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            JsonNode namesNode = subNode.getValue().get(DeserializerHelper.NAMES_KEY);

            if (namesNode != null) {
                Map<String, String> names = DeserializerHelper.extractMapFromJsonNode(namesNode);

                if (subNode.getValue().has(DeserializerHelper.WIKIDATA_KEY)) {
                    allergens.add(new AllergenResponse(subNode.getKey(), names, subNode.getValue().get(DeserializerHelper.WIKIDATA_KEY).toString()));
                } else {
                    allergens.add(new AllergenResponse(subNode.getKey(), names));
                }
            }
        }

        AllergensWrapper wrapper = new AllergensWrapper();
        wrapper.setAllergens(allergens);

        return wrapper;
    }
}
