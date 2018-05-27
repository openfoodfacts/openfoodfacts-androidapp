package org.openfoodfacts.scanner.utils;

/**
 * Created by Lobster on 06.03.18.
 */

import org.openfoodfacts.scanner.utils.NavigationDrawerListener.NavigationDrawerType;

public interface INavigationItem {

    NavigationDrawerListener getNavigationDrawerListener();

    @NavigationDrawerType
    int getNavigationDrawerType();

}
