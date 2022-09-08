package openfoodfacts.github.scrachx.openfood

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidTest
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.scanhistory.ScanHistoryActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
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
class TakeScreenshotShowProductsTest : AbstractScreenshotTest() {

    @Rule
    var activityHistoryRule =
        ScreenshotActivityTestRule(ScanHistoryActivity::class.java, context = context, localeManager = localeManager)

    @Rule
    var activityShowProductRule =
        ScreenshotActivityTestRule(ProductViewActivity::class.java, context = context, localeManager = localeManager)

    @Test
    fun testTakeScreenshot() {
        startForAllLocales(createProductIntent, listOf(activityShowProductRule), context)
        startForAllLocales(rules = listOf(activityHistoryRule), context = context)
    }

    private val createProductIntent: (ScreenshotParameter) -> List<Intent?> = { parameter ->
        parameter.productCodes.map { productCode ->
            Intent<ProductViewActivity>(context) {
                putExtra(KEY_STATE, ProductState().apply {
                    product = Product().apply { code = productCode }
                })
                putExtra(ACTION_NAME, ProductViewActivity::class.java.simpleName + "-" + productCode)
            }
        }
    }
}
