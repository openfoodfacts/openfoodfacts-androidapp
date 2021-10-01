package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConnectivityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Check if the user is connected to a network. This can be any network.
     * @return `true` if connected or connecting, `false` otherwise
     */
    @Suppress("DEPRECATION")
    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val capability = cm.getNetworkCapabilities(cm.activeNetwork)
            capability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        } else {
            cm.activeNetworkInfo?.isConnectedOrConnecting ?: false
        }
    }
}
