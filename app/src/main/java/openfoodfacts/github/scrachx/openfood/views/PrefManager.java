package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Date;

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private Context context;
    final int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "open-facts-welcome";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String FIRST_TIME_LAUNCH_TIME = "FirstTimeLaunchTime";
    private static final String USER_ASKED_TO_RATE = "UserAskedToRateApp";

    public PrefManager(Context context) {
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor = pref.edit();
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        if (pref.getLong(FIRST_TIME_LAUNCH_TIME, new Date(0).getTime()) == new Date(0).getTime()) {
            // First time launch
            if (editor == null) {
                editor = pref.edit();
            }
            // Save first launch time
            editor.putLong(FIRST_TIME_LAUNCH_TIME, Calendar.getInstance().getTimeInMillis());
            editor.apply();
        }

        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public boolean getUserAskedToRate() {
        return pref.getBoolean(USER_ASKED_TO_RATE, false);
    }

    public void setUserAskedToRate(boolean userAskedToRate) {
        if (editor == null) {
            if (pref == null) {
                pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            }
            editor = pref.edit();
        }
        editor.putBoolean(USER_ASKED_TO_RATE, userAskedToRate);
        editor.apply();
    }

    public long getFirstTimeLaunchTime() {
        return pref.getLong(FIRST_TIME_LAUNCH_TIME, new Date(0).getTime());
    }
}
