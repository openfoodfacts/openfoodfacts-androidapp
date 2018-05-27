package org.openfoodfacts.scanner.fragments;

/**
 * Created by Lobster on 06.03.18.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.openfoodfacts.scanner.utils.INavigationItem;
import org.openfoodfacts.scanner.utils.NavigationDrawerListener;

abstract class NavigationBaseFragment extends BaseFragment implements INavigationItem {

    private NavigationDrawerListener navigationDrawerListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigationDrawerListener) {
            navigationDrawerListener = (NavigationDrawerListener) context;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navigationDrawerListener != null) {
            navigationDrawerListener.setItemSelected(getNavigationDrawerType());
        }
    }

    @Override
    public NavigationDrawerListener getNavigationDrawerListener() {
        return navigationDrawerListener;
    }
}
