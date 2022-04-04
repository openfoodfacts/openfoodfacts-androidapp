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
package openfoodfacts.github.scrachx.openfood.features.home

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import logcat.logcat
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentHomeBinding
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity.Companion.LoginContract
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment
import openfoodfacts.github.scrachx.openfood.models.TagLine
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import java.text.NumberFormat
import javax.inject.Inject

/**
 * @see R.layout.fragment_home
 */
@AndroidEntryPoint
class HomeFragment : NavigationBaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    private var taglineURL: String? = null

    private val numberFormat by lazy { NumberFormat.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTagLine.setOnClickListener { openDailyFoodFacts() }

        viewModel.signInState.observe(viewLifecycleOwner, ::checkLoginValidity)
        viewModel.productCount.observe(viewLifecycleOwner, ::setProductCount)
        viewModel.tagline.observe(viewLifecycleOwner, ::setTagline)

        checkUserCredentials()
    }

    private fun setTagline(tagline: TagLine) {

        taglineURL = tagline.url
        binding.tvTagLine.text = tagline.message

        binding.tvTagLine.visibility = View.VISIBLE
    }


    override fun onDestroyView() {
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

        val login = requireContext().getLoginUsername()
        val password = requireContext().getLoginPassword()

        logcat { "Checking user saved credentials..." }
        if (login.isNullOrEmpty() || password.isNullOrEmpty()) {
            logcat { "User is not logged in." }
            return
        }

        viewModel.signIn(login, password)
    }

    private fun checkLoginValidity(valid: Boolean) {
        if (valid) return

        requireContext().setLogin("", "")

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.alert_dialog_warning_title)
            .setMessage(R.string.alert_dialog_warning_msg_user)
            .setPositiveButton(android.R.string.ok) { _, _ -> loginLauncher.launch(Unit) }
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshProductCount()
        viewModel.refreshTagline()

        (activity as? AppCompatActivity)?.supportActionBar?.let { it.title = "" }
    }

    /**
     * Set text displayed on Home based on build variant
     *
     * @param count count of total products available on the apps database
     */
    private fun setProductCount(count: Int) {
        binding.textHome.text =
            if (count == 0) getString(R.string.txtHome)
            else {
                getString(R.string.txtHomeOnline, numberFormat.format(count))
            }
    }

    companion object {
        fun newInstance() = HomeFragment().apply { arguments = Bundle() }
    }
}