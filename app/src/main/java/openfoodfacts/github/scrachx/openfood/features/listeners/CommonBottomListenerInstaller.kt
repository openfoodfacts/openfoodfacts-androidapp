package openfoodfacts.github.scrachx.openfood.features.listeners

import android.app.Activity
import com.google.android.material.bottomnavigation.BottomNavigationView
import openfoodfacts.github.scrachx.openfood.R

object CommonBottomListenerInstaller {
    // We use LinkedHashSet to retain insertion order
    private val NAV_ITEMS = linkedSetOf(
            R.id.scan_bottom_nav,
            R.id.compare_products,
            R.id.home_page,
            R.id.history_bottom_nav,
            R.id.my_lists
    )


    @JvmStatic
    fun selectNavigationItem(bottomNavigationView: BottomNavigationView, itemId: Int) {
        if (itemId in NAV_ITEMS) {
            bottomNavigationView.menu.findItem(itemId).isChecked = true
        } else {
            bottomNavigationView.menu.getItem(0).isCheckable = false
        }
    }

    @JvmStatic
    fun install(activity: Activity, bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener(CommonBottomListener(activity))
    }

}