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
        assertThat(QuantityParserUtilKt.getFloatValue((String) null)).isNull();
        assertThat(QuantityParserUtilKt.getFloatValue("")).isNull();
        assertThat(QuantityParserUtilKt.getFloatValue("   ")).isNull();
        assertThat(QuantityParserUtilKt.getFloatValue((String) null)).isNull();
        assertThat(QuantityParserUtilKt.getFloatValue("")).isNull();
        assertThat(QuantityParserUtilKt.getFloatValue(" ")).isNull();
    }

    @Test
    public void testIsGreaterThan() {
        Spinner mockSpinner = mock(Spinner.class);
        when(mockSpinner.getSelectedItemPosition()).thenReturn(2);
        assertThat(QuantityParserUtilKt.isModifierEqualsToGreaterThan(mockSpinner)).isTrue();
        when(mockSpinner.getSelectedItemPosition()).thenReturn(1);
        assertThat(QuantityParserUtilKt.isModifierEqualsToGreaterThan(mockSpinner)).isFalse();
        when(mockSpinner.getSelectedItemPosition()).thenReturn(0);
        assertThat(QuantityParserUtilKt.isModifierEqualsToGreaterThan(mockSpinner)).isFalse();
    }
}
