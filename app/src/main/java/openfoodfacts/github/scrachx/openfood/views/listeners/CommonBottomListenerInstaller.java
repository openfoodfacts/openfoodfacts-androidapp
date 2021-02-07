package openfoodfacts.github.scrachx.openfood.views.listeners;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.LinkedHashSet;
import java.util.Set;

import openfoodfacts.github.scrachx.openfood.R;

public class CommonBottomListenerInstaller {
    // We use LinkedHashSet to retain insertion order
    protected static final Set<Integer> NAV_ITEMS = new LinkedHashSet<>();

    static {
        NAV_ITEMS.add(R.id.scan_bottom_nav);
        NAV_ITEMS.add(R.id.compare_products);
        NAV_ITEMS.add(R.id.home_page);
        NAV_ITEMS.add(R.id.history_bottom_nav);
        NAV_ITEMS.add(R.id.my_lists);
    }

    private CommonBottomListenerInstaller() {
    }

    public static void selectNavigationItem(BottomNavigationView bottomNavigationView, int itemId) {
        if (NAV_ITEMS.contains(itemId)) {
            bottomNavigationView.getMenu().findItem(itemId).setChecked(true);
        } else {
            bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        }
    }

    public static void install(@NonNull Activity activity, @NonNull BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnNavigationItemSelectedListener(new CommonBottomListener(activity));
    }
}
