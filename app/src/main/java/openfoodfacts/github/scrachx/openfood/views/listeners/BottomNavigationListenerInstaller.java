package openfoodfacts.github.scrachx.openfood.views.listeners;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.util.Log;

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


    public static void install(BottomNavigationView bottomNavigationView, Activity activity, Context context) {
        try {
            BottomNavigationListenerInstaller.disableShiftMode(bottomNavigationView);
        } catch (Exception e) {
            Log.i(BottomNavigationListenerInstaller.class.getSimpleName(),"install",e);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new CommonBottomListener(activity,context));
    }


}
