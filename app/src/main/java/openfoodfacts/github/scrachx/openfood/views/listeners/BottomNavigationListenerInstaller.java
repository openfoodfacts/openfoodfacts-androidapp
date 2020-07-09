package openfoodfacts.github.scrachx.openfood.views.listeners;

import android.app.Activity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashSet;
import java.util.Set;

import openfoodfacts.github.scrachx.openfood.R;

public class BottomNavigationListenerInstaller {
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

    private BottomNavigationListenerInstaller() {
    }

    public static void install(BottomNavigationView bottomNavigationView, Activity activity) {
        bottomNavigationView.setOnNavigationItemSelectedListener(new CommonBottomListener(activity));
    }


}
