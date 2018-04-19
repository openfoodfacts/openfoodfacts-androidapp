package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.State;

public class ProductFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<String> navMenuTitles;
    private List<BaseFragment> fragments;

    public ProductFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<>();
        this.navMenuTitles = new ArrayList<>();
    }

    public void addFragment(BaseFragment fragment, String title) {
        this.fragments.add(fragment);
        this.navMenuTitles.add(title);
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return navMenuTitles.get(position);
    }

    public void refresh(State state) {
        for (BaseFragment f : fragments) {
            if (f.isAdded()) {
                f.refreshView(state);
            }
        }
    }
}