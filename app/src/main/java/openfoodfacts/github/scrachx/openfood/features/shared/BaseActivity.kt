/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features.shared

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.EntryPoints
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.hilt.AppEntryPoint
import openfoodfacts.github.scrachx.openfood.utils.Intent
import openfoodfacts.github.scrachx.openfood.utils.MY_PERMISSIONS_REQUEST_CAMERA
import openfoodfacts.github.scrachx.openfood.utils.isHardwareCameraInstalled

abstract class BaseActivity : AppCompatActivity() {

    val requestCameraThenOpenScan = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (it) startScanActivity() }

    override fun attachBaseContext(newBase: Context) {
        val lm = EntryPoints.get(newBase.applicationContext, AppEntryPoint::class.java).localeManager()
        super.attachBaseContext(lm.restoreLocalizedContext(newBase))
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA
            && grantResults.isNotEmpty()
            && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            startScanActivity()
        }
    }

    fun openScanActivity() {
        if (!isHardwareCameraInstalled()) {
            logcat { "Device does not have a camera installed" }
        }

        val perm = Manifest.permission.CAMERA
        when {
            checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED -> {
                startScanActivity()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, perm) -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.action_about)
                    .setMessage(R.string.permission_camera)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        requestCameraThenOpenScan.launch(Manifest.permission.CAMERA)
                    }
                    .show()
            }
            else -> {
                requestCameraThenOpenScan.launch(Manifest.permission.CAMERA)
            }
        }
    }

    protected open fun startScanActivity() {
        startActivity(Intent<ContinuousScanActivity>(this) {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
    }
}


