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
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(WebViewFallback::class.simpleName, "Can't start activity" + activity::class.simpleName, e)
        }
    }
}