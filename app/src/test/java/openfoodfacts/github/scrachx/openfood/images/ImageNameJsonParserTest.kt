package openfoodfacts.github.scrachx.openfood.images

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.images.ImageNamesParser.isNameOk
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
}