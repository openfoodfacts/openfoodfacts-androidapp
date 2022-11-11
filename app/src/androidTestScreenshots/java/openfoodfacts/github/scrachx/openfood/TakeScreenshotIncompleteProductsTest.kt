package openfoodfacts.github.scrachx.openfood

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.models.SearchInfo
import openfoodfacts.github.scrachx.openfood.test.ScreenshotActivityTestRule
import openfoodfacts.github.scrachx.openfood.test.ScreenshotParameter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Take screenshots...
 */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class TakeScreenshotIncompleteProductsTest : AbstractScreenshotTest() {

    @Rule
    var incompleteRule = ScreenshotActivityTestRule(
        ProductSearchActivity::class.java,
        "incompleteProducts",
        context,
        localeManager
    )

    @Test
    fun testTakeScreenshot() = startForAllLocales(createSearchIntent, listOf(incompleteRule), context)

    private val createSearchIntent: (ScreenshotParameter) -> List<Intent?> = {
        listOf(Intent<ProductSearchActivity>(context) {
            putExtra(ProductSearchActivity.SEARCH_INFO, SearchInfo.emptySearchInfo())
        })
    }
}
