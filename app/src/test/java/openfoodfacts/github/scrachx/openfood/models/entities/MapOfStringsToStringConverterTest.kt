package openfoodfacts.github.scrachx.openfood.models.entities

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric is needed due to android.util.Base64 usage.
@RunWith(RobolectricTestRunner::class)
internal class MapOfStringsToStringConverterTest {

    private val testSubject = MapOfStringsToStringConverter()

    @Test
    fun convertToEntityProperty() {
        val input = """rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVz
            |aG9sZHhwP0AAAAAAAAN3CAAAAAQAAAACdAAEa2V5MXQABnZhbHVlMXQABGtleTJ0AAZ2YWx1ZTJ4""".trimMargin()

        val result = testSubject.convertToEntityProperty(input)

        assertThat(result).isEqualTo(mapOf("key1" to "value1", "key2" to "value2"))
    }

    @Test
    fun convertToDatabaseValue() {
        val map = mapOf("key1" to "value1", "key2" to "value2")

        val result = testSubject.convertToDatabaseValue(map)

        assertThat(result.trim()).isEqualTo("""rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVz
            |aG9sZHhwP0AAAAAAAAN3CAAAAAQAAAACdAAEa2V5MXQABnZhbHVlMXQABGtleTJ0AAZ2YWx1ZTJ4""".trimMargin())
    }
}
