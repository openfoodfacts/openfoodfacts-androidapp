package openfoodfacts.github.scrachx.openfood.features.product

import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.ProductState

class ProductFragmentPagerAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragments = mutableListOf<BaseFragment>()
    private val tabsTitles = mutableListOf<String>()

    fun add(fragment: BaseFragment, tabTitle: String) {
        fragments.add(fragment)
        tabsTitles.add(tabTitle)
    }

    fun add(fragment: BaseFragment, @StringRes tabTitleRes: Int) {
        fragments.add(fragment)
        tabsTitles.add(fragmentActivity.getString(tabTitleRes))
    }

    override fun createFragment(i: Int) = fragments[i]

    override fun getItemCount() = fragments.size

    fun getPageTitle(position: Int) = tabsTitles[position]

    fun refresh(productState: ProductState) =
            fragments.filter { it.isAdded }.forEach { it.refreshView(productState) }
}