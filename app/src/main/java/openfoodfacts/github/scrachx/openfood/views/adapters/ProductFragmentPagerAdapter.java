package openfoodfacts.github.scrachx.openfood.views.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.models.State;

public class ProductFragmentPagerAdapter extends FragmentStateAdapter {
    private final List<BaseFragment> fragments;
    private final List<String> tabsTitles;

    public ProductFragmentPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.fragments = new ArrayList<>();
        this.tabsTitles = new ArrayList<>();
    }

    public void addFragment(BaseFragment fragment, String tabTitle) {
        this.fragments.add(fragment);
        this.tabsTitles.add(tabTitle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int i) {
        return fragments.get(i);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public CharSequence getPageTitle(int position) {
        return tabsTitles.get(position);
    }

    public void refresh(State state) {
        for (BaseFragment f : fragments) {
            if (f.isAdded()) {
                f.refreshView(state);
            }
        }
    }
}
