package openfoodfacts.github.scrachx.openfood.models

import android.widget.Spinner
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import openfoodfacts.github.scrachx.openfood.utils.modifier
import org.junit.jupiter.api.Test

class ModifierTest {
    @Test
    fun `test findBySymbol`() {
        assertThat(Modifier.findBySymbol(">")).isEqualTo(Modifier.GREATER_THAN)
        assertThat(Modifier.findBySymbol("<")).isEqualTo(Modifier.LESS_THAN)
        assertThat(Modifier.findBySymbol("=")).isEqualTo(Modifier.EQUALS_TO)
        assertThat(Modifier.findBySymbol("?")).isNull()
    }


    @Test
    fun `modifier should match index`() {
        val mockSpinner = mockk<Spinner>()
        every { mockSpinner.selectedItemPosition } returns 0
        assertThat(mockSpinner.modifier).isEqualTo(Modifier.EQUALS_TO)

        every { mockSpinner.selectedItemPosition } returns 1
        assertThat(mockSpinner.modifier).isEqualTo(Modifier.LESS_THAN)

        every { mockSpinner.selectedItemPosition } returns 2
        assertThat(mockSpinner.modifier).isEqualTo(Modifier.GREATER_THAN)
    }
}
