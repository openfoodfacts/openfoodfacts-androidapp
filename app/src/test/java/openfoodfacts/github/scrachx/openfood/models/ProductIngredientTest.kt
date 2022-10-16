package openfoodfacts.github.scrachx.openfood.models

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Tests for [ProductIngredient]
 */
class ProductIngredientTest {

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