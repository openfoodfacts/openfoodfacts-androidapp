package openfoodfacts.github.scrachx.openfood.fragments;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import openfoodfacts.github.scrachx.openfood.utils.INavigationItem;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener;


/**
 * An abstract navigation base fragment which extends base fragment and
 * implements INavigationItem.
 * @author Lobster
 * @since 06.03.18
 */
abstract class NavigationBaseFragment extends BaseFragment implements INavigationItem {

    private NavigationDrawerListener navigationDrawerListener;

    /**
     * This method attaches the fragment to its content which will be called after onCreate() method
     * Since it is overridden method super class implementation must be called.
     * @param context: Context to attach the fragment
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigationDrawerListener) {
            navigationDrawerListener = (NavigationDrawerListener) context;
        }
    }

    /**
     * This method gives subclasses a chance to initialize themselves once they know their view
     * hierarchy has been completely created.
     * This method is called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned.
     * @param view: The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState: If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navigationDrawerListener != null) {
            navigationDrawerListener.setItemSelected(getNavigationDrawerType());
        }
    }

    /**
     * This method returns the navigation drawer listener created
     * @return: navigation drawer listener
     */
    @Override
    public NavigationDrawerListener getNavigationDrawerListener() {
        return navigationDrawerListener;
    }
}
