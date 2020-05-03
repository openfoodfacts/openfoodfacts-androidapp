package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class QuantityParserTest {
    @Test
    public void testWithEmptyValues() {
        assertNull(QuantityParserUtil.getFloatValue((String) null, QuantityParserUtil.EntryFormat.NO_PREFIX));
        assertNull(QuantityParserUtil.getFloatValue("", QuantityParserUtil.EntryFormat.NO_PREFIX));
        assertNull(QuantityParserUtil.getFloatValue("   ", QuantityParserUtil.EntryFormat.NO_PREFIX));
        assertNull(QuantityParserUtil.getFloatValue((String) null, QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        assertNull(QuantityParserUtil.getFloatValue("", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        assertNull(QuantityParserUtil.getFloatValue(" ", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
    }

    @Test
    public void testGetPrefix() {
        assertNull(QuantityParserUtil.getModifier("+1.5"));
        assertEquals(">", QuantityParserUtil.getModifier(">1.5"));
        assertEquals("<", QuantityParserUtil.getModifier("<1.5"));
        assertEquals("~", QuantityParserUtil.getModifier("~1.5"));
    }

    @Test
    public void testIsGreaterThan() {
        assertTrue(QuantityParserUtil.isModifierEqualsToGreaterThan(">1.5"));
        assertFalse(QuantityParserUtil.isModifierEqualsToGreaterThan("<1.5"));
        assertFalse(QuantityParserUtil.isModifierEqualsToGreaterThan("~1.5"));
    }

    @Test
    public void testWithPrefixAndCorrectValues() {
        assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("1,5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue(">1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("~ 1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("< 1,5 ", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));

        assertEquals(Double.valueOf("1.5"), QuantityParserUtil.getDoubleValue(">1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        assertTrue(QuantityParserUtil.containFloatValue("1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
    }

    @Test
    public void testWithPrefixAndIncorrectValues() {
        assertNull(QuantityParserUtil.getFloatValue("> 1.5.1", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
        assertNull(QuantityParserUtil.getFloatValue("><1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));

        assertFalse(QuantityParserUtil.containFloatValue("><1.5", QuantityParserUtil.EntryFormat.WITH_KNOWN_PREFIX));
    }

    @Test
    public void testWithoutPrefixAndCorrectValues() {
        assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue("1.5", QuantityParserUtil.EntryFormat.NO_PREFIX));
        assertEquals(Float.valueOf("1.5"), QuantityParserUtil.getFloatValue(" 1,5 ", QuantityParserUtil.EntryFormat.NO_PREFIX));
    }

    @Test
    public void testWithoutPrefixAndIncorrectValues() {
        assertNull(QuantityParserUtil.getFloatValue("> 1.5", QuantityParserUtil.EntryFormat.NO_PREFIX));
        assertNull(QuantityParserUtil.getFloatValue("1.5a", QuantityParserUtil.EntryFormat.NO_PREFIX));
    }
}
