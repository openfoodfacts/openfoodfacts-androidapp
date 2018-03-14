package openfoodfacts.github.scrachx.openfood.network.deserializers;

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

import openfoodfacts.github.scrachx.openfood.models.AllergenResponse;
import openfoodfacts.github.scrachx.openfood.models.AllergensWrapper;

/**
 * Custom deserializer for {@link openfoodfacts.github.scrachx.openfood.models.AllergensWrapper AllergensWrapper}
 *
 * @author Lobster 2018-03-04
 * @author ross-holloway94 2018-03-14
 */

public class AllergensWrapperDeserializer implements JsonDeserializer<AllergensWrapper> {

    private static final String NAMES_KEY = "name";

    /**
     * <p>
     * Deserialize an AllergensWrapper JSON. </p>
     * <p>{@inheritDoc}</p>
     */
    @Override
    public AllergensWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<AllergenResponse> allergens = new ArrayList<>();
        JsonObject labelsWrapperJson = json.getAsJsonObject();

        for (Map.Entry<String, JsonElement> label : labelsWrapperJson.entrySet()) {
            JsonElement namesJsonElement = label.getValue().getAsJsonObject().get(NAMES_KEY);
            if (namesJsonElement != null) {
                JsonObject namesJson = namesJsonElement.getAsJsonObject();
                Map<String, String> names = new HashMap<String, String>();  /* Entry<Language Code, Product Name> */
                for (Map.Entry<String, JsonElement> name : namesJson.entrySet()) {
                    String strName = name.getValue().toString();
                    names.put(name.getKey(), strName.substring(1, strName.length() - 1)); /* Substring removes needless quotes */
                }

                allergens.add(new AllergenResponse(label.getKey(), names));
            }
        }

        AllergensWrapper labelsWrapper = new AllergensWrapper();
        labelsWrapper.setAllergens(allergens);

        return labelsWrapper;
    }
}
