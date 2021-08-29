package openfoodfacts.github.scrachx.openfood.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 *
 */
class QuantityParserTest {
    @Test
    fun `test getFloatValue`() {
        assertThat(getFloatValue(null)).isNull()
        assertThat(getFloatValue("")).isNull()
        assertThat(getFloatValue("   ")).isNull()
        assertThat(getFloatValue(null)).isNull()
        assertThat(getFloatValue("")).isNull()
        assertThat(getFloatValue(" ")).isNull()
    }
}