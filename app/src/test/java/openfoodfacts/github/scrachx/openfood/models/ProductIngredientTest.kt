package openfoodfacts.github.scrachx.openfood.models

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Tests for [ProductIngredient]
 */
class ProductIngredientTest {
    @Test
    fun `toString returns correct format`() {
        val text = "Mayonnaise"
        val id = "mayo_id"
        val rank = 400L
        val percent = "20%"
        val additionalPropertyName = "Saltiness"
        val additionalPropertyValue = "100"
        val additionalProperties = hashMapOf<String, Any>(additionalPropertyName to additionalPropertyValue)

        val productIngredient = ProductIngredient(text, id, rank, percent)
            .apply { setAdditionalProperty(additionalPropertyName, additionalPropertyValue) }

        val expectedString = "ProductIngredient[text=$text," +
                "id=$id," +
                "rank=$rank," +
                "percent=$percent," +
                "additionalProperties=$additionalProperties]"
        assertThat(productIngredient.toString()).isEqualTo(expectedString)
    }

    @Test
    fun `Fills up additional property`() {
        val productIngredient = ProductIngredient("Ketchup", "ketchup_id", 300L, "20%")
            .apply { setAdditionalProperty("Sweetness", "90") }
        assertThat(productIngredient.additionalProperties["Sweetness"]).isEqualTo("90")
    }

    @Test
    fun `Fills up fields`() {
        val productIngredient = ProductIngredient("Mustard", "mustard_id", 200L, "25%")
        assertThat(productIngredient.text).isEqualTo("Mustard")
        assertThat(productIngredient.id).isEqualTo("mustard_id")
        assertThat(productIngredient.rank).isEqualTo(200L)
        assertThat(productIngredient.percent).isEqualTo("25%")
    }
}