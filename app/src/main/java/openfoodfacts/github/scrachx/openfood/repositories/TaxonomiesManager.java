package openfoodfacts.github.scrachx.openfood.repositories;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import org.greenrobot.greendao.AbstractDao;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.utils.DaoUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public class TaxonomiesManager {
    public static final long TAXONOMY_NO_INTERNET = -9999L;
    private static final String LOG_TAG = TaxonomiesManager.class.getSimpleName();

    private TaxonomiesManager() {
    }

    /**
     * Get the last modified date of the taxonomy.json file on the server.
     *
     * @param taxonomy The taxonomy to check
     * @return The timestamp of the last changes date of the taxonomy.json on the server
     *     or {@link #TAXONOMY_NO_INTERNET} if there is no connection to the server.
     */
    static Single<Long> getLastModifiedDateFromServer(@NonNull Taxonomy taxonomy) {
        return Single.fromCallable(() -> {
            long lastModifiedDate;
            final String baseUrl = BuildConfig.OFWEBSITE;
            final URL taxoUrl = new URL(baseUrl + taxonomy.getJsonUrl());
            try {
                HttpURLConnection httpCon = (HttpURLConnection) taxoUrl.openConnection();
                lastModifiedDate = httpCon.getLastModified();
                httpCon.disconnect();
            } catch (IOException e) {
                //Problem
                Log.e(LOG_TAG, String.format(
                    "Could not get last modified date from server for taxonomy %s.",
                    taxonomy.name()), e);
                lastModifiedDate = TAXONOMY_NO_INTERNET;
            }
            Log.i(LOG_TAG, String.format("Last modified date for taxonomy \"%s\" is %d", taxonomy, lastModifiedDate));
            return lastModifiedDate;
        });
    }

    /**
     * @param taxonomy
     * @param repository
     * @param checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *     <ul>
     *         <li>If checkUpdate is true (or local database is empty) then load it from the server,</li>
     *         <li>else from the local database.</li>
     *     </ul>
     * @param dao used to check if locale data is empty
     * @param <T> type of taxonomy
     */
    static <T> Single<List<T>> getTaxonomyData(@NonNull Taxonomy taxonomy, ProductRepository repository,
                                               boolean checkUpdate,
                                               AbstractDao<T, ?> dao) {
        // WARNING: Before "return" all code is executed on MAIN THREAD
        SharedPreferences mSettings = OFFApplication.getInstance().getSharedPreferences("prefs", 0);

        // First check if this taxonomy is to be loaded for this flavor, else return empty list
        boolean isTaxonomyActivated = mSettings.getBoolean(taxonomy.getDownloadActivatePreferencesId(), false);
        if (!isTaxonomyActivated) {
            return Single.just(Collections.emptyList());
        }

        // If the database scheme changed, this settings should be true
        boolean forceUpdate = mSettings.getBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false);

        // If database is empty or we have to force update, download it
        if (DaoUtils.isDaoEmpty(dao) || forceUpdate) {
            // Table is empty, no need check for update, just load taxonomy
            return download(taxonomy, repository);
        } else if (checkUpdate) {
            // Get local last downloaded time
            long localDownloadTime = mSettings.getLong(taxonomy.getLastDownloadTimeStampPreferenceId(), 0L);
            // We need to check for update. Test if file on server is more recent than last download.
            return checkAndDownloadIfNewer(taxonomy, repository, localDownloadTime);
        }
        return Single.just(Collections.emptyList());
    }

    private static <T> Single<List<T>> download(Taxonomy taxonomy, ProductRepository repository) {
        return getLastModifiedDateFromServer(taxonomy).flatMap(lastModifiedDate -> {
            if (lastModifiedDate != TAXONOMY_NO_INTERNET) {
                return DaoUtils.logDownload(taxonomy.load(repository, lastModifiedDate), taxonomy);
            }
            return Single.just(Collections.emptyList());
        });
    }

    private static <T> Single<List<T>> checkAndDownloadIfNewer(Taxonomy taxonomy, ProductRepository repository, long localDownloadTime) {
        return getLastModifiedDateFromServer(taxonomy).flatMap(lastModifiedDateFromServer -> {
            if (lastModifiedDateFromServer == 0 || lastModifiedDateFromServer > localDownloadTime) {
                return DaoUtils.logDownload(taxonomy.load(repository, lastModifiedDateFromServer), taxonomy);
            }
            return Single.just(Collections.emptyList());
        });
    }
}
