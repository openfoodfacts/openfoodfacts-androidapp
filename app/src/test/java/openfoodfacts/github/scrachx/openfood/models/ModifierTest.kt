package openfoodfacts.github.scrachx.openfood.models

import android.widget.Spinner
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.utils.modifier
import org.junit.Test
import org.mockito.Mockito

class ModifierTest {
    @Test
    fun `test findBySymbol`() {
        assertThat(Modifier.findBySymbol(">")).isEqualTo(Modifier.GREATER_THAN)
        assertThat(Modifier.findBySymbol("<")).isEqualTo(Modifier.LESS_THAN)
        assertThat(Modifier.findBySymbol("=")).isEqualTo(Modifier.EQUALS_TO)
        assertThat(Modifier.findBySymbol("?")).isNull()
    }


    @Test
    fun `test nullIfDefault`() {
        Modifier.values().forEach {
            assertThat(it.nullIfDefault()).run {
                if (it == Modifier.EQUALS_TO) isNull()
                else isEqualTo(it)
            }
        }
    }


    @Test
    fun `modifier should match index`() {
        val mockSpinner = Mockito.mock(Spinner::class.java)
        Mockito.`when`(mockSpinner.selectedItemPosition).thenReturn(0)
        assertThat(mockSpinner.modifier).isEqualTo(Modifier.EQUALS_TO)

        Mockito.`when`(mockSpinner.selectedItemPosition).thenReturn(1)
        assertThat(mockSpinner.modifier).isEqualTo(Modifier.LESS_THAN)

        Mockito.`when`(mockSpinner.selectedItemPosition).thenReturn(2)
        assertThat(mockSpinner.modifier).isEqualTo(Modifier.GREATER_THAN)
    }
}
