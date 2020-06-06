package openfoodfacts.github.scrachx.openfood.utils;

import android.widget.Spinner;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class QuantityParserTest {
    @Test
    public void testWithEmptyValues() {
        assertNull(QuantityParserUtil.getFloatValue((String) null));
        assertNull(QuantityParserUtil.getFloatValue(""));
        assertNull(QuantityParserUtil.getFloatValue("   "));
        assertNull(QuantityParserUtil.getFloatValue((String) null));
        assertNull(QuantityParserUtil.getFloatValue(""));
        assertNull(QuantityParserUtil.getFloatValue(" "));
    }

    @Test
    public void testIsGreaterThan() {
        Spinner mockSpinner = mock(Spinner.class);
        when(mockSpinner.getSelectedItemPosition()).thenReturn(2);
        assertTrue(QuantityParserUtil.isModifierEqualsToGreaterThan(mockSpinner));
        when(mockSpinner.getSelectedItemPosition()).thenReturn(1);
        assertFalse(QuantityParserUtil.isModifierEqualsToGreaterThan(mockSpinner));
        when(mockSpinner.getSelectedItemPosition()).thenReturn(0);
        assertFalse(QuantityParserUtil.isModifierEqualsToGreaterThan(mockSpinner));
    }
}
