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
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.fasterxml.jackson.databind.JsonNode
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentHomeBinding
import openfoodfacts.github.scrachx.openfood.features.LoginActivity.LoginContract
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment
import openfoodfacts.github.scrachx.openfood.models.TaglineLanguageModel
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import openfoodfacts.github.scrachx.openfood.utils.Utils
import retrofit2.Response
import java.io.IOException
import java.text.NumberFormat
import java.util.*

/**
 * @see R.layout.fragment_home
 */
class HomeFragment : NavigationBaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var api: ProductsAPI
    private val compDisp = CompositeDisposable()
    private var taglineURL: String? = null
    private var sharedPrefs: SharedPreferences? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        api = OpenFoodAPIClient(requireActivity()).rawAPI
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        binding.tvDailyFoodFact.setOnClickListener { openDailyFoodFacts() }
        checkUserCredentials()
    }

    override fun onDestroy() {
        // Stop the call to server to get total product count and tagline
        compDisp.dispose()
        _binding = null
        super.onDestroy()
    }

    private fun openDailyFoodFacts() {
        // chrome custom tab init
        val customTabsIntent: CustomTabsIntent
        val customTabActivityHelper = CustomTabActivityHelper()
        customTabActivityHelper.connectionCallback = object : CustomTabActivityHelper.ConnectionCallback {
            override fun onCustomTabsConnected() {}
            override fun onCustomTabsDisconnected() {}
        }
        val dailyFoodFactUri = Uri.parse(taglineURL)
        customTabActivityHelper.mayLaunchUrl(dailyFoodFactUri, null, null)
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireActivity(),
                customTabActivityHelper.session)
        CustomTabActivityHelper.openCustomTab(requireActivity(),
                customTabsIntent, dailyFoodFactUri, WebViewFallback())
    }

    @NavigationDrawerType
    override fun getNavigationDrawerType(): Int = NavigationDrawerListener.ITEM_HOME


    private val loginLauncher = registerForActivityResult(LoginContract()) { }

    private fun checkUserCredentials() {
        val settings = OFFApplication.instance.getSharedPreferences("login", 0)
        val login = settings.getString("user", "")
        val password = settings.getString("pass", "")
        Log.d(LOG_TAG, "Checking user saved credentials...")
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) {
            Log.d(LOG_TAG, "User is not logged in.")
            return
        }
        compDisp.add(api.signIn(login, password, "Sign-in")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response: Response<ResponseBody> ->
                    val htmlNoParsed: String = try {
                        response.body()!!.string()
                    } catch (e: IOException) {
                        Log.e(LOG_TAG, "I/O Exception while checking user saved credentials.", e)
                        return@subscribe
                    }
                    if (htmlNoParsed.contains("Incorrect user name or password.")
                            || htmlNoParsed.contains("See you soon!")) {
                        Log.w(LOG_TAG, "Cannot validate login, deleting saved credentials and asking the user to log back in.")
                        with(settings.edit()) {
                            putString("user", "")
                            putString("pass", "")
                            apply()
                        }
                        with(MaterialDialog.Builder(requireActivity())) {
                            title(R.string.alert_dialog_warning_title)
                            content(R.string.alert_dialog_warning_msg_user)
                            positiveText(R.string.txtOk)
                            onPositive { _, _ -> loginLauncher.launch(Unit) }
                            show()
                        }

                    }
                })
                { throwable -> Log.e(HomeFragment::class.java.name, "Cannot check user credentials.", throwable) })
    }

    override fun onResume() {
        super.onResume()
        val productCount = sharedPrefs!!.getInt("productCount", 0)
        refreshProductCount(productCount)
        refreshTagline()
        if (activity is AppCompatActivity) {
            val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
            if (actionBar != null) {
                actionBar.title = ""
            }
        }
    }

    private fun refreshProductCount(oldCount: Int) {
        Log.d(LOG_TAG, "Refreshing total product count...")
        compDisp.add(api.getTotalProductCount(Utils.getUserAgent())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { setProductCount(oldCount) }
                .subscribe({ json: JsonNode ->
                    val totalProductCount = json["count"].asInt(0)
                    Log.d(LOG_TAG, String.format(
                            "Refreshed total product count. There are %d products on the database.",
                            totalProductCount
                    ))
                    setProductCount(totalProductCount)
                    val editor = sharedPrefs!!.edit()
                    editor.putInt("productCount", totalProductCount)
                    editor.apply()
                }
                ) { e: Throwable? ->
                    setProductCount(oldCount)
                    Log.e(LOG_TAG, "Could not retrieve product count from server.", e)
                })
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
    private fun refreshTagline() {
        compDisp.add(api.getTagline(Utils.getUserAgent())
                .subscribeOn(Schedulers.io()) // io for network
                .observeOn(AndroidSchedulers.mainThread()) // Move to main thread for UI changes
                .subscribe({ models: ArrayList<TaglineLanguageModel> ->
                    val locale = LocaleHelper.getLocale(context)
                    val localAsString = locale.toString()
                    var isLanguageFound = false
                    var isExactLanguageFound = false
                    for (tagLine in models) {
                        val languageCountry = tagLine.language
                        if (!isExactLanguageFound && (languageCountry == localAsString || languageCountry.contains(localAsString))) {
                            isExactLanguageFound = languageCountry == localAsString
                            taglineURL = tagLine.taglineModel.url
                            binding.tvDailyFoodFact.text = tagLine.taglineModel.message
                            binding.tvDailyFoodFact.visibility = View.VISIBLE
                            isLanguageFound = true
                        }
                    }
                    if (!isLanguageFound) {
                        taglineURL = models[models.size - 1].taglineModel.url
                        binding.tvDailyFoodFact.text = models[models.size - 1].taglineModel.message
                        binding.tvDailyFoodFact.visibility = View.VISIBLE
                    }
                }) { e -> Log.e(LOG_TAG, "Could not retrieve tag-line from server.", e) })
    }

    companion object {
        private val LOG_TAG = HomeFragment::class.java.simpleName

        @JvmStatic
        fun newInstance() = HomeFragment().apply {
            arguments = Bundle()
        }
    }
}