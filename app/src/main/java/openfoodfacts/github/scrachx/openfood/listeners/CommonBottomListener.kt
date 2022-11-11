package openfoodfacts.github.scrachx.openfood.listeners

import android.app.Activity
import android.content.Intent
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.MainActivity
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity
import openfoodfacts.github.scrachx.openfood.features.home.HomeFragment
import openfoodfacts.github.scrachx.openfood.features.productlists.ProductListsActivity
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.scanhistory.ScanHistoryActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
import openfoodfacts.github.scrachx.openfood.utils.Intent
import openfoodfacts.github.scrachx.openfood.utils.isHardwareCameraInstalled

class CommonBottomListener internal constructor(
    private val currentActivity: BaseActivity,
) : NavigationBarView.OnItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.scan_bottom_nav -> {
                openScanActivity()
                true
            }
            R.id.compare_products -> {
                if (currentActivity !is ProductCompareActivity) {
                    currentActivity.startActivity(createIntent<ProductCompareActivity>())
                }
                true
            }
            R.id.home_page, R.id.home -> {
                if (currentActivity !is WelcomeActivity && currentActivity !is MainActivity) {
                    currentActivity.startActivity(createIntent<MainActivity>())
                } else {
                    openHomeFragment()
                }
                true
            }
            R.id.history_bottom_nav -> {
                if (currentActivity !is ScanHistoryActivity) {
                    currentActivity.startActivity(createIntent<ScanHistoryActivity>())
                }
                true
            }
            R.id.my_lists -> {
                if (currentActivity !is ProductListsActivity) {
                    currentActivity.startActivity(createIntent<ProductListsActivity>())
                }
                true
            }
            else -> true
        }
    }

    private fun openHomeFragment() {
        (currentActivity as FragmentActivity).supportFragmentManager.let {
            val fragment = it.fragments.lastOrNull()
            if (fragment == null || fragment !is HomeFragment) {
                it.commit {
                    replace(R.id.fragment_container, HomeFragment.newInstance())
                    addToBackStack(null)
                }
            }
        }
    }

    private fun openScanActivity() {
        val activity = currentActivity

        // If already on the continuous scan activity, just lower the bottom sheet
        if (activity is ContinuousScanActivity) {
            activity.collapseBottomSheet()
            return
        }

        // If no camera is installed, alert the user
        if (!isHardwareCameraInstalled(activity)) {
            MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.no_camera_dialog_title)
                .setMessage(R.string.no_camera_dialog_content)
                .setNeutralButton(android.R.string.ok) { d, _ -> d.dismiss() }
                .show()
            return
        }

        // Otherwise check permissions and go to continuous scan activity
        activity.openScanActivity()
    }

    ////////////////////
    // Utility functions
    private inline fun <reified T : Activity> createIntent(): Intent {
        return Intent<T>(currentActivity) {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
    }
}
