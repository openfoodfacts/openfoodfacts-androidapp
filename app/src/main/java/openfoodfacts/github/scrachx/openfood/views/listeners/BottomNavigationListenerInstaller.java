package openfoodfacts.github.scrachx.openfood.views.listeners;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

import openfoodfacts.github.scrachx.openfood.R;

public class BottomNavigationListenerInstaller {
    /*
        public method in order to disable shift mode in the bottom navigation bar
        Can also be resolved by using : app:labelVisibilityMode="labeled" on xml fragment
        if using the library com.android.support:design.28.0.0-alpha1
         */
    @SuppressLint("RestrictedApi")
    private static void disableShiftMode(BottomNavigationView view) {
//        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
//        try {
//            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
//            shiftingMode.setAccessible(true);
//            shiftingMode.setBoolean(menuView, false);
//            shiftingMode.setAccessible(false);
//            for (int i = 0; i < menuView.getChildCount(); i++) {
//                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
//                item.setShiftingMode(false);
//
//                item.setChecked(item.getItemData().isChecked());
//            }
//        } catch (NoSuchFieldException e) {
//            Log.i(BottomNavigationListenerInstaller.class.getSimpleName(),"disableShiftMode",e);
//
//        } catch (IllegalAccessException e) {
//            Log.i(BottomNavigationListenerInstaller.class.getSimpleName(),"disableShiftMode",e);
//        }
    }

    public static void selectNavigationItem(BottomNavigationView bottomNavigationView, int itemId) {
        Set<Integer> navItems = new HashSet<>();
        navItems.add(R.id.scan_bottom_nav);
        navItems.add(R.id.compare_products);
        navItems.add(R.id.home_page);
        navItems.add(R.id.history_bottom_nav);
        navItems.add(R.id.my_lists);

        if (navItems.contains(itemId)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1){
                bottomNavigationView.setSelectedItemId(itemId);
            } else{
                bottomNavigationView.getMenu().findItem(itemId).setChecked(true);
            }
        } else {
            bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        }
    }

    public static void install(BottomNavigationView bottomNavigationView, Activity activity, Context context) {
        try {
            BottomNavigationListenerInstaller.disableShiftMode(bottomNavigationView);
        } catch (Exception e) {
            Log.i(BottomNavigationListenerInstaller.class.getSimpleName(),"install",e);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new CommonBottomListener(activity, context));
    }


}
