package openfoodfacts.github.scrachx.openfood.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity

object ProductFragmentUtils {
    fun getSearchLinkText(
        text: String,
        type: SearchType,
        activityToStart: Activity,
    ): CharSequence {
        val span = ClickableSpan { ProductSearchActivity.start(activityToStart, type, text) }
        return buildSpannedString {
            inSpans(span) { append(text) }
        }
    }

    fun buildSignInDialog(
        context: Context,
        onPositive: (DialogInterface, Int) -> Unit = { d, _ -> d.dismiss() },
        onNegative: (DialogInterface, Int) -> Unit = { d, _ -> d.dismiss() },
    ): MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.sign_in_to_edit)
        .setPositiveButton(R.string.txtSignIn) { d, i -> onPositive(d, i) }
        .setNegativeButton(R.string.dialog_cancel) { d, i -> onNegative(d, i) }
}