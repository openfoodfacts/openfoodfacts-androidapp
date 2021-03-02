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

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentHomeBinding
import openfoodfacts.github.scrachx.openfood.features.LoginActivity.Companion.LoginContract
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import openfoodfacts.github.scrachx.openfood.utils.getUserAgent
import java.io.IOException
import java.text.NumberFormat
import java.util.*

/**
 * @see R.layout.fragment_home
 */
class HomeFragment : NavigationBaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val api by lazy { CommonApiManager.productsApi }
    private val sharedPrefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireActivity()) }

    private var taglineURL: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDailyFoodFact.setOnClickListener { openDailyFoodFacts() }
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

        api.signIn(login, password, "Sign-in")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(LOG_TAG, "Cannot check user credentials.", it) }
                .subscribe { response ->
                    val htmlBody: String = try {
                        response.body()!!.string()
                    } catch (e: IOException) {
                        Log.e(LOG_TAG, "I/O Exception while checking user saved credentials.", e)
                        return@subscribe
                    }
                    if (htmlBody.contains("Incorrect user name or password.")
                            || htmlBody.contains("See you soon!")) {
                        Log.w(LOG_TAG, "Cannot validate login, deleting saved credentials and asking the user to log back in.")
                        settings.edit {
                            putString("user", "")
                            putString("pass", "")
                        }
                        MaterialDialog.Builder(requireActivity()).let {
                            it.title(R.string.alert_dialog_warning_title)
                            it.content(R.string.alert_dialog_warning_msg_user)
                            it.positiveText(R.string.txtOk)
                            it.onPositive { _, _ -> loginLauncher.launch(Unit) }
                            it.show()
                        }

                    }
                }.addTo(disp)
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

        api.getTotalProductCount(getUserAgent())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { setProductCount(oldCount) }
                .doOnError {
                    setProductCount(oldCount)
                    Log.e(LOG_TAG, "Could not retrieve product count from server.", it)
                }
                .subscribe { resp ->
                    val totalProductCount = resp.count.toInt()
                    Log.d(LOG_TAG, "Refreshed total product count. There are $totalProductCount products on the database.")
                    setProductCount(totalProductCount)
                    sharedPrefs.edit {
                        putInt(PRODUCT_COUNT_KEY, totalProductCount)
                        apply()
                    }
                }.addTo(disp)
    }

    /**
     * Set text displayed on Home based on build variant
     *
     * @param count count of total products available on the apps database
     */
    private fun setProductCount(count: Int) {
        if (count == 0) {
            binding.textHome.setText(R.string.txtHome)
        } else {
            binding.textHome.text = resources.getString(R.string.txtHomeOnline, NumberFormat.getInstance().format(count))
        }
    }

    /**
     * get tag line url from OpenFoodAPIService
     */
    private fun refreshTagLine() {
        api.getTagline(getUserAgent())
                .subscribeOn(Schedulers.io()) // io for network
                .observeOn(AndroidSchedulers.mainThread()) // Move to main thread for UI changes
                .doOnError { Log.e(LOG_TAG, "Could not retrieve tag-line from server.", it) }
                .subscribe { languages ->
                    val localAsString = LocaleHelper.getLocaleFromContext(context).toString()
                    var isLanguageFound = false
                    var isExactLanguageFound = false
                    languages.forEach { tagLine ->
                        val languageCountry = tagLine.language
                        if (!isExactLanguageFound && (languageCountry == localAsString || languageCountry.contains(localAsString))) {
                            isExactLanguageFound = languageCountry == localAsString
                            taglineURL = tagLine.tagLine.url
                            binding.tvDailyFoodFact.text = tagLine.tagLine.message
                            isLanguageFound = true
                        }
                    }
                    if (!isLanguageFound) {
                        taglineURL = languages.last().tagLine.url
                        binding.tvDailyFoodFact.text = languages.last().tagLine.message
                    }
                    binding.tvDailyFoodFact.visibility = View.VISIBLE
                }.addTo(disp)
    }

    companion object {
        private val LOG_TAG = HomeFragment::class.simpleName!!
        private const val PRODUCT_COUNT_KEY = "productCount"
        fun newInstance() = HomeFragment().apply { arguments = Bundle() }
    }
}
