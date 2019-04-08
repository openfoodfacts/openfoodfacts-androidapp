package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class UnitUtilTest {
    @Test
    public void testConvertFromGram() {
        Assert.assertEquals(0.001, UnitUtils.convertFromGram(1, UnitUtils.UNIT_KILOGRAM), 1e-5);
        Assert.assertEquals(1, UnitUtils.convertFromGram(1, UnitUtils.UNIT_GRAM), 1e-5);
        Assert.assertEquals(1000, UnitUtils.convertFromGram(1, UnitUtils.UNIT_MILLIGRAM), 1e-5);
        Assert.assertEquals(1000*1000, UnitUtils.convertFromGram(1, UnitUtils.UNIT_MICROGRAM), 1e-5);
    }

    @Test
    public void testConvertFromMl() {
        Assert.assertEquals(0.001, UnitUtils.convertFromGram(1, UnitUtils.UNIT_LITER), 1e-5);
        Assert.assertEquals(0.01, UnitUtils.convertFromGram(1, UnitUtils.UNIT_DECILITRE), 1e-5);
        Assert.assertEquals(0.1, UnitUtils.convertFromGram(1, UnitUtils.UNIT_CENTILITRE), 1e-5);
        Assert.assertEquals(1, UnitUtils.convertFromGram(1, UnitUtils.UNIT_MILLILITRE), 1e-5);
    }

    @Test
    public void testConvertToMl() {
        Assert.assertEquals(1000, UnitUtils.convertToGrams(1, UnitUtils.UNIT_LITER), 1e-5);
        Assert.assertEquals(100, UnitUtils.convertToGrams(1, UnitUtils.UNIT_DECILITRE), 1e-5);
        Assert.assertEquals(10, UnitUtils.convertToGrams(1, UnitUtils.UNIT_CENTILITRE), 1e-5);
        Assert.assertEquals(1, UnitUtils.convertToGrams(1, UnitUtils.UNIT_MILLILITRE), 1e-5);
    }

    @Test
    public void testConvertToGram() {
        Assert.assertEquals(1000, UnitUtils.convertToGrams(1, UnitUtils.UNIT_KILOGRAM), 1e-5);
        Assert.assertEquals(1, UnitUtils.convertToGrams(1, UnitUtils.UNIT_GRAM), 1e-5);
        Assert.assertEquals(1e-3, UnitUtils.convertToGrams(1, UnitUtils.UNIT_MILLIGRAM), 1e-5);
        Assert.assertEquals(1e-6, UnitUtils.convertToGrams(1, UnitUtils.UNIT_MICROGRAM), 1e-5);
    }
}
