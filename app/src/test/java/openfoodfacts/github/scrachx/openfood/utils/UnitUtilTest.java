package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import openfoodfacts.github.scrachx.openfood.models.Units;

import static org.junit.Assert.*;

/**
 *
 */
public class UnitUtilTest {

    private static final double DELTA = 1e-5;

    @Test
    public void testGetServingInL() {
        assertEquals("1l", UnitUtils.getServingInL("1l"));

        assertEquals("1.0 l", UnitUtils.getServingInL("33.814oz"));
        assertEquals("1.0 l", UnitUtils.getServingInL("33.814 oz"));
    }

    @Test
    public void testGetServingInOz() {
        assertEquals("1oz", UnitUtils.getServingInOz("1oz"));
        // DEPENDS ON LOCALE
        /*
        assertEquals("1.00 oz", UnitUtils.getServingInOz("0.0295735l"));
        assertEquals("1.00 oz", UnitUtils.getServingInOz("0.0295735 l"));

        assertEquals("1 oz", UnitUtils.getServingInOz("2.95735cl"));
        assertEquals("1 oz", UnitUtils.getServingInOz("2.95735 cl"));

        assertEquals("1 oz", UnitUtils.getServingInOz("29.5735ml"));
        assertEquals("1 oz", UnitUtils.getServingInOz("29.5735 ml"));
        */
    }
// Not finished yet

    @Test
    public void testConvertFromGram() {
        assertEquals(0.001, UnitUtils.convertFromGram(1, Units.UNIT_KILOGRAM), DELTA);
        assertEquals(1, UnitUtils.convertFromGram(1, Units.UNIT_GRAM), DELTA);
        assertEquals(1000, UnitUtils.convertFromGram(1, Units.UNIT_MILLIGRAM), DELTA);
        assertEquals(1000 * 1000, UnitUtils.convertFromGram(1, Units.UNIT_MICROGRAM), DELTA);
    }

    @Test
    public void testConvertFromMl() {
        assertEquals(0.001, UnitUtils.convertFromGram(1, Units.UNIT_LITER), DELTA);
        assertEquals(0.01, UnitUtils.convertFromGram(1, Units.UNIT_DECILITRE), DELTA);
        assertEquals(0.1, UnitUtils.convertFromGram(1, Units.UNIT_CENTILITRE), DELTA);
        assertEquals(1, UnitUtils.convertFromGram(1, Units.UNIT_MILLILITRE), DELTA);
    }

    @Test
    public void testConvertToMl() {
        assertEquals(1000, UnitUtils.convertToGrams(1, Units.UNIT_LITER), DELTA);
        assertEquals(100, UnitUtils.convertToGrams(1, Units.UNIT_DECILITRE), DELTA);
        assertEquals(10, UnitUtils.convertToGrams(1, Units.UNIT_CENTILITRE), DELTA);
        assertEquals(1, UnitUtils.convertToGrams(1, Units.UNIT_MILLILITRE), DELTA);
    }

    @Test
    public void testConvertToGram() {
        assertEquals(1000, UnitUtils.convertToGrams(1, Units.UNIT_KILOGRAM), DELTA);
        assertEquals(1, UnitUtils.convertToGrams(1, Units.UNIT_GRAM), DELTA);
        assertEquals(1e-3, UnitUtils.convertToGrams(1, Units.UNIT_MILLIGRAM), DELTA);
        assertEquals(1e-6, UnitUtils.convertToGrams(1, Units.UNIT_MICROGRAM), DELTA);
    }

    @Test
    public void testConvertToKiloCalories() {
        assertEquals(23, UnitUtils.convertToKiloCalories(100, Units.ENERGY_KJ), DELTA);
        assertEquals(100, UnitUtils.convertToKiloCalories(100, Units.ENERGY_KCAL), DELTA);

        assertThrows(IllegalArgumentException.class, () -> UnitUtils.convertToKiloCalories(1, Units.UNIT_GRAM));
    }

    @Test
    public void testSaltSodiumConversion() {
        assertEquals(2.54, UnitUtils.sodiumToSalt(1.0), DELTA);
        assertEquals(1, UnitUtils.saltToSodium(2.54), DELTA);
    }
}
