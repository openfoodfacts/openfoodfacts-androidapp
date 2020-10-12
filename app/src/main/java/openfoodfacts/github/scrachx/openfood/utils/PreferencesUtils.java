package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import openfoodfacts.github.scrachx.openfood.features.PreferencesFragment;

public final class PreferencesUtils {
    private PreferencesUtils() {
        // Utility class
    }

    @NonNull
    public static <T extends Preference> T requirePreference(@NonNull PreferencesFragment preferencesFragment, @NonNull String key) {
        final T preference = preferencesFragment.findPreference(key);
        if (preference == null) {
            throw new IllegalStateException(key + " preference does not exist.");
        }
        return preference;
    }

    @NonNull
    public static <T extends Preference> T requirePreference(@NonNull PreferenceScreen screen, @NonNull String key) {
        final T preference = screen.findPreference(key);
        if (preference == null) {
            throw new IllegalStateException(key + " preference does not exist.");
        }
        return preference;
    }
}
