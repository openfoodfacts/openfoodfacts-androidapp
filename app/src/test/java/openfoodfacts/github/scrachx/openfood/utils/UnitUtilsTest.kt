package openfoodfacts.github.scrachx.openfood.utils

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit.*
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.getServingInL
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.getServingInOz
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 *
 */
class UnitUtilsTest {
    @Test
    fun testGetServingInL() {
        assertThat(getServingInL("1l")).isEqualTo("1 l")
        assertThat(getServingInL("33.814oz")).isEqualTo("1 l")
        assertThat(getServingInL("33.814 oz")).isEqualTo("1 l")
    }

    @Test
    fun testGetServingInOz() {
        assertThat(getServingInOz("1oz")).isEqualTo("1 oz")

        assertThat(getServingInOz("0.0295735l")).isEqualTo("1 oz")
        assertThat(getServingInOz("0.0295735 l")).isEqualTo("1 oz")

        assertThat(getServingInOz("2.95735cl")).isEqualTo("1 oz")
        assertThat(getServingInOz("2.95735 cl")).isEqualTo("1 oz")

        assertThat(getServingInOz("29.5735ml")).isEqualTo("1 oz")
        assertThat(getServingInOz("29.5735 ml")).isEqualTo("1 oz")

    }

    // Not finished yet
    @Test
    fun testConvertFromGram() {
        assertThat(measure(1f, UNIT_GRAM).convertTo(UNIT_KILOGRAM).value).isWithin(DELTA).of(0.001f)
        assertThat(measure(1f, UNIT_GRAM).convertTo(UNIT_GRAM).value).isWithin(DELTA).of(1f)
        assertThat(measure(1f, UNIT_GRAM).convertTo(UNIT_MILLIGRAM).value).isWithin(DELTA).of(1000f)
        assertThat(measure(1f, UNIT_GRAM).convertTo(UNIT_MICROGRAM).value).isWithin(DELTA).of(1000 * 1000f)
    }

    @Test
    fun testConvertFromMl() {
        assertThat(measure(1f, UNIT_MILLILITRE).convertTo(UNIT_LITER).value).isWithin(DELTA).of(0.001f)
        assertThat(measure(1f, UNIT_MILLILITRE).convertTo(UNIT_DECILITRE).value).isWithin(DELTA).of(0.01f)
        assertThat(measure(1f, UNIT_MILLILITRE).convertTo(UNIT_CENTILITRE).value).isWithin(DELTA).of(0.1f)
        assertThat(measure(1f, UNIT_MILLILITRE).convertTo(UNIT_MILLILITRE).value).isWithin(DELTA).of(1f)
    }

    @Test
    fun testConvertToMl() {
        assertThat(measure(1f, UNIT_LITER).convertToGrams().value).isWithin(DELTA).of(1000f)
        assertThat(measure(1f, UNIT_DECILITRE).convertToGrams().value).isWithin(DELTA).of(100f)
        assertThat(measure(1f, UNIT_CENTILITRE).convertToGrams().value).isWithin(DELTA).of(10f)
        assertThat(measure(1f, UNIT_MILLILITRE).convertToGrams().value).isWithin(DELTA).of(1f)
    }

    @Test
    fun testConvertToGram() {
        assertThat(measure(1f, UNIT_KILOGRAM).convertToGrams().value).isWithin(DELTA).of(1000f)
        assertThat(measure(1f, UNIT_GRAM).convertToGrams().value).isWithin(DELTA).of(1f)
        assertThat(measure(1f, UNIT_MILLIGRAM).convertToGrams().value).isWithin(DELTA).of(1e-3f)
        assertThat(measure(1f, UNIT_MICROGRAM).convertToGrams().value).isWithin(DELTA).of(1e-6f)
    }

    @Test
    fun testConvertToKiloCalories() {
        assertThat(measure(100f, ENERGY_KJ).convertEnergyTo(ENERGY_KCAL).value).isWithin(DELTA).of(23f)
        assertThat(measure(100f, ENERGY_KCAL).convertEnergyTo(ENERGY_KCAL).value).isWithin(DELTA).of(100f)
        assertThrows(IllegalArgumentException::class.java) { measure(1f, UNIT_GRAM).convertEnergyTo(ENERGY_KCAL) }
    }

    @Test
    fun testSaltSodiumConversion() {
        assertThat(1.0f.sodiumToSalt()).isWithin(DELTA).of(2.54f)
        assertThat(2.54f.saltToSodium()).isWithin(DELTA).of(1.0f)
    }

    companion object {
        private const val DELTA = 1e-5f
    }

}