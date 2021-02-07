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
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityWelcomeBinding
import openfoodfacts.github.scrachx.openfood.features.MainActivity
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.PrefManager

/**
 * This is the on boarding activity shown on first-run.
 */
/*
 * TODO: redesign it & change the content
 * TODO: explain the 3 scores
 * TODO: be honest about offline until we implement offline scan (nobody cares about offline edit)
 * TODO: perhaps highlight ingredient analysis
 */
class WelcomeActivity : AppCompatActivity() {
    private var _binding: ActivityWelcomeBinding? = null
    private val binding get() = _binding!!

    private val layouts = intArrayOf(
            R.layout.welcome_slide1,
            R.layout.welcome_slide2,
            R.layout.welcome_slide3,
            R.layout.welcome_slide4
    )
    private lateinit var prefManager: PrefManager
    private var lastPage = false

    private val viewPagerPageChangeListener = object : OnPageChangeListener {
        private var currentState = 0
        override fun onPageSelected(position: Int) {
            refreshBottomDots(position)
            if (position == layouts.lastIndex) {
                binding.btnNext.text = getString(R.string.start)
                binding.btnSkip.visibility = View.GONE
                lastPage = true
            } else {
                binding.btnNext.text = getString(R.string.next)
                binding.btnSkip.visibility = View.VISIBLE
                lastPage = false
            }
        }

        /**
         * If user is on the last page and tries to swipe towards the next page on right then the value of
         * positionOffset returned is always 0. On the other hand if the user tries to swipe towards the
         * previous page on the left then the value of positionOffset returned is 0.999 and decreases as the
         * user continues to swipe in the same direction. Also whenever a user tries to swipe in any
         * direction the state is changed from idle to dragging and onPageScrollStateChanged is called.
         * Therefore if the user is on the last page and the value of positionOffset is 0 and state is
         * dragging it means that the user is trying to go to the next page on right from the last page and
         * hence MainActivity is started in this case.
         */
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (lastPage && positionOffset == 0f && currentState == ViewPager.SCROLL_STATE_DRAGGING) {
                launchHomeScreen()
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            currentState = state
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
        prefManager = PrefManager(this)
        if (!prefManager.isFirstTimeLaunch) {
            launchHomeScreen()
            finish()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).hide(WindowInsetsCompat.Type.statusBars())

        refreshBottomDots(0)
        changeStatusBarColor()

        binding.viewPager.adapter = WelcomePageAdapter(layoutInflater, layouts)
        binding.viewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        binding.btnSkip.setOnClickListener { launchHomeScreen() }

        binding.btnNext.setOnClickListener {
            if (nextItem < layouts.size) {
                binding.viewPager.currentItem = nextItem
            } else {
                launchHomeScreen()
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun refreshBottomDots(currentPage: Int) {
        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)

        binding.layoutDots.removeAllViews()
        val dots = (1..layouts.size).map {
            TextView(this).apply {
                text = HtmlCompat.fromHtml("&#8226;", HtmlCompat.FROM_HTML_MODE_COMPACT)
                textSize = 35f
                setTextColor(colorsInactive[currentPage])
                binding.layoutDots.addView(this)
            }
        }
            dots[currentPage].setTextColor(colorsActive[currentPage])

    }

    private fun launchHomeScreen() {
        prefManager.isFirstTimeLaunch = false
        startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
        finish()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onCreate(newBase))
    }

    private val nextItem get() = binding.viewPager.currentItem + 1

    private fun changeStatusBarColor() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    companion object {
        fun start(context: Context) {
            val starter = Intent(context, WelcomeActivity::class.java)
            context.startActivity(starter)
        }
    }
}