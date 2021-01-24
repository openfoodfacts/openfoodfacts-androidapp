package openfoodfacts.github.scrachx.openfood

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import openfoodfacts.github.scrachx.openfood.test.ScreenshotParameter
import openfoodfacts.github.scrachx.openfood.models.SearchInfo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4::class)
class TakeScreenshotIncompleteProductsTest : AbstractScreenshotTest() {
    @Rule
    var incompleteRule = ScreenshotActivityTestRule(
            ProductSearchActivity::class.java, "incompleteProducts",
    )

    @Test
    fun testTakeScreenshot() = startForAllLocales(createSearchIntent, listOf(incompleteRule))

    companion object {
        private val createSearchIntent: (ScreenshotParameter) -> List<Intent?> = {
            listOf(Intent(OFFApplication.instance, ProductSearchActivity::class.java).apply {
                putExtra(ProductSearchActivity.SEARCH_INFO, SearchInfo.emptySearchInfo())
            })
        }
    }
}
