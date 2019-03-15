package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class FormatUtilTest {
    @Test
    public void testWithEmptyValues() {
        Assert.assertNull(FormatUtil.getFloatValue((String) null, FormatUtil.EntryFormat.NO_PREFIX));
        Assert.assertNull(FormatUtil.getFloatValue("", FormatUtil.EntryFormat.NO_PREFIX));
        Assert.assertNull(FormatUtil.getFloatValue("   ", FormatUtil.EntryFormat.NO_PREFIX));
        Assert.assertNull(FormatUtil.getFloatValue((String) null, FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertNull(FormatUtil.getFloatValue("", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertNull(FormatUtil.getFloatValue(" ", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
    }

    @Test
    public void testWithPrefixAndCorrectValues() {
        Assert.assertEquals(Float.valueOf("1.5"), FormatUtil.getFloatValue("1.5", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), FormatUtil.getFloatValue("1,5", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), FormatUtil.getFloatValue(">1.5", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), FormatUtil.getFloatValue("~ 1.5", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), FormatUtil.getFloatValue("< 1,5 ", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));

        Assert.assertEquals(Double.valueOf("1.5"), FormatUtil.getDoubleValue(">1.5", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertTrue(FormatUtil.containFloatValue("1.5", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
    }

    @Test
    public void testWithPrefixAndIncorrectValues() {
        Assert.assertNull(FormatUtil.getFloatValue("> 1.5.1", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
        Assert.assertNull(FormatUtil.getFloatValue("><1.5", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));

        Assert.assertFalse(FormatUtil.containFloatValue("><1.5", FormatUtil.EntryFormat.WITH_KNOWN_PREFIX));
    }

    @Test
    public void testWithoutPrefixAndCorrectValues() {
        Assert.assertEquals(Float.valueOf("1.5"), FormatUtil.getFloatValue("1.5", FormatUtil.EntryFormat.NO_PREFIX));
        Assert.assertEquals(Float.valueOf("1.5"), FormatUtil.getFloatValue(" 1,5 ", FormatUtil.EntryFormat.NO_PREFIX));
    }

    @Test
    public void testWithoutPrefixAndIncorrectValues() {
        Assert.assertNull(FormatUtil.getFloatValue("> 1.5", FormatUtil.EntryFormat.NO_PREFIX));
        Assert.assertNull(FormatUtil.getFloatValue("1.5a", FormatUtil.EntryFormat.NO_PREFIX));
    }
}
