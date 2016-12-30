package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.utils.JsonUtils;

import static org.junit.Assert.assertEquals;

public class AllergenTest {

    @Test
    public void deserialize_json_rest_response() throws IOException {
        String name = "Milk";
        String url = "https://world.openfoodfacts.org/allergen/milk";
        int products = 11376;
        String id = "en:milk";
        AllergenRestResponse restResponse = JsonUtils.readFor(AllergenRestResponse.class)
                .readValue("{\"tags\":[" +
                        "{\"url\":\"" + url + "\"," +
                        "\"products\":" + products + ",\"name\":\"" + name + "\",\"id\":\"" + id + "\"}," +
                        "{\"url\":\"https://world.openfoodfacts.org/allergen/gluten\"," +
                        "\"id\":\"en:gluten\",\"products\":9812,\"name\":\"Gluten\"}" +
                        "]}");

        List<Allergen> allergens = restResponse.getAllergens();
        assertEquals(allergens.size(), 2);
        assertEquals(allergens.get(0).getName(), name);
        assertEquals(allergens.get(0).getProducts(), Integer.valueOf(products));
        assertEquals(allergens.get(0).getUrl(), url);
        assertEquals(allergens.get(0).getIdAllergen(), id);

    }

}
