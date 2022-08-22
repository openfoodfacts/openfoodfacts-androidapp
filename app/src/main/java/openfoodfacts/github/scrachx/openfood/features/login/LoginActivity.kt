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
package openfoodfacts.github.scrachx.openfood.features.login

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import openfoodfacts.github.scrachx.openfood.AppFlavor
import openfoodfacts.github.scrachx.openfood.AppFlavor.Companion.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.ActivityLoginBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import openfoodfacts.github.scrachx.openfood.utils.hideKeyboard
import javax.inject.Inject

/**
 * A login screen that offers login via login/password.
 * This Activity connect to the Chrome Custom Tabs Service on startup to prefetch the url.
 */
@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginActivityViewModel by viewModels()
    private var loadingSnackbar: Snackbar? = null

    @Inject
    lateinit var productsApi: ProductsAPI

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    private val customTabActivityHelper: CustomTabActivityHelper by lazy { CustomTabActivityHelper() }
    private val userLoginUri by lazy {
        resources.getString(R.string.user_login_url, getString(R.string.website)).toUri()
    }
    private val resetPasswordUri by lazy {
        resources.getString(R.string.reset_password_url, getString(R.string.website)).toUri()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            super.onBackPressed()
            true
        }
        else -> false
    }

    private fun doAttemptLogin() {
        hideKeyboard()

        binding.loginInputLayout.error = null
        binding.passInputLayout.error = null

        // Start checks
        val login = binding.loginInput.text.toString()
        val password = binding.passInput.text.toString()
        if (login.isBlank()) {
            binding.loginInputLayout.error = getString(R.string.error_field_required)
            binding.loginInputLayout.requestFocus()
            return
        }
        if (password.isBlank()) {
            binding.passInputLayout.error = getText(R.string.error_field_required)
            binding.passInputLayout.requestFocus()
            return
        } else if (password.length < 6) {
            binding.passInputLayout.error = getText(R.string.error_invalid_password)
            binding.passInputLayout.requestFocus()
            return
        }
        // End checks

        viewModel.tryLogin(login, password)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // check flavour and show helper text for user account
        if (!isFlavors(AppFlavor.OFF)) {
            binding.txtLoginHelper.visibility = View.VISIBLE
        }

        // Setup view listeners
        binding.btnLogin.setOnClickListener { doAttemptLogin() }
        binding.btnCreateAccount.setOnClickListener { openRegisterLink() }
        binding.btnForgotPass.setOnClickListener { openForgotPasswordLink() }

        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.txtSignIn)

        // Prefetch the uri
        customTabActivityHelper.setConnectionCallback(
            onConnected = { binding.btnCreateAccount.isEnabled = true },
            onDisconnected = { binding.btnCreateAccount.isEnabled = false }
        )
        customTabActivityHelper.mayLaunchUrl(userLoginUri, null, null)
        binding.btnCreateAccount.isEnabled = true

        finishIfAlreadyLogged()

        viewModel.loginButtonEnabled
            .flowWithLifecycle(lifecycle)
            .onEach { binding.btnLogin.isEnabled = it }
            .launchIn(lifecycleScope)

        viewModel.loginStatus
            .flowWithLifecycle(lifecycle)
            .onEach(::updateLoginStatus)
            .launchIn(lifecycleScope)
    }

    private fun updateLoginStatus(it: LoginActivityViewModel.LoginStatus) {
        when (it) {
            LoginActivityViewModel.LoginStatus.WebError -> {
                Toast.makeText(this, R.string.errorWeb, Toast.LENGTH_LONG).show()
            }
            LoginActivityViewModel.LoginStatus.IncorrectCredentials -> {
                loadingSnackbar?.dismiss()

                binding.txtInfoLogin.setTextColor(ContextCompat.getColor(this, R.color.red))
                binding.txtInfoLogin.setText(R.string.txtInfoLoginNo)

                binding.passInput.setText("")
            }
            LoginActivityViewModel.LoginStatus.Success -> {
                loadingSnackbar?.dismiss()

                binding.txtInfoLogin.setTextColor(ContextCompat.getColor(this, R.color.green_500))
                binding.txtInfoLogin.setText(R.string.txtInfoLoginOk)

                setResult(RESULT_OK)
                finish()
            }
            LoginActivityViewModel.LoginStatus.Loading -> {
                loadingSnackbar = Snackbar.make(binding.loginLinearlayout, R.string.toast_retrieving, LENGTH_LONG)
                loadingSnackbar!!.show()
            }
        }
    }

    private fun finishIfAlreadyLogged() {
        val username = getLoginPreferences().getString(resources.getString(R.string.user), null)
        if (username != null) {
            Toast.makeText(this, R.string.login_true, LENGTH_SHORT).show()
            finish()
        }
    }

    private fun openRegisterLink() {
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(this, customTabActivityHelper.session)
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, userLoginUri, WebViewFallback())
    }

    private fun openForgotPasswordLink() {
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(this, customTabActivityHelper.session)
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, resetPasswordUri, WebViewFallback())
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
        customTabActivityHelper.connectionCallback = null
        super.onDestroy()
    }

    companion object {
        class LoginContract : ActivityResultContract<Unit?, Boolean>() {
            override fun createIntent(context: Context, input: Unit?) = Intent(context, LoginActivity::class.java)
            override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
        }

        internal fun isHtmlNotValid(html: String?) = (html == null
                || "Incorrect user name or password." in html
                || "See you soon!" in html)
    }
}
