package openfoodfacts.github.scrachx.openfood.features.shared

import android.content.Context

/**
 * A custom [NavigationBaseFragment] that can be notified of navigation drawer events.
 * The host (= Activity) must contains a Navigation Drawer and implements [NavigationDrawerHost]
 */
abstract class NavigationWithDrawerBaseFragment : NavigationBaseFragment(), OnNavigationDrawerStatusChanged {

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is NavigationDrawerHost) {
            context.setOnDrawerStatusChanged(this)
        }
    }

    override fun onDetach() {
        if (requireContext() is NavigationDrawerHost) {
            (context as NavigationDrawerHost).setOnDrawerStatusChanged(null)
        }

        super.onDetach()
    }

    override fun onDrawerOpened() {
        // No implementation by default
    }

    override fun onDrawerClosed() {
        // No implementation by default
    }
}

/**
 * Interface to implement for an Activity with a NavigationDrawer
 */
interface NavigationDrawerHost {
    fun setOnDrawerStatusChanged(onDrawerStatusChanged: OnNavigationDrawerStatusChanged?)
}

/**
 * Interface to notify when the status of a NavigationDrawer has changed (opened / closed)
 */
interface OnNavigationDrawerStatusChanged {
    fun onDrawerOpened()
    fun onDrawerClosed()
}