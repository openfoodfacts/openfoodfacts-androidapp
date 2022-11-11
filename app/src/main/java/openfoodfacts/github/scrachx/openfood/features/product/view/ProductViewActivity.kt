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
package openfoodfacts.github.scrachx.openfood.features.product.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.AppFlavor.Companion.isFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavor.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavor.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavor.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavor.OPFF
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductBinding
import openfoodfacts.github.scrachx.openfood.features.product.ProductFragmentPagerAdapter
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.ShowIngredientsAction.PERFORM_OCR
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.ShowIngredientsAction.SEND_UPDATED
import openfoodfacts.github.scrachx.openfood.features.product.view.contributors.ContributorsFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.environment.EnvironmentProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients.IngredientsProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.IngredientsAnalysisProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.nutrition.NutritionProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.photos.ProductPhotosFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.serverattributes.ServerAttributesFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductFragment
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.listeners.OnRefreshListener
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.Intent
import openfoodfacts.github.scrachx.openfood.utils.requireProductState
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

@AndroidEntryPoint
class ProductViewActivity : BaseActivity(), IProductView, OnRefreshListener {
    private var _binding: ActivityProductBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var client: ProductRepository

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var productViewActivityStarter: ProductViewActivityStarter

    private var productState: ProductState? = null
    private var adapterResult: ProductFragmentPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.app_name_long)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (!checkActionFromIntent()) {
            productState = requireProductState()
            initViews()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        // To check if the activity is resumed and new product is opened through a deep link. If not, then only we can call requireProductState()
        if (!checkActionFromIntent())
            productState = requireProductState().also { adapterResult!!.refresh(it) }
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    /**
     * Get the product data from the barcode. This takes the barcode and retrieves the information.
     *
     * @param barcode from the URL.
     */
    private suspend fun fetchProduct(barcode: String) = try {
        client.getProductStateFull(barcode)
    } catch (err: Exception) {
        Log.w(this::class.simpleName, "Failed to load product $barcode.", err)
        finish()
        null
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Open product editing after successful login
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            ProductEditActivity.start(this, productState!!.product!!)
        }
    }

    /**
     * Initialise the content that shows the content on the device.
     */
    private fun initViews() {
        adapterResult = setupViewPager(binding.pager)
        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text = adapterResult!!.getPageTitle(position)
        }.attach()
        binding.navigationBottomInclude.bottomNavigation.selectNavigationItem(0)
        binding.navigationBottomInclude.bottomNavigation.installBottomNavigation(this)
    }

    override fun onOptionsItemSelected(item: MenuItem) = onOptionsItemSelected(this, item)

    @Subscribe
    fun onEventBusProductNeedsRefreshEvent(event: ProductNeedsRefreshEvent) {
        if (event.barcode == productState!!.product!!.code) {
            onRefresh()
        }
    }

    override fun onRefresh() {
        productViewActivityStarter.openProduct(productState!!.product!!.code, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }


    override fun showIngredientsTab(action: ShowIngredientsAction) {
        if (adapterResult == null || adapterResult!!.itemCount == 0) return

        for (i in 0 until adapterResult!!.itemCount) {
            val fragment = adapterResult!!.createFragment(i)

            if (fragment is IngredientsProductFragment) {
                binding.pager.currentItem = i
                when (action) {
                    PERFORM_OCR -> fragment.extractIngredients()
                    SEND_UPDATED -> fragment.changeIngImage()
                }
                return
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) = setupViewPager(
        viewPager,
        productState!!,
        this
    )

    enum class ShowIngredientsAction {
        PERFORM_OCR, SEND_UPDATED
    }

    /** To check if an product is opened through a deep link */
    private fun checkActionFromIntent() = when (intent.action) {
        Intent.ACTION_VIEW -> {
            val data = intent.data
            val barcode = data.toString().split("/")[4]

            // Fetch product from server, then initialize views
            lifecycleScope.launch {
                val pState = fetchProduct(barcode) ?: return@launch

                productState = pState
                intent.putExtra(KEY_STATE, pState)
                // Adding check on productState.getProduct() to avoid NPE
                // (happens in setViewPager()) when product not found
                if (productState != null && productState!!.product != null) initViews()
                else finish()
            }

            true
        }
        else -> false
    }

    companion object {
        private const val LOGIN_ACTIVITY_REQUEST_CODE = 1

        fun start(context: Context, productState: ProductState) {
            context.startActivity(Intent<ProductViewActivity>(context).apply {
                putExtra(KEY_STATE, productState)
            })
        }

        /**
         * CAREFUL ! YOU MUST INSTANTIATE YOUR OWN ADAPTERRESULT BEFORE CALLING THIS METHOD
         */
        fun setupViewPager(
            viewPager: ViewPager2,
            productState: ProductState,
            activity: FragmentActivity,
        ): ProductFragmentPagerAdapter {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val adapter = ProductFragmentPagerAdapter(activity)

            val titles = activity.resources.getStringArray(R.array.nav_drawer_items_product)
            val newTitles = activity.resources.getStringArray(R.array.nav_drawer_new_items_product)

            adapter += SummaryProductFragment.newInstance(productState) to titles[0]

            // Add Ingredients fragment for off, obf and opff
            if (isFlavors(OFF, OBF, OPFF)) {
                adapter += IngredientsProductFragment.newInstance(productState) to titles[1]
            }

            if (isFlavors(OFF, OPFF)) {
                adapter += NutritionProductFragment.newInstance(productState) to titles[2]
            }

            if (isFlavors(OFF)) {
                adapter += EnvironmentProductFragment.newInstance(productState) to titles[4]
            }

            if (isFlavors(OPF) || (isFlavors(OFF, OPFF, OBF) && isPhotoMode(sharedPreferences, activity))) {
                adapter += ProductPhotosFragment.newInstance(productState) to newTitles[0]
            }

            if (isFlavors(OFF, OBF)) {
                adapter += ServerAttributesFragment.newInstance(productState) to activity.getString(R.string.synthesis_tab)
            }

            if (isFlavors(OBF)) {
                adapter += IngredientsAnalysisProductFragment.newInstance(productState) to newTitles[1]
            }

            val contributionTabEnabled =
                sharedPreferences.getBoolean(activity.getString(R.string.pref_contribution_tab_key), false)
            if (contributionTabEnabled) {
                adapter += ContributorsFragment.newInstance(productState) to activity.getString(R.string.contribution_tab)
            }

            viewPager.adapter = adapter
            return adapter
        }

        private fun isPhotoMode(sharedPreferences: SharedPreferences, context: Context) =
            sharedPreferences.getBoolean(context.getString(R.string.pref_show_product_photos_key), false)

        fun onOptionsItemSelected(activity: Activity, item: MenuItem) = when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                activity.finish()
                true
            }
            else -> false
        }
    }
}
