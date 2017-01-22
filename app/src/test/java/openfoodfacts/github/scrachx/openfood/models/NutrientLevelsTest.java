package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author herau
 */
public class NutrientLevelsTest {

    @Test
    public void jsonSerialization_ok() throws Exception {
        NutrientLevels nutrientLevels = new NutrientLevels();

        nutrientLevels.setFat(NutrimentLevel.LOW);
        nutrientLevels.setSalt(NutrimentLevel.MODERATE);
        nutrientLevels.setSaturatedFat(NutrimentLevel.HIGH);
        nutrientLevels.setSugars(NutrimentLevel.MODERATE);

        JsonNode jsonNode = new ObjectMapper().valueToTree(nutrientLevels);

        assertEquals(jsonNode.get("fat").asText(), NutrimentLevel.LOW.toString());
        assertEquals(jsonNode.get("salt").asText(), NutrimentLevel.MODERATE.toString());
        assertEquals(jsonNode.get("saturated-fat").asText(), NutrimentLevel.HIGH.toString());
        assertEquals(jsonNode.get("sugars").asText(), NutrimentLevel.MODERATE.toString());
    }

    @Test
    public void jsonDeserialization_ok() throws Exception {
        NutrientLevels nutrientLevels = new NutrientLevels();

        nutrientLevels.setFat(NutrimentLevel.LOW);
        nutrientLevels.setSalt(NutrimentLevel.MODERATE);
        nutrientLevels.setSaturatedFat(NutrimentLevel.HIGH);
        nutrientLevels.setSugars(NutrimentLevel.MODERATE);

        ObjectMapper objectMapper = new ObjectMapper();
        NutrientLevels nutrientLevelsResult = objectMapper.treeToValue(
                objectMapper.createObjectNode()
                .put("fat", "low")
                .put("salt", "moderate")
                .put("saturated-fat", "high")
                .put("sugars", "moderate"), NutrientLevels.class);

        assertEquals(nutrientLevels.getFat(), nutrientLevelsResult.getFat());
        assertEquals(nutrientLevels.getSugars(), nutrientLevelsResult.getSugars());
        assertEquals(nutrientLevels.getSaturatedFat(), nutrientLevelsResult.getSaturatedFat());
        assertEquals(nutrientLevels.getSalt(), nutrientLevelsResult.getSalt());
    }
}