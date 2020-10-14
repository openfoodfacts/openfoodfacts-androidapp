package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class PrefManager {
    private static final int PRIVATE_MODE = 0;
    private SharedPreferences pref;
    private static final String PREF_NAME = "open-facts-welcome";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String FIRST_TIME_LAUNCH_TIME = "FirstTimeLaunchTime";
    private static final String USER_ASKED_TO_RATE = "UserAskedToRateApp";

    public PrefManager(@NonNull Context context) {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    public boolean isFirstTimeLaunch() {
        final long actualTime = System.currentTimeMillis();
        if (pref.getLong(FIRST_TIME_LAUNCH_TIME, actualTime) == actualTime) {
            // First time launch

            // Save first launch time
            pref.edit()
                .putLong(FIRST_TIME_LAUNCH_TIME, actualTime)
                .apply();
        }

        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        pref.edit()
            .putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
            .apply();
    }

    public boolean getUserAskedToRate() {
        return pref.getBoolean(USER_ASKED_TO_RATE, false);
    }

    public void setUserAskedToRate(boolean userAskedToRate) {
        pref.edit()
            .putBoolean(USER_ASKED_TO_RATE, userAskedToRate)
            .apply();
    }

    public long getFirstTimeLaunchTime() {
        return pref.getLong(FIRST_TIME_LAUNCH_TIME, System.currentTimeMillis());
    }
}
