package openfoodfacts.github.scrachx.openfood.features.scan

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.utils.PreferencesService
import javax.inject.Inject


class MainActivityHelper @Inject constructor(
    private val prefManager: PreferencesService,
) {

    /** Dialog for rating the app on play store */
    fun showReviewDialog(activity: Activity, customTabsIntent: CustomTabsIntent) {
        val rateDialog = MaterialAlertDialogBuilder(activity).setTitle(R.string.app_name)
            .setMessage(R.string.user_ask_rate_app)
            .setPositiveButton(R.string.rate_app) { dialog, _ ->
                //open app page in play store
                activity.startActivity(Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${activity.packageName}"),
                ))
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no_thx) { dialog, _ -> dialog.dismiss() }

        //dialog for giving feedback
        val feedbackDialog = MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.app_name)
            .setMessage(R.string.user_ask_show_feedback_form)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                //show feedback form
                CustomTabActivityHelper.openCustomTab(
                    activity,
                    customTabsIntent,
                    activity.getString(R.string.feedback_form_url).toUri(),
                    WebViewFallback(),
                )
                dialog.dismiss()
            }.setNegativeButton(R.string.txtNo) { dialog, _ -> dialog.dismiss() }


        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.app_name)
            .setMessage(R.string.user_enjoying_app)
            .setPositiveButton(R.string.txtYes) { dialog, _ ->
                prefManager.userAskedToRate = true
                rateDialog.show()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.txtNo) { dialog, _ ->
                prefManager.userAskedToRate = true
                feedbackDialog.show()
                dialog.dismiss()
            }.show()
    }
}