package openfoodfacts.github.scrachx.openfood.images

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test


class ImageNameJsonParserTest {
    @Test
    fun testIsNameAccepted() {
        assertThat(isNameOk("n_test")).isFalse()
        assertThat(isNameOk("f_test")).isFalse()
        assertThat(isNameOk("i_test")).isFalse()
        assertThat(isNameOk("o_test")).isFalse()
        assertThat(isNameOk("")).isFalse()
        assertThat(isNameOk("test")).isTrue()
    }

    @Test
    fun testNameTimestampKey() {
        val key1 = NameTimestampKey("test", 1)
        val key2 = NameTimestampKey("test", 2)
        val key3 = NameTimestampKey("tesz", 1)

        assertThat(key1).isGreaterThan(key2)
        assertThat(key1).isLessThan(key3)
    }
}