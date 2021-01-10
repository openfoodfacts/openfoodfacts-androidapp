package openfoodfacts.github.scrachx.openfood.models

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Nutriment
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.convertFromGram
import openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber
import org.junit.Before
import org.junit.Test

class NutrimentsTest {
    private lateinit var nutriments: Nutriments

    @Before
    fun setup() {
        nutriments = Nutriments()
    }

    @Test
    fun getForAnyValue() {
        val valueInGramFor100Gram = 30f
        val valueInGramFor200Gram = 60f
        val nutriment = Nutriment(
                "test",
                "test",
                valueInGramFor100Gram.toDouble().toString(),
                valueInGramFor200Gram.toDouble().toString(),
                Units.UNIT_MILLIGRAM,
                ""
        )
        assertThat(nutriment.displayStringFor100g).isEqualTo(getRoundNumber((30 * 1000).toFloat()) + " mg")
        assertThat(nutriment.getForAnyValue(1f, Units.UNIT_KILOGRAM)).isEqualTo(getRoundNumber(convertFromGram(valueInGramFor100Gram * 10, nutriment.unit)))
        assertThat(nutriment.getForAnyValue(1f, Units.UNIT_GRAM)).isEqualTo(getRoundNumber(convertFromGram(valueInGramFor100Gram / 100, nutriment.unit)))
    }

    @Test
    fun getUnit_returnsUnit() {
        nutriments.setAdditionalProperty(NUTRIMENT_UNIT_KEY, NUTRIMENT_UNIT)
        assertThat(nutriments.getUnit(NUTRIMENT_NAME_KEY)).isEqualTo(NUTRIMENT_UNIT)
    }

    @Test
    fun getServing_returnsServing() {
        nutriments.setAdditionalProperty(NUTRIMENT_SERVING_KEY, NUTRIMENT_SERVING)
        assertThat(nutriments.getServing(NUTRIMENT_NAME_KEY)).isEqualTo(NUTRIMENT_SERVING)
    }

    @Test
    fun get100g_returns100g() {
        nutriments.setAdditionalProperty(NUTRIMENT_100G_KEY, NUTRIMENT_100G)
        assertThat(nutriments.get100g(NUTRIMENT_NAME_KEY)).isEqualTo(NUTRIMENT_100G)
    }

    @Test
    fun getNonExistentNutriment_returnsNull() {
        assertThat(nutriments["not there"]).isNull()
    }

    // See note about confusion between value and name above
    @Test
    fun getAvailableNutriment_returnsNutriment() {
        nutriments.setAdditionalProperty(NUTRIMENT_NAME_KEY, NUTRIMENT_NAME)
        nutriments.setAdditionalProperty(NUTRIMENT_100G_KEY, NUTRIMENT_100G)
        nutriments.setAdditionalProperty(NUTRIMENT_SERVING_KEY, NUTRIMENT_SERVING)
        nutriments.setAdditionalProperty(NUTRIMENT_UNIT_KEY, NUTRIMENT_UNIT)
        val nutriment = nutriments[NUTRIMENT_NAME_KEY]

        // See note about confusion between value and name above
        assertThat(nutriment!!.name).isEqualTo(NUTRIMENT_NAME)
        assertThat(nutriment.unit).isEqualTo(NUTRIMENT_UNIT)
    }

    @Test
    fun setAdditionalPropertyWithMineralName_setsHasMineralsTrue() {
        nutriments.setAdditionalProperty(Nutriments.SILICA, Nutriments.SILICA)
        assertThat(nutriments.hasMinerals).isTrue()
    }

    @Test
    fun setAdditionalPropertyWithVitaminName_setsHasVitaminsTrue() {
        nutriments.setAdditionalProperty(Nutriments.VITAMIN_A, Nutriments.VITAMIN_A)
        assertThat(nutriments.hasVitamins).isTrue()
    }

    @Test
    fun containsWithAvailableElement_returnsTrue() {
        nutriments.setAdditionalProperty(Nutriments.VITAMIN_A, Nutriments.VITAMIN_A)
        assertThat(nutriments.contains(Nutriments.VITAMIN_A)).isTrue()
    }

    @Test
    fun containsWithNonExistentElement_returnsFalse() {
        assertThat(nutriments.contains(Nutriments.VITAMIN_B1)).isFalse()
    }

    companion object {
        // TODO: in Nutriments, there is confusion between name and value when turning it into a Nutriment
        // TODO: in Nutriments, make the key endings public Strings, or at least turn them into variables
        private const val NUTRIMENT_NAME_KEY = "a nutriment"
        private const val NUTRIMENT_NAME = "a nutriment"
        private const val NUTRIMENT_VALUE_KEY = NUTRIMENT_NAME_KEY + "_value"
        private const val NUTRIMENT_VALUE = "100.0"
        private const val NUTRIMENT_100G_KEY = NUTRIMENT_NAME_KEY + "_100g"
        private const val NUTRIMENT_100G = "50%"
        private const val NUTRIMENT_SERVING_KEY = NUTRIMENT_NAME_KEY + "_serving"
        private const val NUTRIMENT_SERVING = "70%"
        private const val NUTRIMENT_UNIT_KEY = NUTRIMENT_NAME_KEY + "_unit"
        private const val NUTRIMENT_UNIT = "mg"
    }
}