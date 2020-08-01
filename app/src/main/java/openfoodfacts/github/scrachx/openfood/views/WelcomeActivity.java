/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityWelcomeBinding;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;

/**
 * This is the Onboarding Activity shown on first-run.
 * TODO: redesign it & change the content
 * TODO: explain the 3 scores
 * TODO: be honest about offline until we implement offline scan (nobody cares about offline edit)
 * TODO: perhaps highlight ingredient analysis
 */
public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;
    private int[] layouts;
    private PrefManager prefManager;
    private boolean lastPage = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private int currentState;
    final ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            if (position == layouts.length - 1) {
                binding.btnNext.setText(getString(R.string.start));
                binding.btnSkip.setVisibility(View.GONE);
                lastPage = true;
            } else {
                binding.btnNext.setText(getString(R.string.next));
                binding.btnSkip.setVisibility(View.VISIBLE);
                lastPage = false;
            }
        }

        /**
         * If user is on the last page and tries to swipe towards the next page on right then the value of
         * positionOffset returned is always 0, on the other hand if the user tries to swipe towards the
         * previous page on the left then the value of positionOffset returned is 0.999 and decreases as the
         * user continues to swipe in the same direction. Also whenever a user tries to swipe in any
         * direction the state is changed from idle to dragging and onPageScrollStateChanged is called.
         * Therefore if the user is on the last page and the value of positionOffset is 0 and state is
         * dragging it means that the user is trying to go to the next page on right from the last page and
         * hence MainActivity is started in this case.
         */
        @Override
        public void onPageScrolled(int arg0, float positionOffset, int positionOffsetPixels) {
            if (lastPage && positionOffset == 0f && currentState == ViewPager.SCROLL_STATE_DRAGGING) {
                launchHomeScreen();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            currentState = state;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        layouts = new int[]{
            R.layout.welcome_slide1,
            R.layout.welcome_slide3,
            R.layout.welcome_slide2,
            R.layout.welcome_slide4
        };

        addBottomDots(0);
        changeStatusBarColor();

        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter(getLayoutInflater(), layouts);
        binding.viewPager.setAdapter(myViewPagerAdapter);
        binding.viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        binding.btnSkip.setOnClickListener(v -> launchHomeScreen());

        binding.btnNext.setOnClickListener(v -> {
            int nextItem = getNextItem();
            if (nextItem < layouts.length) {
                binding.viewPager.setCurrentItem(nextItem);
            } else {
                launchHomeScreen();
            }
        });
    }

    private void addBottomDots(int currentPage) {
        TextView[] dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        binding.layoutDots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            binding.layoutDots.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[currentPage].setTextColor(colorsActive[currentPage]);
        }
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onCreate(newBase));
    }

    private int getNextItem() {
        return binding.viewPager.getCurrentItem() + 1;
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    public static class MyViewPagerAdapter extends PagerAdapter {
        private final LayoutInflater layoutInflater;
        private final int[] layouts;

        public MyViewPagerAdapter(LayoutInflater layoutInflater, @StringRes int[] layouts) {
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
}
