package openfoodfacts.github.scrachx.openfood.features.product

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.ProductState

class ProductFragmentPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragments = mutableListOf<BaseFragment>()
    private val tabsTitles = mutableListOf<String>()

    fun addFragment(fragment: BaseFragment, tabTitle: String) {
        fragments.add(fragment)
        tabsTitles.add(tabTitle)
    }

    override fun createFragment(i: Int) = fragments[i]

    override fun getItemCount() = fragments.size

    fun getPageTitle(position: Int) = tabsTitles[position]

    fun refresh(productState: ProductState) {
        fragments.filter { it.isAdded }.forEach { it.refreshView(productState) }
    }
}