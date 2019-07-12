package openfoodfacts.github.scrachx.openfood.network.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import openfoodfacts.github.scrachx.openfood.models.CountriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.CountryResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Lobster on 03.03.18.
 */

public class CountriesWrapperDeserializer extends StdDeserializer<CountriesWrapper> {




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
            JsonNode namesNode = subNode.getValue().get(DeserializerHelper.NAMES_KEY);
            if (namesNode != null) {
                Map<String, String> names = DeserializerHelper.extractNames(namesNode);
                countries.add(new CountryResponse(subNode.getKey(), names));
            }
        }

        CountriesWrapper wrapper = new CountriesWrapper();
        wrapper.setCountries(countries);

        return wrapper;
    }
}
