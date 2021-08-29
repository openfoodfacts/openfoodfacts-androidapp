package openfoodfacts.github.scrachx.openfood.features.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class WelcomePageAdapter(
        private val layoutInflater: LayoutInflater,
) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layout = WelcomeScreen.values()[position].layout
        val view = layoutInflater.inflate(layout, container, false)
        container.addView(view)
        return view
    }

    override fun getCount() = WelcomeScreen.values().size

    override fun isViewFromObject(view: View, obj: Any) = view === obj

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) = container.removeView(item as View)
}