package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.truth.Truth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @author herau
 */
class NutrientLevelsTest {
    private lateinit var nutrientLevels: NutrientLevels

    @BeforeEach
    fun setUp() {
        nutrientLevels = NutrientLevels()
        nutrientLevels.fat = NutrimentLevel.LOW
        nutrientLevels.salt = NutrimentLevel.MODERATE
        nutrientLevels.saturatedFat = NutrimentLevel.HIGH
        nutrientLevels.sugars = NutrimentLevel.MODERATE
    }

    @Test
    fun jsonSerialization_ok() {
        val jsonNode = jacksonObjectMapper().valueToTree<JsonNode>(nutrientLevels)
        Truth.assertThat(jsonNode["fat"].asText()).isEqualTo(NutrimentLevel.LOW.toString())
        Truth.assertThat(jsonNode["salt"].asText()).isEqualTo(NutrimentLevel.MODERATE.toString())
        Truth.assertThat(jsonNode["saturated-fat"].asText()).isEqualTo(NutrimentLevel.HIGH.toString())
        Truth.assertThat(jsonNode["sugars"].asText()).isEqualTo(NutrimentLevel.MODERATE.toString())
    }

    @Test
    fun jsonDeserialization_ok() {
        val objectMapper = jacksonObjectMapper()
        val nutrientLevelsResult = objectMapper.treeToValue(
                objectMapper.createObjectNode()
                        .put("fat", "low")
                        .put("salt", "moderate")
                        .put("saturated-fat", "high")
                        .put("sugars", "moderate"), NutrientLevels::class.java)
        Truth.assertThat(nutrientLevelsResult.fat).isEqualTo(nutrientLevels.fat)
        Truth.assertThat(nutrientLevelsResult.sugars).isEqualTo(nutrientLevels.sugars)
        Truth.assertThat(nutrientLevelsResult.saturatedFat).isEqualTo(nutrientLevels.saturatedFat)
        Truth.assertThat(nutrientLevelsResult.salt).isEqualTo(nutrientLevels.salt)
    }
}