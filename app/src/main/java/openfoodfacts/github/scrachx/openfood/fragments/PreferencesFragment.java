package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.AdditiveDao;
import openfoodfacts.github.scrachx.openfood.utils.INavigationItem;
import openfoodfacts.github.scrachx.openfood.utils.JsonUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_PREFERENCES;

public class PreferencesFragment extends PreferenceFragmentCompat implements INavigationItem {

    AdditiveDao mAdditiveDao;
    private SharedPreferences settings;
    private NavigationDrawerListener navigationDrawerListener;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        setHasOptionsMenu(true);

        ListPreference languagePreference = ((ListPreference) findPreference("Locale.Helper.Selected.Language"));

        settings = getActivity().getSharedPreferences("prefs", 0);
        mAdditiveDao = Utils.getAppDaoSession(getActivity()).getAdditiveDao();

        String[] localeValues = getActivity().getResources().getStringArray(R.array.languages_array);
        String[] localeLabels = new String[localeValues.length];

        for (int i = 0; i < localeValues.length; i++) {
            Locale current = LocaleHelper.getLocale(localeValues[i]);

            if (current != null) {
                localeLabels[i] = WordUtils.capitalize(current.getDisplayName(current));
            }
        }

        languagePreference.setEntries(localeLabels);
        languagePreference.setEntryValues(localeValues);

        languagePreference.setOnPreferenceChangeListener((preference, locale) -> {

            FragmentActivity activity = PreferencesFragment.this.getActivity();
            Configuration configuration = activity.getResources().getConfiguration();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                configuration.setLocale(LocaleHelper.getLocale((String) locale));
                new GetAdditives().execute();
            }
            return true;
        });

        Preference contactButton = findPreference("contact_team");
        contactButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent contactIntent = new Intent(Intent.ACTION_SENDTO);
                contactIntent.setData(Uri.parse(getString(R.string.off_mail)));
                contactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(contactIntent);
                } catch (android.content.ActivityNotFoundException e) {

                    Toast.makeText(getActivity(), R.string.email_not_found, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });


        Preference faqbutton = findPreference("FAQ");
        faqbutton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                customTabsIntent.intent.putExtra("android.intent.extra.REFERRER",Uri.parse("android-app://"+getContext().getPackageName()));
                CustomTabActivityHelper.openCustomTab(getActivity(), customTabsIntent, Uri.parse(getString(R.string.faq_url)), new WebViewFallback());
                return true;
            }
        });

        Preference terms = findPreference("Terms");
        terms.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                customTabsIntent.intent.putExtra("android.intent.extra.REFERRER",Uri.parse("android-app://"+getContext().getPackageName()));
                CustomTabActivityHelper.openCustomTab(getActivity(), customTabsIntent, Uri.parse(getString(R.string.terms_url)), new WebViewFallback());
                return true;
            }
        });

        Preference langHelp = findPreference("local_translate_help");
        langHelp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                customTabsIntent.intent.putExtra("android.intent.extra.REFERRER",Uri.parse("android-app://"+getContext().getPackageName()));
                CustomTabActivityHelper.openCustomTab(getActivity(), customTabsIntent, Uri.parse(getString(R.string.translate_url)), new WebViewFallback());

                return true;
            }
        });

        ListPreference imageUploadPref = ((ListPreference) findPreference("ImageUpload"));
        String[] values = getActivity().getResources().getStringArray(R.array.upload_image);
        imageUploadPref.setEntries(values);
        imageUploadPref.setEntryValues(values);
        imageUploadPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                settings.edit().putString("imageUpload", (String)newValue).apply();
                return true;
            }
        });
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


    public void onResume() {

        super.onResume();

        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.action_preferences));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }
}
