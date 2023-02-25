package openfoodfacts.github.scrachx.openfood.features.simplescan

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import openfoodfacts.github.scrachx.openfood.models.Barcode
import openfoodfacts.github.scrachx.openfood.utils.Intent

/**
 * Returns a string containing the product barcode.
 */
class SimpleScanActivityContract : ActivityResultContract<Unit, Barcode?>() {

    companion object {
        const val KEY_SCANNED_BARCODE = "scanned_barcode"
    }

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent<SimpleScanActivity>(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Barcode? {
        val bundle = intent?.extras ?: return null
        bundle.getString(KEY_SCANNED_BARCODE, null)


        return if (resultCode == Activity.RESULT_OK && bundle.containsKey(KEY_SCANNED_BARCODE)) {
            Barcode(bundle.getString(KEY_SCANNED_BARCODE, null))
        } else
            null
    }
}
