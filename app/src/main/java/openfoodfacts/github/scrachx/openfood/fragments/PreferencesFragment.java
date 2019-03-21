package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;

import net.steamcrafted.loadtoast.LoadToast;

import org.apache.commons.text.WordUtils;
import org.greenrobot.greendao.async.AsyncSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.CountryNameDao;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.utils.INavigationItem;
import openfoodfacts.github.scrachx.openfood.utils.JsonUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.SearchSuggestionProvider;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_PREFERENCES;

public class PreferencesFragment extends PreferenceFragmentCompat implements INavigationItem {

    AdditiveDao mAdditiveDao;
    private SharedPreferences settings;
    private NavigationDrawerListener navigationDrawerListener;
    public static String USER_COUNTRY_PREFERENCE_KEY = "user_country";

   Context context;


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        setHasOptionsMenu(true);
        context=getContext();


        ListPreference languagePreference = ((ListPreference) findPreference("Locale.Helper.Selected.Language"));

        settings = getActivity().getSharedPreferences("prefs", 0);
        mAdditiveDao = Utils.getAppDaoSession(getActivity()).getAdditiveDao();

        String[] localeValues = getActivity().getResources().getStringArray(R.array.languages_array);
        String[] localeLabels = new String[localeValues.length];
        List<String> finalLocalValues = new ArrayList<>();
        List<String> finalLocalLabels = new ArrayList<>();

        for (int i = 0; i < localeValues.length; i++) {
            Locale current = LocaleHelper.getLocale(localeValues[i]);

            if (current != null) {
                localeLabels[i] = WordUtils.capitalize(current.getDisplayName(current));
                finalLocalLabels.add(localeLabels[i]);
                finalLocalValues.add(localeValues[i]);
            }
        }

        languagePreference.setEntries(finalLocalLabels.toArray(new String[finalLocalLabels.size()]));
        languagePreference.setEntryValues(finalLocalValues.toArray(new String[finalLocalValues.size()]));

        languagePreference.setOnPreferenceChangeListener((preference, locale) -> {

            FragmentActivity activity = PreferencesFragment.this.getActivity();
            Configuration configuration = activity.getResources().getConfiguration();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(LocaleHelper.getLocale((String) locale));
                new GetAdditives().execute();
            }
            return true;
        });

        Preference deleteSearchHistoryButton = findPreference("deleteSearchHistoryPreference");
        deleteSearchHistoryButton.setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(),getString(R.string.preference_delete_search_history),Toast.LENGTH_SHORT).show();
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getContext(), SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.clearHistory();
            return true;
        });

        ListPreference countryPreference = ((ListPreference) findPreference(USER_COUNTRY_PREFERENCE_KEY));
        List<String> countryLabels = new ArrayList<>();

        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        AsyncSession asyncSessionCountries = daoSession.startAsyncSession();
        CountryNameDao countryNameDao = daoSession.getCountryNameDao();

        asyncSessionCountries.setListenerMainThread(operation -> {
            @SuppressWarnings("unchecked")
            List<CountryName> countryNames = (List<CountryName>) operation.getResult();
            for (int i = 0; i < countryNames.size(); i++) {
                countryLabels.add(countryNames.get(i).getName());
            }
            countryPreference.setEntries(countryLabels.toArray(new String[countryLabels.size()]));
            countryPreference.setEntryValues(countryLabels.toArray(new String[countryLabels.size()]));
        });

        asyncSessionCountries.queryList(countryNameDao.queryBuilder()
                .where(CountryNameDao.Properties.LanguageCode.eq(LocaleHelper.getLanguage(getActivity())))
                .orderAsc(CountryNameDao.Properties.Name).build());

        countryPreference.setOnPreferenceChangeListener(((preference, newValue) -> {
            if (preference instanceof ListPreference) {
               if (preference.getKey().equals(USER_COUNTRY_PREFERENCE_KEY)) {
                   String country = (String) newValue;
                   SharedPreferences.Editor editor = settings.edit();
                   editor.putString(preference.getKey(), country);
                   editor.apply();
               }
            }
            return true;
        }));

        Preference contactButton = findPreference("contact_team");
        contactButton.setOnPreferenceClickListener(preference -> {

            Intent contactIntent = new Intent(Intent.ACTION_SENDTO);
            contactIntent.setData(Uri.parse(getString(R.string.off_mail)));
            contactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(contactIntent);
            } catch (android.content.ActivityNotFoundException e) {

                Toast.makeText(getActivity(), R.string.email_not_found, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        Preference rateus=findPreference("RateUs");
        rateus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    String installer = context.getPackageManager()
                            .getInstallerPackageName(context.getPackageName());
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + installer)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
                }
                return true;
            }
        });


        Preference faqbutton = findPreference("FAQ");
        faqbutton.setOnPreferenceClickListener(preference -> {

            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.intent.putExtra("android.intent.extra.REFERRER", Uri.parse("android-app://" + getContext().getPackageName()));
            CustomTabActivityHelper.openCustomTab(getActivity(), customTabsIntent, Uri.parse(getString(R.string.faq_url)), new WebViewFallback());
            return true;
        });

        Preference terms = findPreference("Terms");
        terms.setOnPreferenceClickListener(preference -> {

            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.intent.putExtra("android.intent.extra.REFERRER", Uri.parse("android-app://" + getContext().getPackageName()));
            CustomTabActivityHelper.openCustomTab(getActivity(), customTabsIntent, Uri.parse(getString(R.string.terms_url)), new WebViewFallback());
            return true;
        });

        Preference langHelp = findPreference("local_translate_help");
        langHelp.setOnPreferenceClickListener(preference -> {

            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.intent.putExtra("android.intent.extra.REFERRER", Uri.parse("android-app://" + getContext().getPackageName()));
            CustomTabActivityHelper.openCustomTab(getActivity(), customTabsIntent, Uri.parse(getString(R.string.translate_url)), new
                    WebViewFallback());

            return true;
        });

        ListPreference energyUnitPreference = (ListPreference) findPreference("energyUnitPreference");
        String energyUnits[] = getActivity().getResources().getStringArray(R.array.energy_units);;
        energyUnitPreference.setEntries(energyUnits);
        energyUnitPreference.setEntryValues(energyUnits);
        energyUnitPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            settings.edit().putString("energyUnitPreference", (String) newValue).apply();
            Toast.makeText(getActivity(),getString(R.string.changes_saved),Toast.LENGTH_SHORT).show();
            return true;
        });

        ListPreference volumeUnitPreference = (ListPreference) findPreference("volumeUnitPreference");
        String volumeUnits[] = getActivity().getResources().getStringArray(R.array.volume_units);
        volumeUnitPreference.setEntries(volumeUnits);
        volumeUnitPreference.setEntryValues(volumeUnits);
        volumeUnitPreference.setOnPreferenceChangeListener(((preference, newValue) -> {
            settings.edit().putString("volumeUnitPreference", (String) newValue).apply();
            Toast.makeText(getActivity(),getString(R.string.changes_saved),Toast.LENGTH_SHORT).show();
            return true;
        }));

        ListPreference imageUploadPref = ((ListPreference) findPreference("ImageUpload"));
        String[] values = getActivity().getResources().getStringArray(R.array.upload_image);
        imageUploadPref.setEntries(values);
        imageUploadPref.setEntryValues(values);
        imageUploadPref.setOnPreferenceChangeListener((preference, newValue) -> {
            settings.edit().putString("imageUpload", (String) newValue).apply();
            Toast.makeText(getActivity(),getString(R.string.changes_saved),Toast.LENGTH_SHORT).show();
            return true;
        });


        CheckBoxPreference photoPreference = (CheckBoxPreference) findPreference("photoMode");
        if (BuildConfig.FLAVOR.equals("opf")) {
            photoPreference.setVisible(false);
        }

        /*
            Preference to show version name
         */
        Preference versionPref = (Preference) findPreference("Version");
        versionPref.setEnabled(false);
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            versionPref.setSummary(getString(R.string.version_string) + " " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public NavigationDrawerListener getNavigationDrawerListener() {
        if (navigationDrawerListener == null && getActivity() instanceof NavigationDrawerListener) {
            navigationDrawerListener = (NavigationDrawerListener) getActivity();
        }

        return navigationDrawerListener;
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_PREFERENCES;
    }

    public void onResume() {

        super.onResume();

        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.action_preferences));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private class GetAdditives extends AsyncTask<Void, Integer, Boolean> {

        private static final String ADDITIVE_IMPORT = "ADDITIVE_IMPORT";
        private LoadToast lt = new LoadToast(getActivity());


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lt.setText(getActivity().getString(R.string.toast_retrieving));
            lt.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.blue));
            lt.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            lt.show();
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            boolean result = true;

            String additivesFile = "additives_" + LocaleHelper.getLanguage(getActivity()) + ".json";
            InputStream is = null;
            try {
                is = getActivity().getAssets().open(additivesFile);
                List<Additive> frenchAdditives = JsonUtils.readFor(new TypeReference<List<Additive>>() {
                })
                        .readValue(is);
                mAdditiveDao.insertOrReplaceInTx(frenchAdditives);
            } catch (IOException e) {
                result = false;
                Log.e(ADDITIVE_IMPORT, "Unable to import additives from " + additivesFile);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e1) {
                        Log.e(ADDITIVE_IMPORT, "Unable to close the inputstream of " + additivesFile);
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            super.onPostExecute(result);
            lt.hide();
            getActivity().recreate();
        }

    }
}
