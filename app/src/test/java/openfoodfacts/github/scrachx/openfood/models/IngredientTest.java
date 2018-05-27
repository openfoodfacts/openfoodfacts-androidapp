package org.openfoodfacts.scanner.models;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@link Ingredient}
 */
public class IngredientTest {

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

        Ingredient ingredient = new Ingredient();
        ingredient.setText(text);
        ingredient.setId(id);
        ingredient.setRank(rank);
        ingredient.setPercent(percent);
        ingredient.setAdditionalProperty(additionalPropertyName, additionalPropertyValue);

        String expectedString = "Ingredient{" +
                "text='" + text + '\'' +
                ", id='" + id + '\'' +
                ", rank=" + rank +
                ", percent='" + percent + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';

        assertEquals(expectedString, ingredient.toString());
    }
}
