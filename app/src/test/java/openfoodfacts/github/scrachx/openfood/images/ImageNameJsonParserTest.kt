package openfoodfacts.github.scrachx.openfood.images

import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.images.ImageNameParser.isValidImageName
import org.junit.jupiter.api.Test


class ImageNameJsonParserTest {
    @Test
    fun testIsNameAccepted() {
        assertThat(isValidImageName("n_test")).isFalse()
        assertThat(isValidImageName("f_test")).isFalse()
        assertThat(isValidImageName("i_test")).isFalse()
        assertThat(isValidImageName("o_test")).isFalse()
        assertThat(isValidImageName("")).isFalse()
        assertThat(isValidImageName("test")).isTrue()
    }
}
