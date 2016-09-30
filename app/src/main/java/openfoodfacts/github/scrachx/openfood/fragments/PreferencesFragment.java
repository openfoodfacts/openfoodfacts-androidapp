package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import openfoodfacts.github.scrachx.openfood.R;

public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
