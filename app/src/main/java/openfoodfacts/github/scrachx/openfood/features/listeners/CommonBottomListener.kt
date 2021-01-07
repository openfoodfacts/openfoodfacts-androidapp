package openfoodfacts.github.scrachx.openfood.features.listeners

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
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

class CommonBottomListener internal constructor(private val currentActivity: Activity) : BottomNavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.scan_bottom_nav -> openScanActivity()
            R.id.compare_products -> {
                if (isCurrentActivity(ProductCompareActivity::class.java)) {
                    return true
                }
                currentActivity.startActivity(createIntent(ProductCompareActivity::class.java))
            }
            R.id.home_page, R.id.home -> {
                if (isCurrentActivity(WelcomeActivity::class.java) || isCurrentActivity(MainActivity::class.java)) {
                    (currentActivity as FragmentActivity).supportFragmentManager.commit {
                        replace(R.id.fragment_container, HomeFragment())
                        addToBackStack(null)
                    }
                    return true
                }
                currentActivity.startActivity(createIntent(MainActivity::class.java))
            }
            R.id.history_bottom_nav -> {
                if (isCurrentActivity(ScanHistoryActivity::class.java)) {
                    return true
                }
                currentActivity.startActivity(createIntent(ScanHistoryActivity::class.java))
            }
            R.id.my_lists -> {
                if (isCurrentActivity(ProductListsActivity::class.java)) {
                    return true
                }
                currentActivity.startActivity(createIntent(ProductListsActivity::class.java))
            }
            else -> return true
        }
        return true
    }

    private fun openScanActivity() {
        // If already on the continuous scan activity, just lower the bottom sheet
        if (isCurrentActivity(ContinuousScanActivity::class.java)) {
            (currentActivity as ContinuousScanActivity).collapseBottomSheet()
            return
        }
        // If no camera is installed, alert the user
        if (!isHardwareCameraInstalled(currentActivity)) {
            MaterialDialog.Builder(currentActivity).run {
                title(R.string.no_camera_dialog_title)
                content(R.string.no_camera_dialog_content)
                neutralText(R.string.txtOk)
                show()
            }

            return
        }
        // Otherwise check permissions and go to continuous scan activity
        if (ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (currentActivity.hasWindowFocus() && ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, Manifest.permission.CAMERA)) {
                MaterialDialog.Builder(currentActivity)
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .onNeutral { _, _ ->
                            ActivityCompat.requestPermissions(
                                    currentActivity,
                                    arrayOf(Manifest.permission.CAMERA),
                                    MY_PERMISSIONS_REQUEST_CAMERA
                            )
                        }.show()
            } else {
                ActivityCompat.requestPermissions(currentActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            }
        } else {
            val intent = createIntent(ContinuousScanActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            currentActivity.startActivity(intent)
        }
    }

    ////////////////////
    // Utility functions
    private fun isCurrentActivity(activityClass: Class<out Activity?>) = currentActivity.javaClass == activityClass

    private fun createIntent(activityClass: Class<out Activity?>) = Intent(currentActivity, activityClass).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
}