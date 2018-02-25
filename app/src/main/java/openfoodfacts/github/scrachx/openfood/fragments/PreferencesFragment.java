package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import org.apache.commons.text.WordUtils;

import java.util.Locale;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;

public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        ListPreference languagePreference = ((ListPreference) findPreference("Locale.Helper.Selected.Language"));

        String[] localeValues = getActivity().getResources().getStringArray(R.array.lang_array);
        String[] localeLabels = new String[localeValues.length];

        for (int i = 0; i < localeValues.length; i++) {
            Locale current = LocaleHelper.getLocale(localeValues[i]);

            localeLabels[i] = String.format("%s - %s",
                    // current.getDisplayName(current), // native form
                    WordUtils.capitalize(current.getDisplayName()),
                    localeValues[i].toUpperCase(Locale.getDefault())
                    );
        }

        languagePreference.setEntries(localeLabels);
        languagePreference.setEntryValues(localeValues);

        languagePreference.setOnPreferenceChangeListener((preference, locale) -> {

            FragmentActivity activity = PreferencesFragment.this.getActivity();
            Configuration configuration = activity.getResources().getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {

                configuration.setLocale(LocaleHelper.getLocale((String) locale));

                activity.recreate();
            }
            return true;
        });

        Preference contactButton = findPreference(getString(R.string.contact_key));
        contactButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent contactIntent = new Intent(Intent.ACTION_SENDTO);
                contactIntent.setData(Uri.parse(getString(R.string.off_mail)));
                contactIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try{
                    startActivity(contactIntent);
                }catch (android.content.ActivityNotFoundException e){

                    Toast.makeText(getActivity(), R.string.email_not_found,Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }
}
