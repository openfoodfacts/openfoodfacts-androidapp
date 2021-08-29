package openfoodfacts.github.scrachx.openfood.listeners

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.HomeFragment
import openfoodfacts.github.scrachx.openfood.features.MainActivity
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity
import openfoodfacts.github.scrachx.openfood.features.productlists.ProductListsActivity
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.scanhistory.ScanHistoryActivity
import openfoodfacts.github.scrachx.openfood.features.welcome.WelcomeActivity
import openfoodfacts.github.scrachx.openfood.utils.MY_PERMISSIONS_REQUEST_CAMERA
import openfoodfacts.github.scrachx.openfood.utils.isHardwareCameraInstalled

class CommonBottomListener internal constructor(
    private val currentActivity: Activity
) : BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.scan_bottom_nav -> {
                openScanActivity()
                true
            }
            R.id.compare_products -> {
                if (!isCurrentActivity(ProductCompareActivity::class.java)) {
                    currentActivity.startActivity(createIntent(ProductCompareActivity::class.java))
                }
                true
            }
            R.id.home_page, R.id.home -> {
                if (!isCurrentActivity(WelcomeActivity::class.java) && !isCurrentActivity(MainActivity::class.java)) {
                    currentActivity.startActivity(createIntent(MainActivity::class.java))
                } else openHomeFragment()
                true
            }
            R.id.history_bottom_nav -> {
                if (!isCurrentActivity(ScanHistoryActivity::class.java)) {
                    currentActivity.startActivity(createIntent(ScanHistoryActivity::class.java))
                }
                true
            }
            R.id.my_lists -> {
                if (!isCurrentActivity(ProductListsActivity::class.java)) {
                    currentActivity.startActivity(createIntent(ProductListsActivity::class.java))
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
        // If already on the continuous scan activity, just lower the bottom sheet
        if (isCurrentActivity(ContinuousScanActivity::class.java)) {
            (currentActivity as ContinuousScanActivity).collapseBottomSheet()
            return
        }

        // If no camera is installed, alert the user
        if (!isHardwareCameraInstalled(currentActivity)) {
            MaterialAlertDialogBuilder(currentActivity)
                .setTitle(R.string.no_camera_dialog_title)
                .setMessage(R.string.no_camera_dialog_content)
                .setNeutralButton(android.R.string.ok) { d, _ -> d.dismiss() }
                .show()
            return
        }

        // Otherwise check permissions and go to continuous scan activity
        when {
            PERMISSION_GRANTED == checkSelfPermission(currentActivity, Manifest.permission.CAMERA) -> {
                currentActivity.startActivity(createIntent(ContinuousScanActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            }
            currentActivity.hasWindowFocus() && shouldShowRequestPermissionRationale(currentActivity, Manifest.permission.CAMERA) -> {
                MaterialAlertDialogBuilder(currentActivity)
                    .setTitle(R.string.action_about)
                    .setMessage(R.string.permission_camera)
                    .setNeutralButton(android.R.string.ok) { _, _ ->
                        ActivityCompat.requestPermissions(
                            currentActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            MY_PERMISSIONS_REQUEST_CAMERA
                        )
                    }.show()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    currentActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_REQUEST_CAMERA
                )
            }
        }
    }

    ////////////////////
    // Utility functions
    private fun isCurrentActivity(activityClass: Class<out Activity?>) = currentActivity.javaClass == activityClass

    private fun createIntent(activityClass: Class<out Activity?>) = Intent(currentActivity, activityClass).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
}
