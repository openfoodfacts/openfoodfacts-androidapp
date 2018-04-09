
package openfoodfacts.github.scrachx.openfood.network.deserializers;


import android.util.Log;
import org.json.JSONObject;

import java.lang.reflect.Type;

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

import openfoodfacts.github.scrachx.openfood.models.LabelResponse;
import openfoodfacts.github.scrachx.openfood.models.LabelsWrapper;

/**
 * Created by Lobster on 03.03.18.
 */

public class LabelsWrapperDeserializer extends StdDeserializer<LabelsWrapper> {

    private static final String NAMES_KEY = "name";
    private static final String WIKIDATA_KEY = "wikidata";

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
            JsonNode namesNode = subNode.getValue().get(NAMES_KEY);
            Boolean isWikiNodePresent = subNode.getValue().has(WIKIDATA_KEY);

            if (namesNode != null) {
                Map<String, String> names = new HashMap<>();  /* Entry<Language Code, Product Name> */
                Iterator<Map.Entry<String, JsonNode>> nameNodeIterator = namesNode.fields();
                while (nameNodeIterator.hasNext()) {
                    Map.Entry<String, JsonNode> nameNode = nameNodeIterator.next();
                    String name = nameNode.getValue().asText();
                    names.put(nameNode.getKey(), name);

                }

                if (isWikiNodePresent) {
                    labels.add(new LabelResponse(subNode.getKey(), names, subNode.getValue().get(WIKIDATA_KEY).toString()));
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