package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {
    private val pref: SharedPreferences
    // First time launch

    // Save first launch time
    var isFirstTimeLaunch: Boolean
        get() {
            val actualTime = System.currentTimeMillis()
            if (pref.getLong(FIRST_TIME_LAUNCH_TIME, actualTime) == actualTime) {
                // First time launch

                // Save first launch time
                pref.edit()
                        .putLong(FIRST_TIME_LAUNCH_TIME, actualTime)
                        .apply()
            }
            return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        }
        set(isFirstTime) {
            pref.edit()
                    .putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
                    .apply()
        }
    var userAskedToRate: Boolean
        get() = pref.getBoolean(USER_ASKED_TO_RATE, false)
        set(userAskedToRate) {
            pref.edit()
                    .putBoolean(USER_ASKED_TO_RATE, userAskedToRate)
                    .apply()
        }
    val firstTimeLaunchTime: Long
        get() = pref.getLong(FIRST_TIME_LAUNCH_TIME, System.currentTimeMillis())

    companion object {
        private const val PRIVATE_MODE = 0
        private const val PREF_NAME = "open-facts-welcome"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val FIRST_TIME_LAUNCH_TIME = "FirstTimeLaunchTime"
        private const val USER_ASKED_TO_RATE = "UserAskedToRateApp"
    }

    init {
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    }
}