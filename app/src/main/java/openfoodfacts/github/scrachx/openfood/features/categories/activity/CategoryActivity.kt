package openfoodfacts.github.scrachx.openfood.features.categories.activity

import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.commitNow
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityCategoryBinding
import openfoodfacts.github.scrachx.openfood.features.categories.fragment.CategoryListFragment
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.utils.Intent

@AndroidEntryPoint
class CategoryActivity : BaseActivity() {
    private var _binding: ActivityCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.category_drawer)

        // chrome custom tab for category hunger game
        binding.gameButton.setOnClickListener { openHungerGame() }

        // set fragment container view
        supportFragmentManager.commitNow { add(R.id.fragment, CategoryListFragment()) }

        binding.bottomNavigationInclude.bottomNavigation.selectNavigationItem(0)
        binding.bottomNavigationInclude.bottomNavigation.installBottomNavigation(this)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun openHungerGame() = CustomTabsIntent.Builder()
            .build()
            .launchUrl(this, Uri.parse(getString(R.string.hunger_game_url)))

    companion object {
        fun start(context: Context) = context.startActivity(Intent<CategoryActivity>(context))
    }
}