package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Assert;
import org.junit.Test;

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

    @Test
    public void testConvertFromGram() {
        assertEquals(0.001, UnitUtils.convertFromGram(1, UnitUtils.UNIT_KILOGRAM), DELTA);
        assertEquals(1, UnitUtils.convertFromGram(1, UnitUtils.UNIT_GRAM), DELTA);
        assertEquals(1000, UnitUtils.convertFromGram(1, UnitUtils.UNIT_MILLIGRAM), DELTA);
        assertEquals(1000 * 1000, UnitUtils.convertFromGram(1, UnitUtils.UNIT_MICROGRAM), DELTA);
    }

    @Test
    public void testConvertFromMl() {
        assertEquals(0.001, UnitUtils.convertFromGram(1, UnitUtils.UNIT_LITER), DELTA);
        assertEquals(0.01, UnitUtils.convertFromGram(1, UnitUtils.UNIT_DECILITRE), DELTA);
        assertEquals(0.1, UnitUtils.convertFromGram(1, UnitUtils.UNIT_CENTILITRE), DELTA);
        assertEquals(1, UnitUtils.convertFromGram(1, UnitUtils.UNIT_MILLILITRE), DELTA);
    }

    @Test
    public void testConvertToMl() {
        assertEquals(1000, UnitUtils.convertToGrams(1, UnitUtils.UNIT_LITER), DELTA);
        assertEquals(100, UnitUtils.convertToGrams(1, UnitUtils.UNIT_DECILITRE), DELTA);
        assertEquals(10, UnitUtils.convertToGrams(1, UnitUtils.UNIT_CENTILITRE), DELTA);
        assertEquals(1, UnitUtils.convertToGrams(1, UnitUtils.UNIT_MILLILITRE), DELTA);
    }

    @Test
    public void testConvertToGram() {
        assertEquals(1000, UnitUtils.convertToGrams(1, UnitUtils.UNIT_KILOGRAM), DELTA);
        assertEquals(1, UnitUtils.convertToGrams(1, UnitUtils.UNIT_GRAM), DELTA);
        assertEquals(1e-3, UnitUtils.convertToGrams(1, UnitUtils.UNIT_MILLIGRAM), DELTA);
        assertEquals(1e-6, UnitUtils.convertToGrams(1, UnitUtils.UNIT_MICROGRAM), DELTA);
    }

    @Test
    public void testConvertToKiloCalories() {
        assertEquals(0.239006, UnitUtils.convertToKiloCalories(1, UnitUtils.ENERGY_KJ), DELTA);
        assertEquals(1, UnitUtils.convertToKiloCalories(1, UnitUtils.ENERGY_KCAL), DELTA);
        try {
            UnitUtils.convertToKiloCalories(1, UnitUtils.UNIT_GRAM);
            Assert.fail("Exception 'IllegalArgumentException not thrown");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testSaltSodiumConversion() {
        assertEquals(2.54, UnitUtils.sodiumToSalt(1.0), DELTA);
        assertEquals(1, UnitUtils.saltToSodium(2.54), DELTA);
    }
}
