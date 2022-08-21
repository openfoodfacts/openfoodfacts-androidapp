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
        for (mod in Modifier.values()) {
            assertThat(Modifier.findBySymbol(mod.sym)).isEqualTo(mod)
        }
    }


    @Test
    fun `modifier should match index`() {
        val mockSpinner = mockk<Spinner>()

        for (mod in Modifier.values()) {
            every { mockSpinner.selectedItem } returns (mod.sym as Any)
            assertThat(mockSpinner.modifier).isEqualTo(mod)
        }
    }
}
