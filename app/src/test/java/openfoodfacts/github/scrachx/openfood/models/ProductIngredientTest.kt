package openfoodfacts.github.scrachx.openfood.models

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

/**
 * Tests for [ProductIngredient]
 */
class ProductIngredientTest {
    @Test
    fun toString_returnsCorrectFormat() {
        val text = "Mayonnaise"
        val id = "mayo_id"
        val rank = 400L
        val percent = "20%"
        val additionalPropertyName = "Saltiness"
        val additionalPropertyValue = "100"
        val additionalProperties: MutableMap<String, Any> = HashMap()
        additionalProperties[additionalPropertyName] = additionalPropertyValue

        val productIngredient = ProductIngredient().apply {
            this.text = text
            this.id = id
            this.rank = rank
            this.percent = percent
            this.setAdditionalProperty(additionalPropertyName, additionalPropertyValue)
        }

        val expectedString = """Ingredient{
            |text='$text', 
            |id='$id', 
            |rank=$rank, 
            |percent='$percent', 
            |additionalProperties=$additionalProperties
            |}""".trimMargin()
        assertThat(productIngredient.toString()).isEqualTo(expectedString)
    }

    @Test
    fun productIngredientWithAdditionalProperty() {
        val productIngredient = ProductIngredient()
        productIngredient.text = "Ketchup"
        productIngredient.id = "ketchup_id"
        productIngredient.rank = 300L
        productIngredient.percent = "20%"
        productIngredient.withAdditionalProperty("Sweetness", "90")
        val returnedMap = productIngredient.getAdditionalProperties()
        assertThat(returnedMap["Sweetness"]).isEqualTo("90")
    }

    @Test
    fun productIngredientGetters() {
        val productIngredient = ProductIngredient()
        productIngredient.text = "Mustard"
        productIngredient.id = "mustard_id"
        productIngredient.rank = 200L
        productIngredient.percent = "25%"
        assertThat(productIngredient.text).isEqualTo("Mustard")
        assertThat(productIngredient.id).isEqualTo("mustard_id")
        assertThat(productIngredient.rank).isEqualTo(200L)
        assertThat(productIngredient.percent).isEqualTo("25%")
    }
}