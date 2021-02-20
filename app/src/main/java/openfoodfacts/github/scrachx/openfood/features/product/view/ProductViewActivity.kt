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
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.listeners.OnRefreshListener
import openfoodfacts.github.scrachx.openfood.features.product.ProductFragmentPagerAdapter
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.contributors.ContributorsFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.environment.EnvironmentProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients.IngredientsProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.IngredientsAnalysisProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.nutrition.NutritionProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.photos.ProductPhotosFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.serverattributes.ServerAttributesFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductFragment
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.applyBundle
import openfoodfacts.github.scrachx.openfood.utils.requireProductState
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ProductViewActivity : BaseActivity(), OnRefreshListener {
    private var _binding: ActivityProductBinding? = null
    private val binding get() = _binding!!

    private val disp = CompositeDisposable()

    private lateinit var client: OpenFoodAPIClient

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

        client = OpenFoodAPIClient(this)

        if (!checkIntentAction()) {
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
        if (!checkIntentAction())
            productState = requireProductState().also { adapterResult!!.refresh(it) }
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        disp.dispose()
        super.onDestroy()
    }

    /**
     * Get the product data from the barcode. This takes the barcode and retrieves the information.
     *
     * @param barcode from the URL.
     */
    private fun loadProductDataFromUrl(barcode: String) {
        client.getProductStateFull(barcode, Utils.HEADER_USER_AGENT_SCAN)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.w(this::class.simpleName, "Failed to load product data.", it)
                    finish()
                }
                .subscribe { pState ->
                    productState = pState
                    intent.putExtra(KEY_STATE, pState)
                    // Adding check on productState.getProduct() to avoid null pointer exception (happens in setViewPager()) when product not found
                    if (productState != null && productState!!.product != null) {
                        initViews()
                    } else {
                        finish()
                    }
                }.addTo(disp)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Open product editing after successful login
            Intent(this@ProductViewActivity, ProductEditActivity::class.java).apply {
                putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, productState!!.product)
                startActivity(this)
            }

        }
    }

    /**
     * Initialise the content that shows the content on the device.
     */
    private fun initViews() {
        adapterResult = setupViewPager(binding.pager)
        TabLayoutMediator(binding.tabs, binding.pager) { tab: TabLayout.Tab, position: Int ->
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
        client.openProduct(productState!!.product!!.code, this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }


    fun showIngredientsTab(action: ShowIngredientsAction) {
        if (adapterResult == null || adapterResult!!.itemCount == 0) return

        for (i in 0 until adapterResult!!.itemCount) {
            val fragment = adapterResult!!.createFragment(i)
            if (fragment is IngredientsProductFragment) {
                binding.pager.currentItem = i
                if (action == ShowIngredientsAction.PERFORM_OCR) {
                    fragment.extractIngredients()
                } else if (action == ShowIngredientsAction.SEND_UPDATED) {
                    fragment.changeIngImage()
                }
                return
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) =
            setupViewPager(viewPager, ProductFragmentPagerAdapter(this), productState!!, this)

    enum class ShowIngredientsAction {
        PERFORM_OCR, SEND_UPDATED
    }

    // to check if an product is opened through a deep link (from browser)
    private fun checkIntentAction(): Boolean {
        return when (intent.action) {
            Intent.ACTION_VIEW -> {
                val data = intent.data
                val paths = data.toString().split("/").toTypedArray() // paths[4]
                productState = ProductState()
                loadProductDataFromUrl(paths[4])
                true
            }
            else -> false
        }
    }

    companion object {
        private const val LOGIN_ACTIVITY_REQUEST_CODE = 1

        fun start(context: Context, productState: ProductState) {
            context.startActivity(Intent(context, ProductViewActivity::class.java).apply {
                putExtra(KEY_STATE, productState)
            })
        }

        /**
         * CAREFUL ! YOU MUST INSTANTIATE YOUR OWN ADAPTERRESULT BEFORE CALLING THIS METHOD
         */
        fun setupViewPager(
                viewPager: ViewPager2,
                adapter: ProductFragmentPagerAdapter,
                productState: ProductState,
                activity: Activity
        ): ProductFragmentPagerAdapter {
            val titles = activity.resources.getStringArray(R.array.nav_drawer_items_product)
            val newTitles = activity.resources.getStringArray(R.array.nav_drawer_new_items_product)

            val fBundle = Bundle().apply { putSerializable(KEY_STATE, productState) }

            adapter.add(SummaryProductFragment.newInstance(productState), titles[0])
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)

            // Add Ingredients fragment for off, obf and opff
            if (isFlavors(OFF, OBF, OPFF)) adapter.add(IngredientsProductFragment.newInstance(productState), titles[1])

            if (isFlavors(OFF, OPFF)) adapter.add(NutritionProductFragment.newInstance(productState), titles[2])

            if (isFlavors(OFF)) adapter.add(EnvironmentProductFragment().applyBundle(fBundle), titles[4])

            if (isFlavors(OFF, OPFF, OBF) && isPhotoMode(activity) || isFlavors(OPF)) {
                adapter.add(ProductPhotosFragment.newInstance(productState), newTitles[0])
            }

            if (isFlavors(OFF, OBF)) {
                adapter.add(ServerAttributesFragment.newInstance(productState), activity.getString(R.string.synthesis_tab))
            }

            if (isFlavors(OBF)) adapter.add(IngredientsAnalysisProductFragment().applyBundle(fBundle), newTitles[1])

            if (preferences.getBoolean(activity.getString(R.string.pref_contribution_tab_key), false)) {
                adapter.add(ContributorsFragment.newInstance(productState), activity.getString(R.string.contribution_tab))
            }

            viewPager.adapter = adapter
            return adapter
        }

        private fun isPhotoMode(activity: Activity) =
                PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(activity.getString(R.string.pref_show_product_photos_key), false)

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