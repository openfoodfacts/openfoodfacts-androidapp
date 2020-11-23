package openfoodfacts.github.scrachx.openfood.features.product.view

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.ProductFragmentPagerAdapter
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.install
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.listeners.OnRefreshListener
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.Companion.onOptionsItemSelected
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.Companion.setupViewPager
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.ShowIngredientsAction
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients.IngredientsProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductFragment
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.FragmentUtils.requireProductState

class ProductViewFragment : Fragment(), OnRefreshListener {
    private var _binding: ActivityProductBinding? = null
    private val binding get() = _binding!!
    private var adapterResult: ProductFragmentPagerAdapter? = null
    private lateinit var client: OpenFoodAPIClient
    private val disp = CompositeDisposable()
    private lateinit var productState: ProductState

    override fun onDestroy() {
        disp.dispose()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (resources.getBoolean(R.bool.portrait_only)) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityProductBinding.inflate(inflater)
        binding.toolbar.visibility = View.GONE
        client = OpenFoodAPIClient(requireActivity())
        productState = requireProductState()

        adapterResult = setupViewPager(binding.pager)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.pager.isNestedScrollingEnabled = true
        }
        TabLayoutMediator(binding.tabs, binding.pager) { tab: TabLayout.Tab, position: Int ->
            tab.text = adapterResult!!.getPageTitle(position)
        }.attach()

        selectNavigationItem(binding.navigationBottomInclude.bottomNavigation, 0)
        install(requireActivity(), binding.navigationBottomInclude.bottomNavigation)
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val intent = Intent(activity, ProductEditActivity::class.java)
            intent.putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, productState.product)
            startActivity(intent)
        }
    }

    private fun setupViewPager(viewPager: ViewPager2): ProductFragmentPagerAdapter {
        return setupViewPager(viewPager,
                ProductFragmentPagerAdapter(requireActivity()),
                productState,
                requireActivity())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return onOptionsItemSelected(requireActivity(), item)
    }

    override fun onRefresh() {
        disp.add(client.getProductStateFull(productState.product.code)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ newState ->
                    productState = newState
                    adapterResult!!.refresh(newState)
                }) { adapterResult!!.refresh(productState) }
        )
    }

    fun bottomSheetWillGrow() {
        if (adapterResult == null || adapterResult!!.itemCount == 0) {
            return
        }
        // without this, the view can be centered vertically on initial show. we force the scroll to top !
        if (adapterResult!!.createFragment(0) is SummaryProductFragment) {
            val productFragment = adapterResult!!.createFragment(0) as SummaryProductFragment
            productFragment.resetScroll()
        }
    }

    fun showIngredientsTab(action: ShowIngredientsAction) {
        if (adapterResult == null || adapterResult!!.itemCount == 0) {
            return
        }
        for (i in 0 until adapterResult!!.itemCount) {
            val fragment = adapterResult!!.createFragment(i)
            if (fragment is IngredientsProductFragment) {
                binding.pager.currentItem = i
                if (action === ShowIngredientsAction.PERFORM_OCR) {
                    fragment.extractIngredients()
                } else if (action === ShowIngredientsAction.SEND_UPDATED) {
                    fragment.changeIngImage()
                }
                return
            }
        }
    }

    companion object {
        private const val LOGIN_ACTIVITY_REQUEST_CODE = 1

        @JvmStatic
        fun newInstance(productState: ProductState) = ProductViewFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ProductViewActivity.STATE_KEY, productState)
            }
        }
    }
}