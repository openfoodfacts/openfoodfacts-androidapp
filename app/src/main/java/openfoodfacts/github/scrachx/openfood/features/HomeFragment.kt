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

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentHomeBinding
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity.Companion.LoginContract
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import openfoodfacts.github.scrachx.openfood.utils.getUserAgent
import java.io.IOException
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

/**
 * @see R.layout.fragment_home
 */
@AndroidEntryPoint
class HomeFragment : NavigationBaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var productsApi: ProductsAPI

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    private var taglineURL: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTagLine.setOnClickListener { openDailyFoodFacts() }
        checkUserCredentials()
    }

    override fun onDestroyView() {
        // Stop the call to server to get total product count and tagline
        super.onDestroyView()
        _binding = null
    }

    private fun openDailyFoodFacts() {
        // chrome custom tab init
        val dailyFoodFactUri = Uri.parse(taglineURL)
        val customTabActivityHelper = CustomTabActivityHelper().apply {
            mayLaunchUrl(dailyFoodFactUri, null, null)
        }
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(
            requireActivity(),
            customTabActivityHelper.session,
        )
        CustomTabActivityHelper.openCustomTab(
            requireActivity(),
            customTabsIntent,
            dailyFoodFactUri,
            WebViewFallback()
        )
    }

    @NavigationDrawerType
    override fun getNavigationDrawerType() = NavigationDrawerListener.ITEM_HOME

    private val loginLauncher = registerForActivityResult(LoginContract()) { }

    private fun checkUserCredentials() {
        val settings = requireActivity().getLoginPreferences()

        val login = settings.getString("user", null)
        val password = settings.getString("pass", null)

        Log.d(LOG_TAG, "Checking user saved credentials...")
        if (login.isNullOrEmpty() || password.isNullOrEmpty()) {
            Log.d(LOG_TAG, "User is not logged in.")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val response = try {
                productsApi.signIn(login, password, "Sign-in")
            } catch (err: Throwable) {
                Log.e(LOG_TAG, "Cannot check user credentials.", err)
                null
            } ?: return@launch

            val htmlBody: String = try {
                response.body()!!.string()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "I/O Exception while checking user saved credentials.", e)
                return@launch
            }
            if (LoginActivity.isHtmlNotValid(htmlBody)) {
                Log.w(LOG_TAG, "Cannot validate login, deleting saved credentials and asking the user to log back in.")
                settings.edit {
                    putString("user", "")
                    putString("pass", "")
                }
                withContext(Dispatchers.Main) {
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.alert_dialog_warning_title)
                        .setMessage(R.string.alert_dialog_warning_msg_user)
                        .setPositiveButton(android.R.string.ok) { _, _ -> loginLauncher.launch(Unit) }
                        .show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val productCount = sharedPrefs.getInt(PRODUCT_COUNT_KEY, 0)

        refreshProductCount(productCount)
        refreshTagLine()

        (activity as? AppCompatActivity)?.supportActionBar?.let { it.title = "" }
    }

    private fun refreshProductCount(oldCount: Int) {
        Log.d(LOG_TAG, "Refreshing total product count...")

        lifecycleScope.launch {
            val resp = try {
                withContext(Dispatchers.IO) { productsApi.getTotalProductCount(getUserAgent()) }
            } catch (err: Exception) {
                Log.e(LOG_TAG, "Could not retrieve product count from server.", err)
                setProductCount(oldCount)
                return@launch
            }
            val count = resp.count.toInt()
            Log.d(LOG_TAG, "Refreshed total product count. There are $count products on the database.")

            setProductCount(count)

            sharedPrefs.edit { putInt(PRODUCT_COUNT_KEY, count) }
        }
    }

    /**
     * Set text displayed on Home based on build variant
     *
     * @param count count of total products available on the apps database
     */
    private fun setProductCount(count: Int) {
        if (count == 0) {
            binding.textHome.text = getString(R.string.txtHome)
        } else {
            binding.textHome.text = getString(R.string.txtHomeOnline, NumberFormat.getInstance().format(count))
        }
    }

    /**
     * get tag line url from OpenFoodAPIService
     */
    private fun refreshTagLine() {
        lifecycleScope.launch {
            val tagLines = try {
                withContext(Dispatchers.IO) { productsApi.getTagline(getUserAgent()) }
            } catch (err: Exception) {
                Log.w(LOG_TAG, "Could not retrieve tag-line from server.", err)
                return@launch
            }

            val appLanguage = localeManager.getLanguage()
            var isLanguageFound = false

            for (tag in tagLines) {
                if (appLanguage !in tag.language) continue
                isLanguageFound = true
                taglineURL = tag.tagLine.url
                binding.tvTagLine.text = tag.tagLine.message
                if (tag.language == appLanguage) break
            }

            if (!isLanguageFound) {
                taglineURL = tagLines.last().tagLine.url
                binding.tvTagLine.text = tagLines.last().tagLine.message
            }
            binding.tvTagLine.visibility = View.VISIBLE
        }
    }

    companion object {
        private val LOG_TAG = HomeFragment::class.simpleName!!
        private const val PRODUCT_COUNT_KEY = "productCount"
        fun newInstance() = HomeFragment().apply { arguments = Bundle() }
    }
}
