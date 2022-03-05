package openfoodfacts.github.scrachx.openfood.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class FileUtilsTest {

    @Test
    fun `isLocaleFile returns true when file is local`() {
        assertThat(isLocaleFile(LOCAL_PATH)).isTrue()
    }

    @Test
    fun `isLocaleFile returns false when file is absolute`() {
        assertThat(isLocaleFile(ABSOLUTE_PATH)).isFalse()
    }

    @Test
    fun `isAbsoluteUrl returns true when file is local`() {
        assertThat(isAbsoluteUrl(LOCAL_PATH)).isFalse()
    }

    @Test
    fun `isAbsoluteUrl returns true when file is absolute`() {
        assertThat(isAbsoluteUrl(ABSOLUTE_PATH)).isTrue()
    }

    companion object {
        private const val ABSOLUTE_PATH = "/path"
        private const val LOCAL_PATH = "file://path"
    }
}