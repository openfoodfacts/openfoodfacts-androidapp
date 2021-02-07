package openfoodfacts.github.scrachx.openfood.models.entities.country;

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

/**
 * custom deserializer for CountriesWrapper
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
            Map.Entry<String, JsonNode> countryNode = mainNodeIterator.next();
            JsonNode namesNode = countryNode.getValue().get(DeserializerHelper.NAMES_KEY);
            JsonNode cc2Node = countryNode.getValue().get(DeserializerHelper.COUNTRY_CODE_2_KEY);
            JsonNode cc3Node = countryNode.getValue().get(DeserializerHelper.COUNTRY_CODE_3_KEY);

            Map<String, String> names = null;
            Map<String, String> cc2 = null;
            Map<String, String> cc3 = null;
            if (namesNode != null) {
                names = DeserializerHelper.extractMapFromJsonNode(namesNode);
            }
            if (cc2Node != null) {
                cc2 = DeserializerHelper.extractMapFromJsonNode(cc2Node);
            }
            if (cc3Node != null) {
                cc3 = DeserializerHelper.extractMapFromJsonNode(cc3Node);
            }
            if (names != null && cc2 != null && cc3 != null) {
                countries.add(new CountryResponse(countryNode.getKey(), names, cc2, cc3));
            }
        }

        CountriesWrapper wrapper = new CountriesWrapper();
        wrapper.setResponses(countries);

        return wrapper;
    }
}
