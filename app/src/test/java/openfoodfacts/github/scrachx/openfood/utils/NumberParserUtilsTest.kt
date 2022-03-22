package openfoodfacts.github.scrachx.openfood.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class NumberParserUtilsTest {
    @Test
    fun getAsFloat_inIsNull() {
        assertThat(getAsFloat(null, DEFAULT_FLOAT)).isWithin(0.0f).of(DEFAULT_FLOAT)
    }

    @Test
    fun getAsFloat_inIsInt() {
        assertThat(getAsFloat(3, DEFAULT_FLOAT)).isWithin(0.0f).of(3.0f)
    }

    @Test
    fun getAsFloat_inIsStringNumber() {
        assertThat(getAsFloat("3", DEFAULT_FLOAT)).isWithin(0.0f).of(3.0f)
    }

    @Test
    fun getAsFloat_inIsStringChar() {
        assertThat(getAsFloat("a", DEFAULT_FLOAT)).isWithin(0.0f).of(DEFAULT_FLOAT)
    }

    @Test
    fun getAsFloat_inIsStringBlank() {
        assertThat(getAsFloat(" ", DEFAULT_FLOAT)).isWithin(0.0f).of(DEFAULT_FLOAT)
    }

    @Test
    fun getAsInt_inIsNull() {
        assertThat(getAsInt(null, DEFAULT_INT)).isEqualTo(DEFAULT_INT)
    }

    @Test
    fun getAsInt_inIsFloat() {
        assertThat(getAsInt(3.0, DEFAULT_INT)).isEqualTo(3)
    }

    @Test
    fun getAsInt_inIsStringNumber() {
        assertThat(getAsInt("3", DEFAULT_INT)).isEqualTo(3)
    }

    @Test
    fun getAsInt_inIsStringChar() {
        assertThat(getAsInt("a", DEFAULT_INT)).isEqualTo(DEFAULT_INT)
    }

    @Test
    fun getAsInt_inIsStringBlank() {
        assertThat(getAsInt(" ", DEFAULT_INT)).isEqualTo(DEFAULT_INT)
    }

    companion object {
        private const val DEFAULT_FLOAT = 1.5f
        private const val DEFAULT_INT = 2
    }
}