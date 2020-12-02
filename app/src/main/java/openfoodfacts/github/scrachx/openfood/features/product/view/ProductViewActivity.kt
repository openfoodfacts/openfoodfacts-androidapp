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
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductBinding
import openfoodfacts.github.scrachx.openfood.features.MainActivity
import openfoodfacts.github.scrachx.openfood.features.adapters.ProductFragmentPagerAdapter
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.install
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.listeners.OnRefreshListener
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.contributors.ContributorsFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.environment.EnvironmentProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients.IngredientsProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.IngredientsAnalysisProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.nutrition.NutritionProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.photos.ProductPhotosFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductFragment
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.applyBundle
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ProductViewActivity : BaseActivity(), OnRefreshListener {
    private var _binding: ActivityProductBinding? = null
    private val binding get() = _binding!!
    private var adapterResult: ProductFragmentPagerAdapter? = null
    private lateinit var client: OpenFoodAPIClient
    private val disp = CompositeDisposable()
    private var productState: ProductState? = null
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

        productState = intent.getSerializableExtra(STATE_KEY) as ProductState?
        when {
            intent.action == Intent.ACTION_VIEW -> {
                // handle opening the app via product page url
                val data = intent.data
                val paths = data.toString().split("/").toTypedArray() // paths[4]
                productState = ProductState()
                loadProductDataFromUrl(paths[4])
            }
            productState == null -> {
                //no state-> we can't display anything. we go back to home.
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
            else -> {
                initViews()
            }
        }
    }

    /**
     * Get the product data from the barcode. This takes the barcode and retrieves the information.
     *
     * @param barcode from the URL.
     */
    private fun loadProductDataFromUrl(barcode: String) {
        disp.add(client.getProductStateFull(barcode, Utils.HEADER_USER_AGENT_SCAN)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ state: ProductState? ->
                    productState = state
                    intent.putExtra(STATE_KEY, state)
                    //Adding check on productState.getProduct() to avoid null pointer exception (happens in setViewPager()) when product not found
                    if (productState != null && productState!!.product != null) {
                        initViews()
                    } else {
                        finish()
                    }
                }) { e: Throwable? ->
                    Log.i(javaClass.simpleName, "Failed to load product data", e)
                    finish()
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Open product editing after successful login
            val intent = Intent(this@ProductViewActivity, ProductEditActivity::class.java)
            intent.putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, productState!!.product)
            startActivity(intent)
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
        selectNavigationItem(binding.navigationBottomInclude.bottomNavigation, 0)
        install(this, binding.navigationBottomInclude.bottomNavigation)
    }

    private fun setupViewPager(viewPager: ViewPager2): ProductFragmentPagerAdapter {
        return setupViewPager(viewPager, ProductFragmentPagerAdapter(this), productState!!, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return onOptionsItemSelected(this, item)
    }

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
        productState = (intent.getSerializableExtra(STATE_KEY) as ProductState).also {
            adapterResult!!.refresh(it)
        }

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        disp.dispose()
        super.onDestroy()
    }

    fun showIngredientsTab(action: ShowIngredientsAction) {
        if (adapterResult == null || adapterResult!!.itemCount == 0) {
            return
        }
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

    enum class ShowIngredientsAction {
        PERFORM_OCR, SEND_UPDATED
    }

    companion object {
        private const val LOGIN_ACTIVITY_REQUEST_CODE = 1
        const val STATE_KEY = "state"

        fun start(context: Context, productState: ProductState) {
            val starter = Intent(context, ProductViewActivity::class.java)
            starter.putExtra(STATE_KEY, productState)
            context.startActivity(starter)
        }

        /**
         * CAREFUL ! YOU MUST INSTANTIATE YOUR OWN ADAPTERRESULT BEFORE CALLING THIS METHOD
         */
        @JvmStatic
        fun setupViewPager(viewPager: ViewPager2,
                           adapter: ProductFragmentPagerAdapter,
                           productState: ProductState,
                           activity: Activity): ProductFragmentPagerAdapter {
            val menuTitles = activity.resources.getStringArray(R.array.nav_drawer_items_product)
            val newMenuTitles = activity.resources.getStringArray(R.array.nav_drawer_new_items_product)
            val fBundle = Bundle().apply {
                putSerializable(STATE_KEY, productState)
            }
            adapter.addFragment(SummaryProductFragment().applyBundle(fBundle), menuTitles[0])
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)

            // Add Ingredients fragment for off, obf and opff
            if (isFlavors(OFF, OBF, OPFF)) {
                adapter.addFragment(IngredientsProductFragment().applyBundle(fBundle), menuTitles[1])
            }
            if (isFlavors(OFF)) {
                adapter.addFragment(NutritionProductFragment().applyBundle(fBundle), menuTitles[2])
                adapter.addFragment(EnvironmentProductFragment().applyBundle(fBundle), menuTitles[4])
                if (isPhotoMode(activity)) {
                    adapter.addFragment(ProductPhotosFragment().applyBundle(fBundle), newMenuTitles[0])
                }
            } else if (isFlavors(OPFF)) {
                adapter.addFragment(NutritionProductFragment().applyBundle(fBundle), menuTitles[2])
                if (isPhotoMode(activity)) {
                    adapter.addFragment(ProductPhotosFragment().applyBundle(fBundle), newMenuTitles[0])
                }
            } else if (isFlavors(OBF)) {
                if (isPhotoMode(activity)) {
                    adapter.addFragment(ProductPhotosFragment().applyBundle(fBundle), newMenuTitles[0])
                }
                adapter.addFragment(IngredientsAnalysisProductFragment().applyBundle(fBundle), newMenuTitles[1])
            } else if (isFlavors(OPF)) {
                adapter.addFragment(ProductPhotosFragment().applyBundle(fBundle), newMenuTitles[0])
            }
            if (preferences.getBoolean("contributionTab", false)) {
                adapter.addFragment(ContributorsFragment.newInstance(productState), activity.getString(R.string.contribution_tab))
            }
            viewPager.adapter = adapter
            return adapter
        }

        private fun isPhotoMode(activity: Activity): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("photoMode", false)
        }

        @JvmStatic
        fun onOptionsItemSelected(activity: Activity, item: MenuItem): Boolean {
            return when (item.itemId) {
                android.R.id.home -> {
                    // Respond to the action bar's Up/Home button
                    activity.finish()
                    true
                }
                else -> false
            }
        }
    }
}