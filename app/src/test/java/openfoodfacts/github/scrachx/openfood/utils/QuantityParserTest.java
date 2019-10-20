package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class QuantityParserTest {
    @Test
    public void testWithEmptyValues() {
        Assert.assertNull(QuantityParserUtil.getFloatValue((String) null, QuantityParserUtil.EntryFormat.NO_PREFIX));
        Assert.assertNull(QuantityParserUtil.getFloatValue("", QuantityParserUtil.EntryFormat.NO_PREFIX));
        Assert.assertNull(QuantityParserUtil.getFloatValue("   ", QuantityParserUtil.EntryFormat.NO_PREFIX));
        Assert.assertNull(QuantityParserUtil.getFloatValue((String) null, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertNull(QuantityParserUtil.getFloatValue("", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertNull(QuantityParserUtil.getFloatValue(" ", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
    }

    @Test
    public void testGetPrefix() {
        Assert.assertNull(QuantityParserUtil.getModifier("+1.5"));
        Assert.assertEquals(">", QuantityParserUtil.getModifier(">1.5"));
        Assert.assertEquals("<", QuantityParserUtil.getModifier("<1.5"));
        Assert.assertEquals("~", QuantityParserUtil.getModifier("~1.5"));
    }

    @Test
    public void testIsGreaterThan() {
        Assert.assertTrue(QuantityParserUtil.isModifierEqualsToGreaterThan(">1.5"));
        Assert.assertFalse(QuantityParserUtil.isModifierEqualsToGreaterThan("<1.5"));
        Assert.assertFalse(QuantityParserUtil.isModifierEqualsToGreaterThan("~1.5"));
    }

    @Test
    public void testWithPrefixAndCorrectValues() {
        Assert.assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("1,5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue(">1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("~ 1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("< 1,5 ", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));

        Assert.assertEquals(Double.valueOf("1.5"), QuantityParserUtil.getDoubleValue(">1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertTrue(QuantityParserUtil.containFloatValue("1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
    }

    @Test
    public void testWithPrefixAndIncorrectValues() {
        Assert.assertNull(QuantityParserUtil.getFloatValue("> 1.5.1", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertNull(QuantityParserUtil.getFloatValue("><1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));

        Assert.assertFalse(QuantityParserUtil.containFloatValue("><1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
    }

    @Test
    public void testWithoutPrefixAndCorrectValues() {
        Assert.assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("1.5", QuantityParserUtil.EntryFormat.NO_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue(" 1,5 ", QuantityParserUtil.EntryFormat.NO_PREFIX));
    }

    @Test
    public void testWithoutPrefixAndIncorrectValues() {
        Assert.assertNull(QuantityParserUtil.getFloatValue("> 1.5", QuantityParserUtil.EntryFormat.NO_PREFIX));
        Assert.assertNull(QuantityParserUtil.getFloatValue("1.5a", QuantityParserUtil.EntryFormat.NO_PREFIX));
    }
}
