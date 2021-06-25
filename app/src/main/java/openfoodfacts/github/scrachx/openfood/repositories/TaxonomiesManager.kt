package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxSingle
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.isEmpty
import openfoodfacts.github.scrachx.openfood.utils.logDownload
import org.greenrobot.greendao.AbstractDao
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxonomiesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Get the last modified date of the taxonomy.json file on the server.
     *
     * @param taxonomy The taxonomy to check
     * @return The timestamp of the last changes date of the taxonomy.json on the server
     * or [TAXONOMY_NO_INTERNET] if there is no connection to the server.
     */
    private fun getLastModifiedDateFromServer(taxonomy: Taxonomy): Single<Long> = rxSingle(Dispatchers.IO) {
        var lastModifiedDate: Long
        val taxoUrl = URL(BuildConfig.OFWEBSITE + taxonomy.jsonUrl)
        try {
            val httpCon = taxoUrl.openConnection() as HttpURLConnection
            lastModifiedDate = httpCon.lastModified
            httpCon.disconnect()
        } catch (e: IOException) {
            // Problem
            Log.e(LOG_TAG, "Could not get last modified date from server for taxonomy ${taxonomy.name}.", e)
            lastModifiedDate = TAXONOMY_NO_INTERNET
        }
        Log.i(LOG_TAG, "Last modified date for taxonomy \"$taxonomy\" is $lastModifiedDate")
        return@rxSingle lastModifiedDate
    }

    /**
     * @param taxonomy
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *
     *  * If checkUpdate is true (or local database is empty) then load it from the server,
     *  * else from the local database.
     *
     * @param dao used to check if locale data is empty
     */
    suspend fun <T> getTaxonomyData(
        taxonomy: Taxonomy,
        checkUpdate: Boolean,
        dao: AbstractDao<T, *>,
        productRepository: ProductRepository
    ): List<T> = withContext(Dispatchers.Default) {
        val mSettings = context.getSharedPreferences("prefs", 0)

        // First check if this taxonomy is to be loaded for this flavor, else return empty list
        val isTaxonomyActivated = mSettings.getBoolean(taxonomy.downloadActivatePreferencesId, false)
        if (!isTaxonomyActivated) return@withContext emptyList()

        // If the database scheme changed, this settings should be true
        val forceUpdate = mSettings.getBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false)

        // If database is empty or we have to force update, download it
        val empty = dao.isEmpty()
        if (empty || forceUpdate) {
            // Table is empty, no need check for update, just load taxonomy
            download<T>(taxonomy, productRepository).await()
        } else if (checkUpdate) {
            // Get local last downloaded time
            val localDownloadTime = mSettings.getLong(taxonomy.lastDownloadTimeStampPreferenceId, 0L)

            // We need to check for update. Test if file on server is more recent than last download.
            checkAndDownloadIfNewer<T>(taxonomy, localDownloadTime, productRepository).await()
        } else emptyList()
    }

    private fun <T> download(
        taxonomy: Taxonomy,
        productRepository: ProductRepository
    ): Single<List<T>> = getLastModifiedDateFromServer(taxonomy).flatMap { lastMod ->
        if (lastMod != TAXONOMY_NO_INTERNET)
            logDownload(taxonomy.load(productRepository, lastMod), taxonomy)
        else Single.just(emptyList())
    }

    private fun <T> checkAndDownloadIfNewer(
        taxonomy: Taxonomy,
        localDownloadTime: Long,
        productRepository: ProductRepository
    ): Single<List<T>> = getLastModifiedDateFromServer(taxonomy).flatMap { lastModRemote ->
        if (lastModRemote == 0L || lastModRemote > localDownloadTime)
            logDownload(taxonomy.load(productRepository, lastModRemote), taxonomy)
        else Single.just(emptyList())
    }

    companion object {
        private const val TAXONOMY_NO_INTERNET = -9999L
        private val LOG_TAG = TaxonomiesManager::class.simpleName
    }
}