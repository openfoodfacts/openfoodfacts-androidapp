package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.IngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.NutritionProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.SummaryProductFragment;

public class ProductPagerAdapter extends FragmentStatePagerAdapter {

    private String[] navMenuTitles;

    public ProductPagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        navMenuTitles = c.getResources().getStringArray(R.array.nav_drawer_items_product);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case (0):
                return new SummaryProductFragment();
            case (1):
                return new IngredientsProductFragment();
            case (2):
                return new NutritionProductFragment();
            default:
                return new SummaryProductFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case (0):
                return navMenuTitles[0];
            case (1):
                return navMenuTitles[1];
            case (2):
                return navMenuTitles[2];
            default:
                return navMenuTitles[0];
        }
    }
}