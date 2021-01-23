package openfoodfacts.github.scrachx.openfood.features.product.view

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.listeners.OnRefreshListener
import openfoodfacts.github.scrachx.openfood.features.product.ProductFragmentPagerAdapter
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.Companion.onOptionsItemSelected
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.Companion.setupViewPager
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.ShowIngredientsAction
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.ShowIngredientsAction.PERFORM_OCR
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.ShowIngredientsAction.SEND_UPDATED
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients.IngredientsProductFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductFragment
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.requireProductState

class ProductViewFragment : Fragment(), OnRefreshListener {
    private var _binding: ActivityProductBinding? = null
    private val binding get() = _binding!!

    private val disp = CompositeDisposable()

    private lateinit var client: OpenFoodAPIClient
    private lateinit var adapterResult: ProductFragmentPagerAdapter
    private lateinit var productState: ProductState

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (resources.getBoolean(R.bool.portrait_only)) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        client = OpenFoodAPIClient(requireActivity())
        productState = requireProductState()

        binding.toolbar.visibility = View.GONE

        adapterResult = setupViewPager(binding.pager)
        ViewCompat.setNestedScrollingEnabled(binding.pager, true)

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text = adapterResult.getPageTitle(position)
        }.attach()

        binding.navigationBottomInclude.bottomNavigation.selectNavigationItem(0)
        binding.navigationBottomInclude.bottomNavigation.installBottomNavigation(requireActivity())
    }

    override fun onDestroyView() {
        disp.dispose()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val intent = Intent(activity, ProductEditActivity::class.java)
            intent.putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, productState.product)
            startActivity(intent)
        }
    }

    private fun setupViewPager(viewPager: ViewPager2) = setupViewPager(
            viewPager,
            ProductFragmentPagerAdapter(requireActivity()),
            productState,
            requireActivity(),
    )

    override fun onOptionsItemSelected(item: MenuItem) = onOptionsItemSelected(requireActivity(), item)

    override fun onRefresh() {
        client.getProductStateFull(productState.product!!.code)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { adapterResult.refresh(productState) }
                .subscribe { newState ->
                    productState = newState
                    adapterResult.refresh(newState)
                }.addTo(disp)

    }

    fun bottomSheetWillGrow() {
        if (adapterResult.itemCount == 0) return
        // without this, the view can be centered vertically on initial show. we force the scroll to top !
        if (adapterResult.createFragment(0) is SummaryProductFragment) {
            (adapterResult.createFragment(0) as SummaryProductFragment).resetScroll()
        }
    }

    fun showIngredientsTab(action: ShowIngredientsAction) {
        if (adapterResult.itemCount == 0) return
        (0 until adapterResult.itemCount).forEach { i ->
            val fragment = adapterResult.createFragment(i)
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

    companion object {
        private const val LOGIN_ACTIVITY_REQUEST_CODE = 1

        @JvmStatic
        fun newInstance(productState: ProductState) = ProductViewFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_STATE, productState)
            }
        }
    }
}