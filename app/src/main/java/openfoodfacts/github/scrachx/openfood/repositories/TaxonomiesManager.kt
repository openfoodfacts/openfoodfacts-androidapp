package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import android.util.Log
import io.reactivex.Single
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.isEmpty
import openfoodfacts.github.scrachx.openfood.utils.logDownload
import org.greenrobot.greendao.AbstractDao
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object TaxonomiesManager {
    private const val TAXONOMY_NO_INTERNET = -9999L
    private val LOG_TAG = TaxonomiesManager::class.java.simpleName

    /**
     * Get the last modified date of the taxonomy.json file on the server.
     *
     * @param taxonomy The taxonomy to check
     * @return The timestamp of the last changes date of the taxonomy.json on the server
     * or [TAXONOMY_NO_INTERNET] if there is no connection to the server.
     */
    fun getLastModifiedDateFromServer(taxonomy: Taxonomy): Single<Long> = Single.fromCallable {
        var lastModifiedDate: Long
        val baseUrl = BuildConfig.OFWEBSITE
        val taxoUrl = URL(baseUrl + taxonomy.jsonUrl)
        try {
            val httpCon = taxoUrl.openConnection() as HttpURLConnection
            lastModifiedDate = httpCon.lastModified
            httpCon.disconnect()
        } catch (e: IOException) {
            //Problem
            Log.e(LOG_TAG, String.format(
                    "Could not get last modified date from server for taxonomy %s.",
                    taxonomy.name), e)
            lastModifiedDate = TAXONOMY_NO_INTERNET
        }
        Log.i(LOG_TAG, "Last modified date for taxonomy \"$taxonomy\" is $lastModifiedDate")
        return@fromCallable lastModifiedDate
    }

    /**
     * @param taxonomy
     * @param repository
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *
     *  * If checkUpdate is true (or local database is empty) then load it from the server,
     *  * else from the local database.
     *
     * @param dao used to check if locale data is empty
     * @param <T> type of taxonomy
     */
    @JvmStatic
    fun <T> getTaxonomyData(
            taxonomy: Taxonomy,
            repository: ProductRepository,
            checkUpdate: Boolean,
            dao: AbstractDao<T, *>,
            context: Context
    ): Single<List<T>> {
        // WARNING: Before "return" all code is executed on MAIN THREAD
        val mSettings = context.getSharedPreferences("prefs", 0)

        // First check if this taxonomy is to be loaded for this flavor, else return empty list
        val isTaxonomyActivated = mSettings.getBoolean(taxonomy.downloadActivatePreferencesId, false)
        if (!isTaxonomyActivated) {
            return Single.just(emptyList())
        }

        // If the database scheme changed, this settings should be true
        val forceUpdate = mSettings.getBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false)

        // If database is empty or we have to force update, download it
        if (dao.isEmpty() || forceUpdate) {
            // Table is empty, no need check for update, just load taxonomy
            return download(taxonomy, repository)
        } else if (checkUpdate) {
            // Get local last downloaded time
            val localDownloadTime = mSettings.getLong(taxonomy.lastDownloadTimeStampPreferenceId, 0L)
            // We need to check for update. Test if file on server is more recent than last download.
            return checkAndDownloadIfNewer(taxonomy, repository, localDownloadTime)
        }
        return Single.just(emptyList())
    }

    private fun <T> download(
            taxonomy: Taxonomy,
            repository: ProductRepository
    ): Single<List<T>> = getLastModifiedDateFromServer(taxonomy).flatMap { lastMod: Long ->
        if (lastMod != TAXONOMY_NO_INTERNET)
            logDownload(taxonomy.load(repository, lastMod), taxonomy)
        else Single.just(emptyList())
    }

    private fun <T> checkAndDownloadIfNewer(
            taxonomy: Taxonomy,
            repository: ProductRepository,
            localDownloadTime: Long
    ): Single<List<T>> = getLastModifiedDateFromServer(taxonomy).flatMap { lastModRemote: Long ->
        if (lastModRemote == 0L || lastModRemote > localDownloadTime)
            logDownload(taxonomy.load(repository, lastModRemote), taxonomy)
        else Single.just(emptyList())
    }
}