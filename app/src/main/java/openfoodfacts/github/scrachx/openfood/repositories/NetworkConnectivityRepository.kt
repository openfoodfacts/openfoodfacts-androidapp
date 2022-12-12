package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import openfoodfacts.github.scrachx.openfood.utils.isNetworkAvailable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConnectivityRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** see [Context.isNetworkAvailable] */
    fun isNetworkAvailable() = context.isNetworkAvailable()
}
