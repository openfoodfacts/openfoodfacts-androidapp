package openfoodfacts.github.scrachx.openfood.utils

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.Units.ENERGY_KCAL
import openfoodfacts.github.scrachx.openfood.models.Units.ENERGY_KJ
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_CENTILITRE
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_DECILITRE
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_GRAM
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_KILOGRAM
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_LITER
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MICROGRAM
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MILLIGRAM
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MILLILITRE
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.convertFromGram
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.convertToGrams
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.convertToKiloCalories
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.getServingInL
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.getServingInOz
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.saltToSodium
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.sodiumToSalt
import org.junit.Assert
import org.junit.Test
import org.junit.function.ThrowingRunnable
import java.util.*

/**
 *
 */
class UnitUtilsTest {
    @Test
    fun testGetServingInL() {
        assertThat(getServingInL("1l", Locale.getDefault())).isEqualTo("1 l")
        assertThat(getServingInL("33.814oz", Locale.getDefault())).isEqualTo("1 l")
        assertThat(getServingInL("33.814 oz", Locale.getDefault())).isEqualTo("1 l")
    }

    @Test
    fun testGetServingInOz() {
        assertThat(getServingInOz("1oz", Locale.getDefault())).isEqualTo("1 oz")

        assertThat(getServingInOz("0.0295735l", Locale.ENGLISH)).isEqualTo("1 oz")
        assertThat(getServingInOz("0.0295735 l", Locale.ENGLISH)).isEqualTo("1 oz")

        assertThat(getServingInOz("2.95735cl", Locale.ENGLISH)).isEqualTo("1 oz")
        assertThat(getServingInOz("2.95735 cl", Locale.ENGLISH)).isEqualTo("1 oz")

        assertThat(getServingInOz("29.5735ml", Locale.ENGLISH)).isEqualTo("1 oz")
        assertThat(getServingInOz("29.5735 ml", Locale.ENGLISH)).isEqualTo("1 oz")

    }

    // Not finished yet
    @Test
    fun testConvertFromGram() {
        assertThat(convertFromGram(1f, UNIT_KILOGRAM)).isWithin(DELTA).of(0.001f)
        assertThat(convertFromGram(1f, UNIT_GRAM)).isWithin(DELTA).of(1f)
        assertThat(convertFromGram(1f, UNIT_MILLIGRAM)).isWithin(DELTA).of(1000f)
        assertThat(convertFromGram(1f, UNIT_MICROGRAM)).isWithin(DELTA).of(1000 * 1000f)
    }

    @Test
    fun testConvertFromMl() {
        assertThat(convertFromGram(1f, UNIT_LITER)).isWithin(DELTA).of(0.001f)
        assertThat(convertFromGram(1f, UNIT_DECILITRE)).isWithin(DELTA).of(0.01f)
        assertThat(convertFromGram(1f, UNIT_CENTILITRE)).isWithin(DELTA).of(0.1f)
        assertThat(convertFromGram(1f, UNIT_MILLILITRE)).isWithin(DELTA).of(1f)
    }

    @Test
    fun testConvertToMl() {
        assertThat(convertToGrams(1f, UNIT_LITER)).isWithin(DELTA).of(1000f)
        assertThat(convertToGrams(1f, UNIT_DECILITRE)).isWithin(DELTA).of(100f)
        assertThat(convertToGrams(1f, UNIT_CENTILITRE)).isWithin(DELTA).of(10f)
        assertThat(convertToGrams(1f, UNIT_MILLILITRE)).isWithin(DELTA).of(1f)
    }

    @Test
    fun testConvertToGram() {
        assertThat(convertToGrams(1f, UNIT_KILOGRAM)).isWithin(DELTA).of(1000f)
        assertThat(convertToGrams(1f, UNIT_GRAM)).isWithin(DELTA).of(1f)
        assertThat(convertToGrams(1f, UNIT_MILLIGRAM)).isWithin(DELTA).of(1e-3f)
        assertThat(convertToGrams(1f, UNIT_MICROGRAM)).isWithin(DELTA).of(1e-6f)
    }

    @Test
    fun testConvertToKiloCalories() {
        assertThat(convertToKiloCalories(100, ENERGY_KJ)).isEqualTo(23)
        assertThat(convertToKiloCalories(100, ENERGY_KCAL)).isEqualTo(100)
        Assert.assertThrows<IllegalArgumentException>(IllegalArgumentException::class.java, ThrowingRunnable { convertToKiloCalories(1, UNIT_GRAM) })
    }

    @Test
    fun testSaltSodiumConversion() {
        assertThat(sodiumToSalt(1.0)).isWithin(DELTA.toDouble()).of(2.54)
        assertThat(saltToSodium(2.54)).isWithin(DELTA.toDouble()).of(1.0)
    }

    companion object {
        private const val DELTA = 1e-5f
    }
}