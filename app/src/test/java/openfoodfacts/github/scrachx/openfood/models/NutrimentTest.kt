package openfoodfacts.github.scrachx.openfood.models

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class NutrimentTest {

    @Test
    fun `test findByKey`() {
        Nutriment.values().forEach {
            assertThat(Nutriment.findbyKey(it.key)).isEqualTo(it)
        }
        assertThat(Nutriment.findbyKey("?")).isNull()
    }

    @Test
    fun `test requireByKey`() {
        assertThrows(IllegalArgumentException::class.java) { Nutriment.requireByKey("?") }
    }

}