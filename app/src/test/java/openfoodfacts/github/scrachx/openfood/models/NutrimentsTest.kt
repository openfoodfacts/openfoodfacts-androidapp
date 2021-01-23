package openfoodfacts.github.scrachx.openfood.models

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Companion.SILICA
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Companion.VITAMIN_A
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Companion.VITAMIN_B1
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Nutriment
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_GRAM
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_KILOGRAM
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MILLIGRAM
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.convertFromGram
import openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber
import org.junit.Before
import org.junit.Test

class NutrimentsTest {
    private lateinit var nutriments: Nutriments

    @Before
    fun setup() {
        nutriments = Nutriments().apply { setAdditionalProperty(NUTRIMENT_NAME_KEY, NUTRIMENT_NAME) }
    }

    @Test
    fun getForAnyValue() {
        val valueFor100g = 30.0
        val valueForServing = 60.0
        val nutriment = Nutriment(
                "test",
                "test",
                valueFor100g.toString(),
                valueForServing.toString(),
                UNIT_MILLIGRAM,
                ""
        )
        assertThat(nutriment.displayStringFor100g).isEqualTo("${getRoundNumber((30 * 1000).toFloat())} mg")
        assertThat(nutriment.getForPortion(1f, UNIT_KILOGRAM)).isEqualTo(getRoundNumber(convertFromGram(valueFor100g * 10, nutriment.unit)))
        assertThat(nutriment.getForPortion(1f, UNIT_GRAM)).isEqualTo(getRoundNumber(convertFromGram(valueFor100g / 100, nutriment.unit)))
    }

    @Test
    fun getUnit_returnsUnit() {
        nutriments.setAdditionalProperty(NUTRIMENT_UNIT_KEY, NUTRIMENT_UNIT)
        assertThat(nutriments[NUTRIMENT_NAME_KEY]?.unit).isEqualTo(NUTRIMENT_UNIT)
    }

    @Test
    fun getServing_returnsServing() {
        nutriments.setAdditionalProperty(NUTRIMENT_SERVING_KEY, NUTRIMENT_SERVING)
        assertThat(nutriments[NUTRIMENT_NAME_KEY]?.forServing).isEqualTo(NUTRIMENT_SERVING)
    }

    @Test
    fun get100g_returns100g() {
        nutriments.setAdditionalProperty(NUTRIMENT_100G_KEY, NUTRIMENT_100G)
        assertThat(nutriments[NUTRIMENT_NAME_KEY]?.for100g).isEqualTo(NUTRIMENT_100G)
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
        assertThat(nutriment?.name).isEqualTo(NUTRIMENT_NAME)
        assertThat(nutriment?.unit).isEqualTo(NUTRIMENT_UNIT)
    }

    @Test
    fun setAdditionalPropertyWithMineralName_setsHasMineralsTrue() {
        nutriments.setAdditionalProperty(SILICA, SILICA)
        assertThat(nutriments.hasMinerals).isTrue()
    }

    @Test
    fun setAdditionalPropertyWithVitaminName_setsHasVitaminsTrue() {
        nutriments.setAdditionalProperty(VITAMIN_A, VITAMIN_A)
        assertThat(nutriments.hasVitamins).isTrue()
    }

    @Test
    fun containsWithAvailableElement_returnsTrue() {
        nutriments.setAdditionalProperty(VITAMIN_A, VITAMIN_A)
        assertThat(VITAMIN_A in nutriments).isTrue()
        assertThat(nutriments.hasVitamins).isTrue()
    }

    @Test
    fun containsWithNonExistentElement_returnsFalse() {
        assertThat(VITAMIN_B1 in nutriments).isFalse()
        assertThat(nutriments.hasVitamins).isFalse()
    }

    companion object {
        // TODO: in Nutriments, there is confusion between name and value when turning it into a Nutriment
        // TODO: in Nutriments, make the key endings public Strings, or at least turn them into variables
        private const val NUTRIMENT_NAME_KEY = "a nutriment"
        private const val NUTRIMENT_VALUE_KEY = "${NUTRIMENT_NAME_KEY}_value"
        private const val NUTRIMENT_100G_KEY = "${NUTRIMENT_NAME_KEY}_100g"
        private const val NUTRIMENT_SERVING_KEY = "${NUTRIMENT_NAME_KEY}_serving"
        private const val NUTRIMENT_UNIT_KEY = "${NUTRIMENT_NAME_KEY}_unit"
        private const val NUTRIMENT_NAME = "a nutriment"
        private const val NUTRIMENT_VALUE = "100.0"
        private const val NUTRIMENT_100G = "50%"
        private const val NUTRIMENT_SERVING = "70%"
        private const val NUTRIMENT_UNIT = "mg"
    }
}