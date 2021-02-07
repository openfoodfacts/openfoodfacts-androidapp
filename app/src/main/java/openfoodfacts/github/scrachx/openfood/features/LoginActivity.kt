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
package openfoodfacts.github.scrachx.openfood.features

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import okhttp3.ResponseBody
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.ActivityLoginBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager.productsApi
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import retrofit2.Response
import java.io.IOException
import java.net.HttpCookie

/**
 * A login screen that offers login via login/password.
 * This Activity connect to the Chrome Custom Tabs Service on startup to prefetch the url.
 */
class LoginActivity : BaseActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var customTabActivityHelper: CustomTabActivityHelper

    private val disp = CompositeDisposable()

    private var userLoginUri: Uri? = null
    private var resetPasswordUri: Uri? = null

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            super.onBackPressed()
            true
        }
        else -> false
    }

    private fun doAttemptLogin() {
        Utils.hideKeyboard(this)

        // Start checks
        val login = binding.loginInput.text.toString()
        val password = binding.passInput.text.toString()
        if (login.isBlank()) {
            binding.loginInput.error = getString(R.string.error_field_required)
            binding.loginInput.requestFocus()
            return
        }
        if (password.isBlank()) {
            binding.passInput.error = getString(R.string.error_field_required)
            binding.passInput.requestFocus()
            return
        }
        if (password.length < 6) {
            binding.passInput.error = getText(R.string.error_invalid_password)
            binding.passInput.requestFocus()
            return
        }
        // End checks

        val snackbar = Snackbar.make(binding.loginLinearlayout, R.string.toast_retrieving, BaseTransientBottomBar.LENGTH_LONG)
                .apply { show() }
        binding.btnLogin.isClickable = false

        productsApi.signIn(login, password, "Sign-in")
                .observeOn(AndroidSchedulers.mainThread()) // We need to modify view
                .doOnError {
                    Toast.makeText(this, this.getString(R.string.errorWeb), Toast.LENGTH_LONG).show()
                    Log.e(this::class.simpleName, "onFailure", it)
                }
                .subscribe { response: Response<ResponseBody> ->
                    if (!response.isSuccessful) {
                        Toast.makeText(this@LoginActivity, R.string.errorWeb, Toast.LENGTH_LONG).show()
                        return@subscribe
                    }
                    val htmlNoParsed = try {
                        response.body()?.string()
                    } catch (e: IOException) {
                        Log.e("LOGIN", "Unable to parse the login response page", e)
                        return@subscribe
                    }
                    val pref = this@LoginActivity.getSharedPreferences("login", 0)
                    if (htmlNoParsed == null
                            || htmlNoParsed.contains("Incorrect user name or password.")
                            || htmlNoParsed.contains("See you soon!")) {
                        Snackbar.make(binding.loginLinearlayout, R.string.errorLogin, BaseTransientBottomBar.LENGTH_LONG).show()
                        binding.passInput.setText("")
                        binding.txtInfoLogin.setTextColor(ContextCompat.getColor(this, R.color.red))
                        binding.txtInfoLogin.setText(R.string.txtInfoLoginNo)
                        snackbar.dismiss()
                    } else {
                        // store the user session id (user_session and user_id)
                        for (httpCookie in HttpCookie.parse(response.headers()["set-cookie"])) {
                            // Example format of set-cookie: session=user_session&S0MeR@nD0MSECRETk3Y&user_id&testuser; domain=.openfoodfacts.org; path=/
                            if (BuildConfig.HOST.contains(httpCookie.domain) && httpCookie.path == "/") {
                                val cookieValues = httpCookie.value.split("&")
                                var i = 0
                                while (i < cookieValues.size) {
                                    pref.edit { putString(cookieValues[i], cookieValues[++i]) }
                                    i++
                                }
                                break
                            }
                        }
                        Snackbar.make(binding.loginLinearlayout, R.string.connection, BaseTransientBottomBar.LENGTH_LONG).show()
                        pref.edit {
                            putString("user", login)
                            putString("pass", password)
                        }
                        binding.txtInfoLogin.setTextColor(ContextCompat.getColor(this, R.color.green_500))
                        binding.txtInfoLogin.setText(R.string.txtInfoLoginOk)
                        setResult(RESULT_OK)
                        finish()
                    }
                }.addTo(disp)
        binding.btnLogin.isClickable = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // check flavour and show helper text for user account
        if (!isFlavors(OFF)) {
            binding.txtLoginHelper.setText(R.string.txtloginHelper)
            binding.txtLoginHelper.visibility = View.VISIBLE
        }

        // Setup view listeners
        binding.btnLogin.setOnClickListener { doAttemptLogin() }
        binding.btnCreateAccount.setOnClickListener { doRegister() }
        binding.btnForgotPass.setOnClickListener { doForgotPassword() }
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.txtSignIn)

        userLoginUri = Uri.parse(getString(R.string.website) + "cgi/user.pl")
        resetPasswordUri = Uri.parse(getString(R.string.website) + "cgi/reset_password.pl")

        // prefetch the uri
        customTabActivityHelper = CustomTabActivityHelper()
        customTabActivityHelper.setConnectionCallback(
                onConnected = { binding.btnCreateAccount.isEnabled = true },
                onDisconnected = { binding.btnCreateAccount.isEnabled = false }
        )
        customTabActivityHelper.mayLaunchUrl(userLoginUri, null, null)
        binding.btnCreateAccount.isEnabled = true

        val loginS = getLoginPreferences().getString(resources.getString(R.string.user), resources.getString(R.string.txt_anonymous))
        if (loginS == resources.getString(R.string.user)) {
            MaterialDialog.Builder(this).apply {
                title(R.string.log_in)
                content(R.string.login_true)
                neutralText(R.string.ok_button)
                onNeutral { _, _ -> finish() }
                show()
            }
        }
    }

    private fun doRegister() {
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(baseContext, customTabActivityHelper.session)
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, userLoginUri!!, WebViewFallback())
    }

    private fun doForgotPassword() {
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(baseContext, customTabActivityHelper.session)
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, resetPasswordUri!!, WebViewFallback())
    }

    override fun onStart() {
        super.onStart()
        customTabActivityHelper.bindCustomTabsService(this)
    }

    override fun onStop() {
        super.onStop()
        customTabActivityHelper.unbindCustomTabsService(this)
        binding.btnCreateAccount.isEnabled = false
    }

    override fun onDestroy() {
        disp.dispose()
        customTabActivityHelper.connectionCallback = null
        _binding = null
        super.onDestroy()
    }

    companion object {
        class LoginContract : ActivityResultContract<Unit, Boolean>() {
            override fun createIntent(context: Context, input: Unit) = Intent(context, LoginActivity::class.java)

            override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
        }
    }
}