package openfoodfacts.github.scrachx.openfood.features.scan

import android.app.Activity
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.play.core.review.ReviewManagerFactory
import javax.inject.Inject

class MainActivityHelper @Inject constructor() {
    /** Dialog for rating the app on play store */
    fun showReviewDialog(
        activity: Activity,
        @Suppress("UNUSED_PARAMETER") customTabsIntent: CustomTabsIntent,
    ) {
        //dialog for rating the app on play store
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo)
            }
        }
    }
}