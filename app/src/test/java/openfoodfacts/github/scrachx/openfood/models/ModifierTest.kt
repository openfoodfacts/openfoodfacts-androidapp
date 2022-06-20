package openfoodfacts.github.scrachx.openfood.models

import android.widget.Spinner
import com.google.common.truth.Truth.assertThat
import openfoodfacts.github.scrachx.openfood.utils.modifier
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
        assertThat(Modifier.GREATER_THAN.takeUnlessDefault()).isEqualTo(Modifier.GREATER_THAN)
        assertThat(Modifier.LESS_THAN.takeUnlessDefault()).isEqualTo(Modifier.LESS_THAN)
        assertThat(Modifier.EQUALS_TO.takeUnlessDefault()).isNull()
    }


    @Test
    fun `modifier should match index`() {
        val mockSpinner = mock<Spinner>()
        whenever(mockSpinner.selectedItemPosition) doReturn 0
        assertThat(mockSpinner.modifier).isEqualTo(Modifier.EQUALS_TO)

        whenever(mockSpinner.selectedItemPosition) doReturn 1
        assertThat(mockSpinner.modifier).isEqualTo(Modifier.LESS_THAN)

        whenever(mockSpinner.selectedItemPosition) doReturn 2
        assertThat(mockSpinner.modifier).isEqualTo(Modifier.GREATER_THAN)
    }
}
