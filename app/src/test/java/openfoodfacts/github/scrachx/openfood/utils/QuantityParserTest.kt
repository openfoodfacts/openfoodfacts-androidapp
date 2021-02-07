package openfoodfacts.github.scrachx.openfood.utils

import android.widget.Spinner
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as mockitoWhen

/**
 *
 */
class QuantityParserTest {
    @Test
    fun testWithEmptyValues() {
        assertThat(getFloatValue(null)).isNull()
        assertThat(getFloatValue("")).isNull()
        assertThat(getFloatValue("   ")).isNull()
        assertThat(getFloatValue(null)).isNull()
        assertThat(getFloatValue("")).isNull()
        assertThat(getFloatValue(" ")).isNull()
    }

    @Test
    fun testIsGreaterThan() {
        val mockSpinner = mock(Spinner::class.java)
        mockitoWhen(mockSpinner.selectedItemPosition).thenReturn(2)
        assertThat(mockSpinner.isModifierEqualsToGreaterThan()).isTrue()

        mockitoWhen(mockSpinner.selectedItemPosition).thenReturn(1)
        assertThat(mockSpinner.isModifierEqualsToGreaterThan()).isFalse()

        mockitoWhen(mockSpinner.selectedItemPosition).thenReturn(0)
        assertThat(mockSpinner.isModifierEqualsToGreaterThan()).isFalse()
    }
}