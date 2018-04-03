package openfoodfacts.github.scrachx.openfood.network.deserializers;

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

import openfoodfacts.github.scrachx.openfood.models.CountriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.CountryResponse;

/**
 * Created by Lobster on 03.03.18.
 */

public class CountriesWrapperDeserializer extends StdDeserializer<CountriesWrapper> {


    private static final String NAMES_KEY = "name";

    public CountriesWrapperDeserializer() {
        super(CountriesWrapper.class);
    }

    @Override
    public CountriesWrapper deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        List<CountryResponse> countries = new ArrayList<>();

        JsonNode mainNode = jp.getCodec().readTree(jp);
        Iterator<Map.Entry<String, JsonNode>> mainNodeIterator = mainNode.fields();

        while (mainNodeIterator.hasNext()) {
            Map.Entry<String, JsonNode> subNode = mainNodeIterator.next();
            JsonNode namesNode = subNode.getValue().get(NAMES_KEY);
            if (namesNode != null) {
                Map<String, String> names = new HashMap<>();  /* Entry<Language Code, Product Name> */
                Iterator<Map.Entry<String, JsonNode>> nameNodeIterator = namesNode.fields();
                while (nameNodeIterator.hasNext()) {
                    Map.Entry<String, JsonNode> nameNode = nameNodeIterator.next();
                    String name = nameNode.getValue().asText();
                    names.put(nameNode.getKey(), name);

                }

                countries.add(new CountryResponse(subNode.getKey(), names));
            }
        }

        CountriesWrapper wrapper = new CountriesWrapper();
        wrapper.setCountries(countries);

        return wrapper;
    }
}