package openfoodfacts.github.scrachx.openfood.utils

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit
import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit.*
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 *
 */
class UnitUtilsTest {
    @Test
    fun testGetServingInL() {
        val measure = measure(1f, UNIT_LITER)
        assertThat(getServingInL("1l")).isEqualTo(measure)
        assertThat(getServingInL("33.814oz")).isEqualTo(measure)
        assertThat(getServingInL("33.814 oz")).isEqualTo(measure)
        assertThat(getServingInL("33 hdl")).isNull()
    }

    @Test
    fun testGetServingInOz() {
        val measure = measure(1f, UNIT_OZ)
        assertThat(getServingInOz("1oz")).isEqualTo(measure)

        getServingInOz("0.0295735l")!!.let {
            assertThat(it.unit).isEqualTo(UNIT_OZ)
            assertThat(it.value).isWithin(TOL).of(1f)
        }
        getServingInOz("0.0295735 l")!!.let {
            assertThat(it.unit).isEqualTo(UNIT_OZ)
            assertThat(it.value).isWithin(TOL).of(1f)
        }
        getServingInOz("2.95735cl")!!.let {
            assertThat(it.unit).isEqualTo(UNIT_OZ)
            assertThat(it.value).isWithin(TOL).of(1f)
        }
        getServingInOz("2.95735 cl")!!.let {
            assertThat(it.unit).isEqualTo(UNIT_OZ)
            assertThat(it.value).isWithin(TOL).of(1f)
        }

        getServingInOz("29.5735ml")!!.let {
            assertThat(it.unit).isEqualTo(UNIT_OZ)
            assertThat(it.value).isWithin(TOL).of(1f)
        }
        getServingInOz("29.5735 ml")!!.let {
            assertThat(it.unit).isEqualTo(UNIT_OZ)
            assertThat(it.value).isWithin(TOL).of(1f)
        }

        assertThat(getServingInOz("25 hdl")).isNull()

    }

    // Not finished yet
    @Test
    fun `test convert from grams`() {
        assertThat(measure(1f, UNIT_GRAM).convertTo(UNIT_KILOGRAM).value).isWithin(TOL).of(1e-3f)
        assertThat(measure(1f, UNIT_GRAM).convertTo(UNIT_GRAM).value).isWithin(TOL).of(1f)
        assertThat(measure(1f, UNIT_GRAM).convertTo(UNIT_MILLIGRAM).value).isWithin(TOL).of(1e3f)
        assertThat(measure(1f, UNIT_GRAM).convertTo(UNIT_MICROGRAM).value).isWithin(TOL).of(1e6f)
    }

    @Test
    fun `test convert from ml`() {
        assertThat(measure(1f, UNIT_MILLILITRE).convertTo(UNIT_LITER).value).isWithin(TOL).of(1e-3f)
        assertThat(measure(1f, UNIT_MILLILITRE).convertTo(UNIT_DECILITRE).value).isWithin(TOL).of(1e-2f)
        assertThat(measure(1f, UNIT_MILLILITRE).convertTo(UNIT_CENTILITRE).value).isWithin(TOL).of(0.1f)
        assertThat(measure(1f, UNIT_MILLILITRE).convertTo(UNIT_MILLILITRE).value).isWithin(TOL).of(1f)
    }

    @Test
    fun `test convert to ml`() {
        assertThat(measure(1f, UNIT_LITER).grams.value).isWithin(TOL).of(1e3f)
        assertThat(measure(1f, UNIT_DECILITRE).grams.value).isWithin(TOL).of(1e2f)
        assertThat(measure(1f, UNIT_CENTILITRE).grams.value).isWithin(TOL).of(10f)
        assertThat(measure(1f, UNIT_MILLILITRE).grams.value).isWithin(TOL).of(1f)
    }

    @Test
    fun `test grams conversion`() {
        assertThat(measure(1f, UNIT_KILOGRAM).grams.value).isWithin(TOL).of(1e3f)
        assertThat(measure(0.5f, UNIT_KILOGRAM).grams.value).isWithin(TOL).of(5e2f)
        assertThat(measure(1f, UNIT_GRAM).grams.value).isWithin(TOL).of(1f)
        assertThat(measure(1f, UNIT_MILLIGRAM).grams.value).isWithin(TOL).of(1e-3f)
        assertThat(measure(0.5f, UNIT_MILLIGRAM).grams.value).isWithin(TOL).of(5e-4f)
        assertThat(measure(1f, UNIT_MICROGRAM).grams.value).isWithin(TOL).of(1e-6f)
    }


    @Test
    fun `test energy conversion`() {
        assertThat(measure(5f, ENERGY_KCAL).convertEnergyTo(ENERGY_KJ).value).isWithin(TOL).of(20.92f)
        assertThrows(IllegalArgumentException::class.java) { measure(5f, UNIT_GRAM).convertEnergyTo(ENERGY_KJ) }
    }

    @Test
    fun `test display string`() {
        assertThat(measure(5.92f, UNIT_GRAM).displayString()).isIn(listOf("5.92 g", "5,92 g"))
        assertThat(measure(5f, UNIT_GRAM).displayString()).isEqualTo("5 g")
    }

    @Test
    fun `test salt-sodium conversion`() {
        assertThat(measure(5f, UNIT_GRAM).sodiumToSalt().value).isWithin(TOL).of(12.7f)
        assertThat(measure(5f, UNIT_GRAM).saltToSodium().value).isWithin(TOL).of(1.96850393701f)
    }

    @Test
    fun `test serving size parsing`() {
        assertThat(parseServing("25g")).isEqualTo("25" to UNIT_GRAM)
        assertThat(parseServing("25 g")).isEqualTo("25" to UNIT_GRAM)
        assertThat(parseServing("25.7g")).isEqualTo("25.7" to UNIT_GRAM)
        assertThat(parseServing("25.7 g")).isEqualTo("25.7" to UNIT_GRAM)
        assertThat(parseServing("25,7g")).isEqualTo("25,7" to UNIT_GRAM)
        assertThat(parseServing("25,7 g")).isEqualTo("25,7" to UNIT_GRAM)
        assertThat(parseServing("25.5.7g")).isEqualTo(Pair<String, MeasurementUnit?>("25.5", null))
        assertThat(parseServing("25.5.7")).isEqualTo(Pair<String, MeasurementUnit?>("25.5", null))
    }

    infix fun <A, B> A.toNull(second: B): Pair<A, B?> = (this to second)

    companion object {
        private const val TOL = 1e-5f
    }

}