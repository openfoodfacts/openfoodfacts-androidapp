package openfoodfacts.github.scrachx.openfood.network.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import openfoodfacts.github.scrachx.openfood.models.IngredientResponse;
import openfoodfacts.github.scrachx.openfood.models.IngredientsWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Custom deserializer for {@link IngredientsWrapper IngredientsWrapper}
 *
 * @author dobriseb 2018-12-21 inspired by AllergensWrapperDeserializer
 */
public class IngredientsWrapperDeserializer extends StdDeserializer<IngredientsWrapper> {
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
            JsonNode namesNode = subNode.getValue().get(DeserializerHelper.NAMES_KEY);

            if (namesNode != null) {
                Map<String, String> names = DeserializerHelper.extractNames(namesNode);
                List<String> parents = DeserializerHelper.extractChildNodeAsText(subNode, DeserializerHelper.PARENTS_KEY);
                List<String> children = DeserializerHelper.extractChildNodeAsText(subNode, DeserializerHelper.CHILDREN_KEY);
                String wikiData = (Boolean) subNode.getValue().has(DeserializerHelper.WIKIDATA_KEY) ? subNode.getValue().get(DeserializerHelper.WIKIDATA_KEY).toString() : null;
                ingredients.add(new IngredientResponse(subNode.getKey(), names, parents, children, wikiData));
            }
        }

        IngredientsWrapper wrapper = new IngredientsWrapper();
        wrapper.setIngredients(ingredients);

        return wrapper;
    }
}
