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
package openfoodfacts.github.scrachx.openfood.features.welcome

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.analytics.SentryAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.ActivityWelcomeBinding
import openfoodfacts.github.scrachx.openfood.features.MainActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.utils.PreferencesService
import openfoodfacts.github.scrachx.openfood.utils.darken
import openfoodfacts.github.scrachx.openfood.utils.lighten
import javax.inject.Inject

/**
 * This is the on boarding activity shown on first-run.
 */
/*
 * TODO: redesign it & change the content
 * TODO: explain the 3 scores
 * TODO: be honest about offline until we implement offline scan (nobody cares about offline edit)
 * TODO: perhaps highlight ingredient analysis
 */
@AndroidEntryPoint
class WelcomeActivity : BaseActivity() {
    private var _binding: ActivityWelcomeBinding? = null
    private val binding get() = _binding!!

    private val screens = WelcomeScreen.values()

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sentryAnalytics: SentryAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var prefManager: PreferencesService

    private val viewPagerPageChangeListener = object : OnPageChangeListener {

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
        override fun onPageScrollStateChanged(state: Int) = Unit

        override fun onPageSelected(position: Int) {
            refreshBottomBar(position)

            when (WelcomeScreen[position]) {
                WelcomeScreen.ANALYTICS -> {
                    binding.btnNext.setText(R.string.preference_analytics_bottom_sheet_grant_button)
                    binding.btnSkip.setText(R.string.preference_analytics_bottom_sheet_decline_button)

                    binding.btnNext.setTextColor(ResourcesCompat.getColor(resources, android.R.color.black, theme))
                    binding.btnSkip.setTextColor(ResourcesCompat.getColor(resources, android.R.color.black, theme))

                    binding.btnNext.setOnClickListener { saveThenLaunchHome(true) }
                    binding.btnSkip.setOnClickListener { saveThenLaunchHome(false) }
                }
                else -> {
                    binding.btnNext.setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, theme))
                    binding.btnSkip.setTextColor(ResourcesCompat.getColor(resources, android.R.color.white, theme))

                    binding.btnNext.setText(R.string.next)
                    binding.btnSkip.setText(R.string.skip)
                    setOnClicks()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        _binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!prefManager.isFirstTimeLaunch) {
            launchHome()
            finish()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).hide(WindowInsetsCompat.Type.statusBars())

        refreshBottomBar(0)
        changeStatusBarColor()

        binding.viewPager.adapter = WelcomePageAdapter(layoutInflater)
        binding.viewPager.addOnPageChangeListener(viewPagerPageChangeListener)

        setOnClicks()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun setOnClicks() {
        binding.btnSkip.setOnClickListener { binding.viewPager.currentItem = screens.size - 1 }
        binding.btnNext.setOnClickListener { binding.viewPager.currentItem = nextItem }
    }

    private fun saveThenLaunchHome(analyticsEnabled: Boolean) {
        saveAnalyticsReportingPref(analyticsEnabled)
        matomoAnalytics.setEnabled(analyticsEnabled)
        launchHome()
    }

    private fun saveAnalyticsReportingPref(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(getString(R.string.pref_analytics_reporting_key), enabled)
            putBoolean(sentryAnalytics.prefKey, enabled)
        }
    }

    private fun launchHome() {
        prefManager.isFirstTimeLaunch = false
        MainActivity.start(this)
        finish()
    }

    private fun refreshBottomBar(currentPage: Int) {

        binding.layoutDots.removeAllViews()
        val dots = (0..screens.lastIndex).map {
            TextView(this).apply {
                text = "\u2022"
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35F)
                setTextColor(WelcomeScreen[currentPage].color.lighten(0.85f))
                binding.layoutDots.addView(this)
            }
        }
        dots[currentPage].setTextColor(WelcomeScreen[currentPage].color.darken(0.1f))
    }

    private val nextItem get() = binding.viewPager.currentItem + 1

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, WelcomeActivity::class.java))
    }
}
