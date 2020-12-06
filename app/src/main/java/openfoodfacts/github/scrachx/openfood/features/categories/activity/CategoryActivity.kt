package openfoodfacts.github.scrachx.openfood.features.categories.activity

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityCategoryBinding
import openfoodfacts.github.scrachx.openfood.features.categories.fragment.CategoryListFragment
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.install
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import java.util.*

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
        setTitle(R.string.category_drawer)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // chrome custom tab for category hunger game
        binding.gameButton.setOnClickListener { openHungerGame() }

        // set fragment container view
        supportFragmentManager.beginTransaction().add(R.id.fragment, CategoryListFragment()).commitNow()
        selectNavigationItem(binding.bottomNavigationInclude.bottomNavigation, 0)
        install(this, binding.bottomNavigationInclude.bottomNavigation)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun openHungerGame() = CustomTabsIntent.Builder()
            .build()
            .launchUrl(this@CategoryActivity, Uri.parse(getString(R.string.hunger_game_url)))

    companion object {
        fun start(context: Context) = context.startActivity(Intent(context, CategoryActivity::class.java))
    }
}