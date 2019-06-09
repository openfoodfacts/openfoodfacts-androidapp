
package openfoodfacts.github.scrachx.openfood.network.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import openfoodfacts.github.scrachx.openfood.models.LabelResponse;
import openfoodfacts.github.scrachx.openfood.models.LabelsWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Custom deserializer for LabelsWrapper
 */
public class LabelsWrapperDeserializer extends StdDeserializer<LabelsWrapper> {
    public LabelsWrapperDeserializer() {
        super(LabelsWrapper.class);
    }

    @Override
    public LabelsWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<LabelResponse> labels = new ArrayList<>();

        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            JsonNode namesNode = subNode.getValue().get(DeserializerHelper.NAMES_KEY);

            if (namesNode != null) {
                Map<String, String> names = DeserializerHelper.extractNames(namesNode);
                if (subNode.getValue().has(DeserializerHelper.WIKIDATA_KEY)) {
                    labels.add(new LabelResponse(subNode.getKey(), names, subNode.getValue().get(DeserializerHelper.WIKIDATA_KEY).toString()));
                } else {
                    labels.add(new LabelResponse(subNode.getKey(), names));
                }
            }
        }

        LabelsWrapper wrapper = new LabelsWrapper();
        wrapper.setLabels(labels);

        return wrapper;
    }
}
