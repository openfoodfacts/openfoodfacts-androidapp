
package openfoodfacts.github.scrachx.openfood.models.entities.analysistag;

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

import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper;

/**
 * Custom deserializer for {@link AnalysisTagsWrapper AnalysisTagsWrapper}
 *
 * @author Rares
 */
public class AnalysisTagsWrapperDeserializer extends StdDeserializer<AnalysisTagsWrapper> {
    public AnalysisTagsWrapperDeserializer() {
        super(AnalysisTagsWrapper.class);
    }

    @Override
    public AnalysisTagsWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<AnalysisTagResponse> analysisTags = new ArrayList<>();

        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
            final Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            JsonNode namesNode = subNode.getValue().get(DeserializerHelper.NAMES_KEY);

            if (namesNode != null) {
                Map<String, String> names = DeserializerHelper.extractMapFromJsonNode(namesNode);

                Map<String, String> showIngredients = new HashMap<>();
                JsonNode showIngredientsNode = subNode.getValue().get(DeserializerHelper.SHOW_INGREDIENTS_KEY);
                if(showIngredientsNode != null) {
                    showIngredients = DeserializerHelper.extractMapFromJsonNode(showIngredientsNode);
                }

                analysisTags.add(new AnalysisTagResponse(subNode.getKey(), names, showIngredients));
            }
        }

        AnalysisTagsWrapper wrapper = new AnalysisTagsWrapper();
        wrapper.setAnalysisTags(analysisTags);

        return wrapper;
    }
}
