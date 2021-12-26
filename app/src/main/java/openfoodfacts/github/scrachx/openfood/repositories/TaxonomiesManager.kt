package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.getAppPreferences
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
    private suspend fun <T> getLastModifiedDateFromServer(taxonomy: Taxonomy<T>) = withContext(IO) {
        var lastModifiedDate: Long
        val taxoUrl = URL(BuildConfig.OFWEBSITE + taxonomy.jsonUrl)
        try {
            val httpCon = taxoUrl.openConnection() as HttpURLConnection
            lastModifiedDate = httpCon.lastModified
            httpCon.disconnect()
        } catch (e: IOException) {
            // Problem
            Log.e(LOG_TAG, "Could not get last modified date from server for taxonomy ${taxonomy::class.simpleName}.", e)
            lastModifiedDate = TAXONOMY_NO_INTERNET
        }
        Log.i(LOG_TAG, "Last modified date for taxonomy \"$taxonomy\" is $lastModifiedDate")
        return@withContext lastModifiedDate
    }

    /**
     * @param taxonomy
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *
     *  * If checkUpdate is true (or local database is empty) then force-load it from the server,
     *  * else from the local database.
     *
     * @param dao used to check if there is data saved in the local database.
     */
    suspend fun <T> getTaxonomyData(
        taxonomy: Taxonomy<T>,
        checkUpdate: Boolean,
        dao: AbstractDao<T, *>,
        taxonomiesRepository: TaxonomiesRepository
    ): List<T> = withContext(Dispatchers.Default) {
        val appPrefs = context.getAppPreferences()

        // First check if this taxonomy is to be loaded for this flavor, else return empty list
        val isTaxonomyActivated = appPrefs.getBoolean(taxonomy.getDownloadActivatePreferencesId(), false)
        if (!isTaxonomyActivated) return@withContext emptyList()

        // If the database scheme changed, this settings should be true
        val forceUpdate = appPrefs.getBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false)

        // If database is empty or we have to force update, download it
        val empty = dao.isEmpty()
        if (empty || forceUpdate) {
            // Table is empty, no need check for update, just load taxonomy
            download(taxonomy, taxonomiesRepository)
        } else if (checkUpdate) {
            // Get local last downloaded time
            val localDownloadTime = appPrefs.getLong(taxonomy.getLastDownloadTimeStampPreferenceId(), 0L)

            // We need to check for update. Test if file on server is more recent than last download.
            checkAndDownloadIfNewer(taxonomy, localDownloadTime, taxonomiesRepository)
        } else emptyList()
    }

    private suspend fun <T> download(
        taxonomy: Taxonomy<T>,
        taxonomiesRepository: TaxonomiesRepository
    ) = withContext(IO) {
        val lastMod = getLastModifiedDateFromServer(taxonomy)

        if (lastMod != TAXONOMY_NO_INTERNET) {
            taxonomy.load(taxonomiesRepository, lastMod)
                .also { logDownload(taxonomy) }
        } else emptyList()
    }

    private suspend fun <T> checkAndDownloadIfNewer(
        taxonomy: Taxonomy<T>,
        localDownloadTime: Long,
        taxonomiesRepository: TaxonomiesRepository
    ) = withContext(IO) {
        val lastModRemote = getLastModifiedDateFromServer(taxonomy)

        if (lastModRemote == 0L || lastModRemote > localDownloadTime)
            taxonomy.load(taxonomiesRepository, lastModRemote).also { logDownload(taxonomy) }
        else emptyList()
    }

    companion object {
        private const val TAXONOMY_NO_INTERNET = -9999L
        private val LOG_TAG = TaxonomiesManager::class.simpleName
    }
}