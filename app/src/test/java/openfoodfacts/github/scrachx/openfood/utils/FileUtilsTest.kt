package openfoodfacts.github.scrachx.openfood.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FileUtilsTest {
    private val absoluteURL = "/path"
    private val localURL = "file://path"

    @Test
    fun fileIsLocal_true() {
        assertThat(isLocaleFile(localURL)).isTrue()
    }

    @Test
    fun fileIsLocal_false() {
        assertThat(isLocaleFile(absoluteURL)).isFalse()
    }

    @Test
    fun isAbsolute_true() {
        assertThat(isAbsoluteUrl(absoluteURL)).isTrue()
    }

    @Test
    fun isAbsolute_false() {
        assertThat(isAbsoluteUrl(localURL)).isFalse()
    }
}