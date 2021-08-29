package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit

class PrefManager(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)

    // First time launch
    var isFirstTimeLaunch: Boolean
        get() {
            val actualTime = System.currentTimeMillis()
            // First time launch
            if (pref.getLong(FIRST_TIME_LAUNCH_TIME, actualTime) == actualTime) {
                // Save first launch time
                pref.edit { putLong(FIRST_TIME_LAUNCH_TIME, actualTime) }
            }
            return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        }
        set(isFirstTime) = pref.edit { putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime) }

    var userAskedToRate: Boolean
        get() = pref.getBoolean(USER_ASKED_TO_RATE, false)
        set(userAskedToRate) = pref.edit { putBoolean(USER_ASKED_TO_RATE, userAskedToRate) }

    val firstTimeLaunchTime: Long
        get() = pref.getLong(FIRST_TIME_LAUNCH_TIME, System.currentTimeMillis())

    companion object {
        private const val PREF_NAME = "open-facts-welcome"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val FIRST_TIME_LAUNCH_TIME = "FirstTimeLaunchTime"
        private const val USER_ASKED_TO_RATE = "UserAskedToRateApp"
    }
}