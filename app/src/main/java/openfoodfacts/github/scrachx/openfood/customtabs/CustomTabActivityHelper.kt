// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License")
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package openfoodfacts.github.scrachx.openfood.customtabs

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper.getPackageNameToUse

/**
 * This is a helper class to manage the connection to the Custom Tabs Service.
 */
class CustomTabActivityHelper : ServiceConnectionCallback {
    private var mCustomTabsSession: CustomTabsSession? = null
    private var mClient: CustomTabsClient? = null
    private var mConnection: CustomTabsServiceConnection? = null

    /**
     * Callback to be called when connected or disconnected from the Custom Tabs Service.
     *
     */
    var connectionCallback: ConnectionCallback? = null

    fun setConnectionCallback(
            onConnected: () -> Unit = {},
            onDisconnected: () -> Unit = {}
    ) {
        connectionCallback = object : ConnectionCallback {
            override fun onCustomTabsConnected() = onConnected()
            override fun onCustomTabsDisconnected() = onDisconnected()
        }
    }


    /**
     * Unbinds the Activity from the Custom Tabs Service.
     *
     * @param activity the activity that is connected to the service.
     */
    fun unbindCustomTabsService(activity: Activity) {
        if (mConnection == null) return
        activity.unbindService(mConnection!!)
        mClient = null
        mCustomTabsSession = null
        mConnection = null
    }

    /**
     * Creates or retrieves an exiting CustomTabsSession.
     *
     * @return a CustomTabsSession.
     */
    val session: CustomTabsSession?
        get() {
            if (mClient == null) {
                mCustomTabsSession = null
            } else if (mCustomTabsSession == null) {
                mCustomTabsSession = mClient!!.newSession(null)
            }
            return mCustomTabsSession
        }

    /**
     * Binds the Activity to the Custom Tabs Service.
     *
     * @param activity the activity to be binded to the service.
     */
    fun bindCustomTabsService(activity: Activity?) {
        if (mClient != null) {
            return
        }
        val packageName = getPackageNameToUse(activity!!) ?: return
        ServiceConnection(this).let {
            mConnection = it
            CustomTabsClient.bindCustomTabsService(activity, packageName, it)
        }
    }

    /**
     * @return true if call to mayLaunchUrl was accepted.
     * @see CustomTabsSession.mayLaunchUrl
     */
    fun mayLaunchUrl(uri: Uri?, extras: Bundle?, otherLikelyBundles: List<Bundle?>?): Boolean {
        if (mClient == null) return false

        val session = session
        return session != null && session.mayLaunchUrl(uri!!, extras, otherLikelyBundles)
    }

    override fun onServiceConnected(client: CustomTabsClient) {
        mClient = client.apply { warmup(0L) }

        connectionCallback?.onCustomTabsConnected()
    }

    override fun onServiceDisconnected() {
        mClient = null
        mCustomTabsSession = null

        connectionCallback?.onCustomTabsDisconnected()
    }

    /**
     * A Callback for when the service is connected or disconnected. Use those callbacks to
     * handle UI changes when the service is connected or disconnected.
     */
    interface ConnectionCallback {
        /**
         * Called when the service is connected.
         */
        fun onCustomTabsConnected()

        /**
         * Called when the service is disconnected.
         */
        fun onCustomTabsDisconnected()
    }

    /**
     * To be used as a fallback to open the Uri when Custom Tabs is not available.
     */
    fun interface CustomTabFallback {
        /**
         * @param activity The Activity that wants to open the Uri.
         * @param uri The uri to be opened by the fallback.
         */
        fun openUri(activity: Activity, uri: Uri)
    }

    companion object {
        /**
         * Opens the URL on a Custom Tab if possible. Otherwise falls back to opening it on a WebView.
         *
         * @param activity The host activity.
         * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
         * @param uri the Uri to be opened.
         * @param fallback a CustomTabFallback to be used if Custom Tabs is not available.
         */
        fun openCustomTab(
                activity: Activity,
                customTabsIntent: CustomTabsIntent,
                uri: Uri,
                fallback: CustomTabFallback?
        ) {
            val packageName = getPackageNameToUse(activity)

            //If we cant find a package name, it means theres no browser that supports
            //Chrome Custom Tabs installed. So, we fallback to the webview
            if (packageName == null) {
                fallback?.openUri(activity, uri)
            } else {
                customTabsIntent.intent.setPackage(packageName)
                customTabsIntent.launchUrl(activity, uri)
            }
        }
    }
}