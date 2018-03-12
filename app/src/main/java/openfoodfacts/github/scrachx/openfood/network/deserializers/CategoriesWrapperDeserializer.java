package openfoodfacts.github.scrachx.openfood.network.deserializers;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.models.CategoriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.CategoryResponse;
import openfoodfacts.github.scrachx.openfood.models.LabelResponse;

/**
 * Created by Lobster on 03.03.18.
 */

public class CategoriesWrapperDeserializer implements JsonDeserializer<CategoriesWrapper> {

    private static final String NAMES_KEY = "name";
    private static final String WIKIDATA_KEY = "wikidata";


    @Override
    public CategoriesWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<CategoryResponse> categories = new ArrayList<>();
        JsonObject categoriesWrapperJson = json.getAsJsonObject();

        for (Map.Entry<String, JsonElement> category : categoriesWrapperJson.entrySet()) {
            JsonObject jsonObject = category.getValue().getAsJsonObject();
            JsonElement namesJsonElement = jsonObject.get(NAMES_KEY);
            Boolean wikiDataJsonElement = jsonObject.has(WIKIDATA_KEY);

            if (namesJsonElement != null) {
                JsonObject namesJson = namesJsonElement.getAsJsonObject();

                Map<String, String> names = new HashMap<String, String>();  /* Entry<Language Code, Product Name> */
                for (Map.Entry<String, JsonElement> name : namesJson.entrySet()) {
                    String strName = name.getValue().toString();
                    names.put(name.getKey(), strName.substring(1, strName.length() - 1)); /* Substring removes needless quotes */
                }
                if (wikiDataJsonElement) {
                    categories.add(new CategoryResponse(category.getKey(), names, jsonObject.get(WIKIDATA_KEY).toString()));
                } else {
                    categories.add(new CategoryResponse(category.getKey(), names));
                }
            }
        }

        CategoriesWrapper categoriesWrapper = new CategoriesWrapper();
        categoriesWrapper.setCategories(categories);

        return categoriesWrapper;
    }
}
