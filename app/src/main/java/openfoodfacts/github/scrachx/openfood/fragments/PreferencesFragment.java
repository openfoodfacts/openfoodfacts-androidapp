package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.PreferenceFragmentCompat;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;

public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        findPreference("Locale.Helper.Selected.Language").setOnPreferenceChangeListener((preference, locale) -> {
            FragmentActivity activity = PreferencesFragment.this.getActivity();
            Configuration configuration = activity.getResources().getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                configuration.setLocale(LocaleHelper.getLocale((String) locale));

                activity.recreate();
            }
            return true;
        });
    }
}
