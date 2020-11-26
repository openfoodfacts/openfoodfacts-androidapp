package openfoodfacts.github.scrachx.openfood.utils

import com.google.common.truth.Truth
import org.junit.Test

class FileUtilsTest {
    private val absoluteURL = "/path"
    private val localURL = "file://path"

    @Test
    fun fileIsLocal_true() {
        Truth.assertThat(FileUtils.isLocaleFile(localURL)).isTrue()
    }

    @Test
    fun fileIsLocal_false() {
        Truth.assertThat(FileUtils.isLocaleFile(absoluteURL)).isFalse()
    }

    @Test
    fun isAbsolute_true() {
        Truth.assertThat(FileUtils.isAbsoluteUrl(absoluteURL)).isTrue()
    }

    @Test
    fun isAbsolute_false() {
        Truth.assertThat(FileUtils.isAbsoluteUrl(localURL)).isFalse()
    }
}