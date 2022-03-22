package openfoodfacts.github.scrachx.openfood.features.product.view.contributors

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ContributorsFragmentTest {
    @Test
    fun `test incomplete states`() {
        assertThat(ContributorsFragment.isIncompleteState("images-not-selected")).isTrue()
    }
}