package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author herau
 */
public class NutrientLevelsTest {
    private NutrientLevels nutrientLevels;

    @Before
    public void setUp() {
        nutrientLevels = new NutrientLevels();
        nutrientLevels.setFat(NutrimentLevel.LOW);
        nutrientLevels.setSalt(NutrimentLevel.MODERATE);
        nutrientLevels.setSaturatedFat(NutrimentLevel.HIGH);
        nutrientLevels.setSugars(NutrimentLevel.MODERATE);
    }

    @Test
    public void jsonSerialization_ok() {
        JsonNode jsonNode = new ObjectMapper().valueToTree(nutrientLevels);

        assertThat(jsonNode.get("fat").asText()).isEqualTo(NutrimentLevel.LOW.toString());
        assertThat(jsonNode.get("salt").asText()).isEqualTo(NutrimentLevel.MODERATE.toString());
        assertThat(jsonNode.get("saturated-fat").asText()).isEqualTo(NutrimentLevel.HIGH.toString());
        assertThat(jsonNode.get("sugars").asText()).isEqualTo(NutrimentLevel.MODERATE.toString());
    }

    @Test
    public void jsonDeserialization_ok() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        NutrientLevels nutrientLevelsResult = objectMapper.treeToValue(
            objectMapper.createObjectNode()
                .put("fat", "low")
                .put("salt", "moderate")
                .put("saturated-fat", "high")
                .put("sugars", "moderate"), NutrientLevels.class);

        assertThat(nutrientLevelsResult.getFat()).isEqualTo(nutrientLevels.getFat());
        assertThat(nutrientLevelsResult.getSugars()).isEqualTo(nutrientLevels.getSugars());
        assertThat(nutrientLevelsResult.getSaturatedFat()).isEqualTo(nutrientLevels.getSaturatedFat());
        assertThat(nutrientLevelsResult.getSalt()).isEqualTo(nutrientLevels.getSalt());
    }
}