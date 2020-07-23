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

package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.preference.PreferenceManager;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.util.Locale;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentHomeBinding;
import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.TaglineLanguageModel;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_HOME;

/**
 * @see R.layout#fragment_home
 */
public class HomeFragment extends NavigationBaseFragment {
    private FragmentHomeBinding binding;
    private ProductsAPI apiClient;
    private SharedPreferences sp;
    private String taglineURL;
    private CompositeDisposable compDisp = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvDailyFoodFact.setOnClickListener(v -> setDailyFoodFact());

        apiClient = new OpenFoodAPIClient(requireActivity()).getRawAPI();
        checkUserCredentials();
        sp = PreferenceManager.getDefaultSharedPreferences(requireActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop the call to off to get total product counts:
        compDisp.dispose();
        binding = null;
    }

    private void setDailyFoodFact() {
        // chrome custom tab init
        CustomTabsIntent customTabsIntent;
        CustomTabActivityHelper customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(new CustomTabActivityHelper.ConnectionCallback() {
            @Override
            public void onCustomTabsConnected() {

            }

            @Override
            public void onCustomTabsDisconnected() {

            }
        });
        Uri dailyFoodFactUri = Uri.parse(taglineURL);
        customTabActivityHelper.mayLaunchUrl(dailyFoodFactUri, null, null);

        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getContext(),
            customTabActivityHelper.getSession());
        CustomTabActivityHelper.openCustomTab(requireActivity(),
            customTabsIntent, dailyFoodFactUri, new WebViewFallback());
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_HOME;
    }

    private void checkUserCredentials() {
        final SharedPreferences settings = OFFApplication.getInstance().getSharedPreferences("login", 0);
        String login = settings.getString("user", "");
        String password = settings.getString("pass", "");

        if (!login.isEmpty() && !password.isEmpty()) {
            compDisp.add(apiClient.signIn(login, password, "Sign-in").subscribe(response -> {
                String htmlNoParsed = null;
                try {
                    htmlNoParsed = response.body().string();
                } catch (IOException e) {
                    Log.e(HomeFragment.class.getSimpleName(), "signin", e);
                }
                if (htmlNoParsed != null && (htmlNoParsed.contains("Incorrect user name or password.")
                    || htmlNoParsed.contains("See you soon!"))) {
                    settings.edit()
                        .putString("user", "")
                        .putString("pass", "")
                        .apply();

                    if (getActivity() != null) {
                        new MaterialDialog.Builder(getActivity())
                            .title(R.string.alert_dialog_warning_title)
                            .content(R.string.alert_dialog_warning_msg_user)
                            .positiveText(R.string.txtOk)
                            .show();
                    }
                }
            }, throwable -> Log.e(HomeFragment.class.getName(), "Unable to Sign-in", throwable)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int productCount = sp.getInt("productCount", 0);
        refreshProductCount(productCount);

        refreshTagline();

        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("");
            }
        }
    }

    private void refreshProductCount(int oldCount) {
        apiClient.getTotalProductCount(Utils.getUserAgent())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new SingleObserver<Search>() {
                @Override
                public void onSubscribe(Disposable d) {
                    compDisp.add(d);
                    if (isAdded()) {
                        updateTextHome(oldCount);
                    }
                }

                @Override
                public void onSuccess(Search search) {
                    if (isAdded()) {
                        int totalProductCount = oldCount;
                        try {
                            totalProductCount = Integer.parseInt(search.getCount());
                        } catch (NumberFormatException e) {
                            Log.w(HomeFragment.class.getSimpleName(), "can parse " + search.getCount() + " as int", e);
                        }
                        updateTextHome(totalProductCount);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt("productCount", totalProductCount);
                        editor.apply();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    if (isAdded()) {
                        updateTextHome(oldCount);
                    }
                }
            });
    }

    /**
     * Set text displayed on Home based on build variant
     *
     * @param totalProductCount count of total products available on the apps database
     */
    private void updateTextHome(int totalProductCount) {
        try {
            binding.textHome.setText(R.string.txtHome);
            if (totalProductCount != 0) {
                String txtHomeOnline = getResources().getString(R.string.txtHomeOnline);
                binding.textHome.setText(String.format(txtHomeOnline, totalProductCount));
            }
        } catch (Exception e) {
            Log.w(HomeFragment.class.getSimpleName(), "can format text for home", e);
        }
    }

    /**
     * get tag line url from OpenFoodAPIService
     */
    private void refreshTagline() {
        compDisp.add(apiClient.getTagline(Utils.getUserAgent())
            .subscribeOn(Schedulers.io()) // io for network
            .observeOn(AndroidSchedulers.mainThread()) // Move to main thread for UI changes
            .subscribe(models -> {
                final Locale locale = LocaleHelper.getLocale(getContext());
                String localAsString = locale.toString();
                boolean isLanguageFound = false;
                boolean isExactLanguageFound = false;

                for (TaglineLanguageModel tagLine : models) {
                    final String languageCountry = tagLine.getLanguage();
                    if (!isExactLanguageFound && (languageCountry.equals(localAsString) || languageCountry.contains(localAsString))) {
                        isExactLanguageFound = languageCountry.equals(localAsString);
                        taglineURL = tagLine.getTaglineModel().getUrl();
                        binding.tvDailyFoodFact.setText(tagLine.getTaglineModel().getMessage());
                        binding.tvDailyFoodFact.setVisibility(View.VISIBLE);
                        isLanguageFound = true;
                    }
                }

                if (!isLanguageFound) {
                    taglineURL = models.get(models.size() - 1).getTaglineModel().getUrl();
                    binding.tvDailyFoodFact.setText(models.get(models.size() - 1).getTaglineModel().getMessage());
                    binding.tvDailyFoodFact.setVisibility(View.VISIBLE);
                }
            }, e -> Log.w("getTagline", "cannot get tagline from server", e)));
    }
}
