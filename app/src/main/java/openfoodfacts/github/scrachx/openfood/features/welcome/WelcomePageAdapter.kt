package openfoodfacts.github.scrachx.openfood.features.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.viewpager.widget.PagerAdapter

class WelcomePageAdapter(
        private val layoutInflater: LayoutInflater,
        @param:StringRes private val layouts: IntArray
) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(layouts[position], container, false)
        container.addView(view)
        return view
    }

    override fun getCount() = layouts.size

    override fun isViewFromObject(view: View, obj: Any) = view === obj

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) = container.removeView(item as View)
}