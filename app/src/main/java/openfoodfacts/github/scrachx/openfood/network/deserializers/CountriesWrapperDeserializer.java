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

import openfoodfacts.github.scrachx.openfood.models.CountriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.CountryResponse;

/**
 * Created by Lobster on 03.03.18.
 */

public class CountriesWrapperDeserializer implements JsonDeserializer<CountriesWrapper> {

    private static final String NAMES_KEY = "name";

    @Override
    public CountriesWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<CountryResponse> countries = new ArrayList<>();
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

                countries.add(new CountryResponse(label.getKey(), names));
            }
        }

        CountriesWrapper countriesWrapper = new CountriesWrapper();
        countriesWrapper.setCountries(countries);

        return countriesWrapper;
    }
}
