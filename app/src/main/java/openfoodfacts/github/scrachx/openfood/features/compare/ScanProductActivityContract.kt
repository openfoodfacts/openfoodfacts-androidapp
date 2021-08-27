package openfoodfacts.github.scrachx.openfood.features.compare

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.models.Product

class ScanProductActivityContract : ActivityResultContract<Unit, Product?>() {

    override fun createIntent(context: Context, input: Unit?): Intent {
        return Intent(context, ContinuousScanActivity::class.java)
            .putExtra(ProductCompareActivity.KEY_COMPARE_PRODUCT, true)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Product? {
        return when (resultCode) {
            Activity.RESULT_OK -> intent?.extras?.getSerializable(ProductCompareActivity.KEY_PRODUCTS_TO_COMPARE) as? Product
            else -> null
        }
    }
}
