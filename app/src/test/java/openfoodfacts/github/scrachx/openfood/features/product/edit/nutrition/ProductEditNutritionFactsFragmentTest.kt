package openfoodfacts.github.scrachx.openfood.features.product.edit.nutrition

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.Nutriment
import org.junit.jupiter.api.Test

class ProductEditNutritionFactsFragmentTest {

    val nutrientsTestValues = mapOf(Nutriment.VITAMIN_D to "µg", Nutriment.SELENIUM to "µg", Nutriment.BICARBONATE to "mg", Nutriment.CALCIUM to "mg")

    @Test
    fun nutritionDefaultValuesTest() {
        nutrientsTestValues.keys.forEach {
            assertThat(PARAMS_OTHER_NUTRIENTS_DEFAULT_UNITS[PREFIX_NUTRIMENT_LONG_NAME + it.key]).matches(nutrientsTestValues[it])
        }
    }
}