package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PreferencesService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE)

    // First time launch
    var isFirstTimeLaunch: Boolean
        get() {
            val actualTime = System.currentTimeMillis()
            // First time launch
            if (firstTimeLaunchTime == actualTime) {
                // Save first launch time
                firstTimeLaunchTime = actualTime
            }
            return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        }
        set(isFirstTime) = sharedPreferences.edit { putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime) }

    var userAskedToRate: Boolean
        get() = sharedPreferences.getBoolean(USER_ASKED_TO_RATE, false)
        set(userAskedToRate) = sharedPreferences.edit { putBoolean(USER_ASKED_TO_RATE, userAskedToRate) }

    /**
     * The time the app has been launched for the first time in unix millis.
     *
     * If it is the first time return [System.currentTimeMillis]
     */
    var firstTimeLaunchTime: Long
        get() = sharedPreferences.getLong(FIRST_TIME_LAUNCH_TIME, System.currentTimeMillis())
        private set(value) = sharedPreferences.edit { putLong(FIRST_TIME_LAUNCH_TIME, value) }

    companion object {
        private const val PREF_NAME = "open-facts-welcome"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val FIRST_TIME_LAUNCH_TIME = "FirstTimeLaunchTime"
        private const val USER_ASKED_TO_RATE = "UserAskedToRateApp"
    }
}