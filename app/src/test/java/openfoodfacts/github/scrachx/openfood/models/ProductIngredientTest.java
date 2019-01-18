package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@link ProductIngredient}
 */
public class ProductIngredientTest {

    @Test
    public void toString_returnsCorrectFormat() {
        String text = "Mayonnaise";
        String id = "mayo_id";
        long rank = 400L;
        String percent = "20%";
        String additionalPropertyName = "Saltiness";
        String additionalPropertyValue = "100";
        Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put(additionalPropertyName, additionalPropertyValue);

        ProductIngredient productIngredient = new ProductIngredient();
        productIngredient.setText(text);
        productIngredient.setId(id);
        productIngredient.setRank(rank);
        productIngredient.setPercent(percent);
        productIngredient.setAdditionalProperty(additionalPropertyName, additionalPropertyValue);

        String expectedString = "Ingredient{" +
                "text='" + text + '\'' +
                ", id='" + id + '\'' +
                ", rank=" + rank +
                ", percent='" + percent + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';

        assertEquals(expectedString, productIngredient.toString());
    }
}
