package openfoodfacts.github.scrachx.openfood.utils;

/**
 * Created by Lobster on 06.03.18.
 */

import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;

public interface INavigationItem {

    NavigationDrawerListener getNavigationDrawerListener();

    @NavigationDrawerType
    int getNavigationDrawerType();

}
