package openfoodfacts.github.scrachx.openfood.network.deserializers;

<<<<<<< HEAD
<<<<<<< HEAD
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

=======

=======

>>>>>>> upstream/master
import android.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;


<<<<<<< HEAD
>>>>>>> upstream/master
=======
>>>>>>> upstream/master
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.models.AdditiveResponse;
import openfoodfacts.github.scrachx.openfood.models.CategoriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.CategoryResponse;
import openfoodfacts.github.scrachx.openfood.models.LabelResponse;

/**
 * Created by Lobster on 03.03.18.
 */

public class CategoriesWrapperDeserializer extends StdDeserializer<CategoriesWrapper> {


    private static final String NAMES_KEY = "name";
    private static final String WIKIDATA_KEY = "wikidata";


    public CategoriesWrapperDeserializer() {
        super(CategoriesWrapper.class);
    }
<<<<<<< HEAD

    public CategoriesWrapperDeserializer() {
        super(CategoriesWrapper.class);
    }
=======
>>>>>>> upstream/master

    @Override
    public CategoriesWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<CategoryResponse> categories = new ArrayList<>();
<<<<<<< HEAD
<<<<<<< HEAD
=======

>>>>>>> upstream/master
=======

>>>>>>> upstream/master
        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            JsonNode namesNode = subNode.getValue().get(NAMES_KEY);
<<<<<<< HEAD
<<<<<<< HEAD
=======
            Boolean isWikiNodePresent = subNode.getValue().has(WIKIDATA_KEY);

>>>>>>> upstream/master
=======
            Boolean isWikiNodePresent = subNode.getValue().has(WIKIDATA_KEY);

>>>>>>> upstream/master
            if (namesNode != null) {
                Map<String, String> names = new HashMap<>();  /* Entry<Language Code, Product Name> */
                Iterator<Map.Entry<String, JsonNode>> nameNodeIterator = namesNode.fields();
                while (nameNodeIterator.hasNext()) {
                    Map.Entry<String, JsonNode> nameNode = nameNodeIterator.next();
                    String name = nameNode.getValue().asText();
                    names.put(nameNode.getKey(), name);

<<<<<<< HEAD
<<<<<<< HEAD
                }

                categories.add(new CategoryResponse(subNode.getKey(), names));
=======
=======
>>>>>>> upstream/master
                }

                if (isWikiNodePresent) {
                    categories.add(new CategoryResponse(subNode.getKey(), names, subNode.getValue().get(WIKIDATA_KEY).toString()));
                } else {
                    categories.add(new CategoryResponse(subNode.getKey(), names));
                }

<<<<<<< HEAD
>>>>>>> upstream/master
=======
>>>>>>> upstream/master
            }
        }

        CategoriesWrapper wrapper = new CategoriesWrapper();
        wrapper.setCategories(categories);

        return wrapper;
    }
}