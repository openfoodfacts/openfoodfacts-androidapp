package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

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

        assertThat(productIngredient.toString()).isEqualTo(expectedString);
    }

    @Test
    public void ProductIngredientWithAdditionalProperty() {
        ProductIngredient productIngredient = new ProductIngredient();
        productIngredient.setText("Ketchup");
        productIngredient.setId("ketchup_id");
        productIngredient.setRank(300L);
        productIngredient.setPercent("20%");

        productIngredient.withAdditionalProperty("Sweetness", "90");
        Map<String, Object> returnedMap = productIngredient.getAdditionalProperties();

        assertThat(((String) returnedMap.get("Sweetness"))).isEqualTo("90");
    }

    @Test
    public void ProductIngredientGetters() {
        ProductIngredient productIngredient = new ProductIngredient();
        productIngredient.setText("Mustard");
        productIngredient.setId("mustard_id");
        productIngredient.setRank(200L);
        productIngredient.setPercent("25%");

        assertThat(productIngredient.getText()).isEqualTo("Mustard");
        assertThat(productIngredient.getId()).isEqualTo("mustard_id");
        assertThat(productIngredient.getRank()).isEqualTo(200L);
        assertThat(productIngredient.getPercent()).isEqualTo("25%");
    }
}
