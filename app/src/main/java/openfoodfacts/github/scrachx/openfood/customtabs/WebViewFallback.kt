package openfoodfacts.github.scrachx.openfood.customtabs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper.CustomTabFallback

/**
 * Fallback of customtab with a standard web view
 */
class WebViewFallback : CustomTabFallback {
    override fun openUri(activity: Activity, uri: Uri) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            Log.e(WebViewFallback::class.simpleName, "Can't start activity" + activity::class.simpleName, e)
        }
    }
}