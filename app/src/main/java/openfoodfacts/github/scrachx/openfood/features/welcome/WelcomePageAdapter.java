package openfoodfacts.github.scrachx.openfood.features.welcome;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.viewpager.widget.PagerAdapter;

public class WelcomePageAdapter extends PagerAdapter {
    private final LayoutInflater layoutInflater;
    private final int[] layouts;

    public WelcomePageAdapter(LayoutInflater layoutInflater, @StringRes int[] layouts) {
        this.layoutInflater = layoutInflater;
        this.layouts = layouts;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = layoutInflater.inflate(layouts[position], container, false);
        container.addView(view);

        return view;
    }

    @Override
    public int getCount() {
        return layouts.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return view == obj;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        View view = (View) object;
        container.removeView(view);
    }
}
