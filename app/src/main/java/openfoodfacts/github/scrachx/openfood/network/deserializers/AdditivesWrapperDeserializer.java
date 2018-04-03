package openfoodfacts.github.scrachx.openfood.network.deserializers;


import android.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.models.AdditiveResponse;
import openfoodfacts.github.scrachx.openfood.models.AdditivesWrapper;
import openfoodfacts.github.scrachx.openfood.models.CategoryResponse;

/**
 * Created by Lobster on 03.03.18.
 */

public class AdditivesWrapperDeserializer extends StdDeserializer<AdditivesWrapper> {


    private static final String NAMES_KEY = "name";
    private static final String WIKIDATA_KEY = "wikidata";


    public AdditivesWrapperDeserializer() {
        super(AdditivesWrapper.class);
    }

    @Override
    public AdditivesWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<AdditiveResponse> additives = new ArrayList<>();

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
                    additives.add(new AdditiveResponse(subNode.getKey(), names, subNode.getValue().get(WIKIDATA_KEY).toString()));
                } else {
                    additives.add(new AdditiveResponse(subNode.getKey(), names));
                }
            }
        }


        AdditivesWrapper wrapper = new AdditivesWrapper();
        wrapper.setAdditives(additives);

        return wrapper;
    }
}