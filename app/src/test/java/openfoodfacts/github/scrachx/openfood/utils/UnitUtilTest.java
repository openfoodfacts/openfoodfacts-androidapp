package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import openfoodfacts.github.scrachx.openfood.models.Units;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.Units.ENERGY_KCAL;
import static openfoodfacts.github.scrachx.openfood.models.Units.ENERGY_KJ;
import static openfoodfacts.github.scrachx.openfood.models.Units.UNIT_CENTILITRE;
import static openfoodfacts.github.scrachx.openfood.models.Units.UNIT_DECILITRE;
import static openfoodfacts.github.scrachx.openfood.models.Units.UNIT_GRAM;
import static openfoodfacts.github.scrachx.openfood.models.Units.UNIT_KILOGRAM;
import static openfoodfacts.github.scrachx.openfood.models.Units.UNIT_LITER;
import static openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MICROGRAM;
import static openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MILLIGRAM;
import static openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MILLILITRE;
import static org.junit.Assert.*;

/**
 *
 */
public class UnitUtilTest {
    private static final float DELTA = 1e-5f;

    @Test
    public void testGetServingInL() {
        assertThat(UnitUtils.getServingInL("1l")).isEqualTo("1l");

        assertThat(UnitUtils.getServingInL("33.814oz")).isEqualTo("1.0 l");
        assertThat(UnitUtils.getServingInL("33.814 oz")).isEqualTo("1.0 l");
    }

    @Test
    public void testGetServingInOz() {
        assertThat(UnitUtils.getServingInOz("1oz")).isEqualTo("1oz");
        // DEPENDS ON LOCALE
        /*
        assertThat(UnitUtils.getServingInOz("0.0295735l")).isEqualTo("1.00 oz");
        assertThat(UnitUtils.getServingInOz("0.0295735 l")).isEqualTo("1.00 oz");

        assertThat(UnitUtils.getServingInOz("2.95735cl")).isEqualTo("1 oz");
        assertThat(UnitUtils.getServingInOz("2.95735 cl")).isEqualTo("1 oz");

        assertThat(UnitUtils.getServingInOz("29.5735ml")).isEqualTo("1 oz");
        assertThat(UnitUtils.getServingInOz("29.5735 ml")).isEqualTo("1 oz");
        */
    }
// Not finished yet

    @Test
    public void testConvertFromGram() {
        assertThat(UnitUtils.convertFromGram(1, UNIT_KILOGRAM)).isWithin(DELTA).of(0.001f);
        assertThat(UnitUtils.convertFromGram(1, UNIT_GRAM)).isWithin(DELTA).of(1);
        assertThat(UnitUtils.convertFromGram(1, UNIT_MILLIGRAM)).isWithin(DELTA).of(1000);
        assertThat(UnitUtils.convertFromGram(1, UNIT_MICROGRAM)).isWithin(DELTA).of(1000 * 1000);
    }

    @Test
    public void testConvertFromMl() {
        assertThat(UnitUtils.convertFromGram(1, UNIT_LITER)).isWithin(DELTA).of(0.001f);
        assertThat(UnitUtils.convertFromGram(1, UNIT_DECILITRE)).isWithin(DELTA).of(0.01f);
        assertThat(UnitUtils.convertFromGram(1, UNIT_CENTILITRE)).isWithin(DELTA).of(0.1f);
        assertThat(UnitUtils.convertFromGram(1, UNIT_MILLILITRE)).isWithin(DELTA).of(1);
    }

    @Test
    public void testConvertToMl() {
        assertThat(UnitUtils.convertToGrams(1, UNIT_LITER)).isWithin(DELTA).of(1000);
        assertThat(UnitUtils.convertToGrams(1, UNIT_DECILITRE)).isWithin(DELTA).of(100);
        assertThat(UnitUtils.convertToGrams(1, UNIT_CENTILITRE)).isWithin(DELTA).of(10);
        assertThat(UnitUtils.convertToGrams(1, UNIT_MILLILITRE)).isWithin(DELTA).of(1);
    }

    @Test
    public void testConvertToGram() {
        assertThat(UnitUtils.convertToGrams(1, UNIT_KILOGRAM)).isWithin(DELTA).of(1000);
        assertThat(UnitUtils.convertToGrams(1, UNIT_GRAM)).isWithin(DELTA).of(1);
        assertThat(UnitUtils.convertToGrams(1, UNIT_MILLIGRAM)).isWithin(DELTA).of(1e-3f);
        assertThat(UnitUtils.convertToGrams(1, UNIT_MICROGRAM)).isWithin(DELTA).of(1e-6f);
    }

    @Test
    public void testConvertToKiloCalories() {
        assertThat(UnitUtils.convertToKiloCalories(100, ENERGY_KJ)).isEqualTo(23);
        assertThat(UnitUtils.convertToKiloCalories(100, ENERGY_KCAL)).isEqualTo(100);

        assertThrows(IllegalArgumentException.class, () -> UnitUtils.convertToKiloCalories(1, Units.UNIT_GRAM));
    }

    @Test
    public void testSaltSodiumConversion() {
        assertThat(UnitUtils.sodiumToSalt(1.0)).isWithin(DELTA).of(2.54);
        assertThat(UnitUtils.saltToSodium(2.54)).isWithin(DELTA).of(1.0);
    }
}
