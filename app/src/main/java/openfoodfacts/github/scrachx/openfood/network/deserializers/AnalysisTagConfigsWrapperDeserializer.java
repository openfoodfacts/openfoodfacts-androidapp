
package openfoodfacts.github.scrachx.openfood.network.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.models.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagGonfigsWrapper;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagsWrapper;

/**
 * Custom deserializer for {@link AnalysisTagGonfigsWrapper AnalysisTagsWrapper}
 *
 * @author Rares
 */
public class AnalysisTagConfigsWrapperDeserializer extends StdDeserializer<AnalysisTagGonfigsWrapper> {
    public AnalysisTagConfigsWrapperDeserializer() {
        super(AnalysisTagsWrapper.class);
    }

    @Override
    public AnalysisTagGonfigsWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<AnalysisTagConfig> analysisTagConfigs = new ArrayList<>();

        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
            final Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            String type = subNode.getValue().get(DeserializerHelper.TYPE_KEY).asText();
            String icon = subNode.getValue().get(DeserializerHelper.ICON_KEY).asText();
            String color = subNode.getValue().get(DeserializerHelper.COLOR_KEY).asText();
            analysisTagConfigs.add(new AnalysisTagConfig(subNode.getKey(), type, icon, color));
        }

        AnalysisTagGonfigsWrapper wrapper = new AnalysisTagGonfigsWrapper();
        wrapper.setAnalysisTagConfigs(analysisTagConfigs);

        return wrapper;
    }
}
