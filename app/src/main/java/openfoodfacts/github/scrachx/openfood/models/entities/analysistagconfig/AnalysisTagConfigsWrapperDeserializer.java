
package openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.models.entities.analysistag.AnalysisTagsWrapper;
import openfoodfacts.github.scrachx.openfood.utils.DeserializerHelper;

/**
 * Custom deserializer for {@link AnalysisTagConfigsWrapper AnalysisTagsWrapper}
 *
 * @author Rares
 */
public class AnalysisTagConfigsWrapperDeserializer extends StdDeserializer<AnalysisTagConfigsWrapper> {
    public AnalysisTagConfigsWrapperDeserializer() {
        super(AnalysisTagsWrapper.class);
    }

    @Override
    public AnalysisTagConfigsWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
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

        AnalysisTagConfigsWrapper wrapper = new AnalysisTagConfigsWrapper();
        wrapper.setAnalysisTagConfigs(analysisTagConfigs);

        return wrapper;
    }
}
