package openfoodfacts.github.scrachx.openfood.utils

import androidx.annotation.IntDef

/**
 * Created by Lobster on 06.03.18.
 */
interface NavigationDrawerListener {
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
            ITEM_HOME,
            ITEM_SEARCH_BY_CODE,
            ITEM_CATEGORIES,
            ITEM_SCAN,
            ITEM_COMPARE,
            ITEM_HISTORY,
            ITEM_LOGIN,
            ITEM_ALERT,
            ITEM_PREFERENCES,
            ITEM_ABOUT,
            ITEM_CONTRIBUTE,
            ITEM_OBF,
            ITEM_ADVANCED_SEARCH,
            ITEM_MY_CONTRIBUTIONS,
            ITEM_LOGOUT,
            ITEM_INCOMPLETE_PRODUCTS,
            ITEM_ADDITIVES
    )
    annotation class NavigationDrawerType

    fun setItemSelected(@NavigationDrawerType type: Int)

    companion object {
        const val ITEM_USER = 0
        const val ITEM_HOME = 1
        const val ITEM_SEARCH_BY_CODE = 2
        const val ITEM_CATEGORIES = 3
        const val ITEM_SCAN = 4
        const val ITEM_COMPARE = 5
        const val ITEM_HISTORY = 6
        const val ITEM_LOGIN = 7
        const val ITEM_ALERT = 8
        const val ITEM_PREFERENCES = 9
        const val ITEM_ABOUT = 11
        const val ITEM_CONTRIBUTE = 12
        const val ITEM_OBF = 13
        const val ITEM_ADVANCED_SEARCH = 14
        const val ITEM_MY_CONTRIBUTIONS = 15
        const val ITEM_LOGOUT = 16
        const val ITEM_MANAGE_ACCOUNT = 17
        const val ITEM_INCOMPLETE_PRODUCTS = 18
        const val ITEM_ADDITIVES = 19
        const val ITEM_YOUR_LISTS = 20
    }
}