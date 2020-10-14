package openfoodfacts.github.scrachx.openfood.utils;

import android.widget.Spinner;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class QuantityParserTest {
    @Test
    public void testWithEmptyValues() {
        assertThat(QuantityParserUtil.getFloatValue((String) null)).isNull();
        assertThat(QuantityParserUtil.getFloatValue("")).isNull();
        assertThat(QuantityParserUtil.getFloatValue("   ")).isNull();
        assertThat(QuantityParserUtil.getFloatValue((String) null)).isNull();
        assertThat(QuantityParserUtil.getFloatValue("")).isNull();
        assertThat(QuantityParserUtil.getFloatValue(" ")).isNull();
    }

    @Test
    public void testIsGreaterThan() {
        Spinner mockSpinner = mock(Spinner.class);
        when(mockSpinner.getSelectedItemPosition()).thenReturn(2);
        assertThat(QuantityParserUtil.isModifierEqualsToGreaterThan(mockSpinner)).isTrue();
        when(mockSpinner.getSelectedItemPosition()).thenReturn(1);
        assertThat(QuantityParserUtil.isModifierEqualsToGreaterThan(mockSpinner)).isFalse();
        when(mockSpinner.getSelectedItemPosition()).thenReturn(0);
        assertThat(QuantityParserUtil.isModifierEqualsToGreaterThan(mockSpinner)).isFalse();
    }
}
