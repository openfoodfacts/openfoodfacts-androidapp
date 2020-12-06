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

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication.Companion.appComponent
import openfoodfacts.github.scrachx.openfood.dagger.component.ActivityComponent
import openfoodfacts.github.scrachx.openfood.dagger.module.ActivityModule
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.onCreate
import openfoodfacts.github.scrachx.openfood.utils.MY_PERMISSIONS_REQUEST_CAMERA
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences

abstract class BaseActivity : AppCompatActivity() {
    var activityComponent: ActivityComponent? = null
        private set

    companion object {

        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        activityComponent = appComponent.plusActivityComponent(ActivityModule(this))
        activityComponent!!.inject(this)
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        onCreate(this)
    }

    fun getUserLogin(): String? {
        val preferences = getLoginPreferences()
        return preferences?.getString("user", null)
    }

    fun getUserSession(): String? {
        val preferences = getLoginPreferences()
        return preferences!!.getString("user_session", null)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA
                && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this@BaseActivity, ContinuousScanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
    }
}

