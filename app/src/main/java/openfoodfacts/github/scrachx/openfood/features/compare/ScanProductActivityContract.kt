package openfoodfacts.github.scrachx.openfood.features.compare

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity.Companion.KEY_COMPARE_PRODUCT
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity.Companion.KEY_PRODUCTS_TO_COMPARE
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.models.Product

class ScanProductActivityContract : ActivityResultContract<Unit, Product?>() {

    override fun createIntent(context: Context, input: Unit?): Intent {
        return Intent(context, ContinuousScanActivity::class.java)
            .putExtra(KEY_COMPARE_PRODUCT, true)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Product? {
        val bundle = intent?.extras ?: return null
        return if (resultCode == Activity.RESULT_OK && bundle.containsKey(KEY_PRODUCTS_TO_COMPARE)) {
            bundle.getSerializable(KEY_PRODUCTS_TO_COMPARE) as? Product
                ?: error("Unable to deserialize product from intent.")
        } else {
            null
        }
    }
}
