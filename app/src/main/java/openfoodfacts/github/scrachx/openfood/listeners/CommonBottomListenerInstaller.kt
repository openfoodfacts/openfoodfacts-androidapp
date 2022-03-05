package openfoodfacts.github.scrachx.openfood.listeners

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


    fun BottomNavigationView.selectNavigationItem(itemId: Int) = if (itemId in NAV_ITEMS) {
        menu.findItem(itemId).isChecked = true
    } else {
        menu.getItem(0).isCheckable = false
    }

    fun BottomNavigationView.installBottomNavigation(activity: Activity) =
        setOnItemSelectedListener(CommonBottomListener(activity))

}