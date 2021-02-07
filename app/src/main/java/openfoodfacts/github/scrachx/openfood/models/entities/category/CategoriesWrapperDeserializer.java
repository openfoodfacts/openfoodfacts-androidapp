package openfoodfacts.github.scrachx.openfood.models.entities.category;

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

/*
 * Created by Lobster on 03.03.18.
 */

/**
 * custom deserializer for CategoriesWrapper
 */
public class CategoriesWrapperDeserializer extends StdDeserializer<CategoriesWrapper> {
    public CategoriesWrapperDeserializer() {
        super(CategoriesWrapper.class);
    }

    @Override
    public CategoriesWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<CategoryResponse> categories = new ArrayList<>();

        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            JsonNode namesNode = subNode.getValue().get(DeserializerHelper.NAMES_KEY);
            if (namesNode != null) {
                Map<String, String> names = DeserializerHelper.extractMapFromJsonNode(namesNode);
                if (subNode.getValue().has(DeserializerHelper.WIKIDATA_KEY)) {
                    categories.add(new CategoryResponse(subNode.getKey(), names, subNode.getValue().get(DeserializerHelper.WIKIDATA_KEY).toString()));
                } else {
                    categories.add(new CategoryResponse(subNode.getKey(), names));
                }
            }
        }

        CategoriesWrapper wrapper = new CategoriesWrapper();
        wrapper.setCategories(categories);
        return wrapper;
    }
}
