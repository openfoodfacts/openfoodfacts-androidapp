package openfoodfacts.github.scrachx.openfood.repositories;

import android.content.SharedPreferences;
import android.util.Log;

import org.greenrobot.greendao.AbstractDao;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.models.Additive;
import openfoodfacts.github.scrachx.openfood.models.Allergen;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTag;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.Category;
import openfoodfacts.github.scrachx.openfood.models.Country;
import openfoodfacts.github.scrachx.openfood.models.Ingredient;
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcode;
import openfoodfacts.github.scrachx.openfood.models.Label;
import openfoodfacts.github.scrachx.openfood.models.Tag;
import openfoodfacts.github.scrachx.openfood.network.services.AnalysisDataAPI;
import openfoodfacts.github.scrachx.openfood.utils.DaoUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public enum Taxonomy {
    LABEL(AnalysisDataAPI.LABELS_JSON) {
        @Override
        public Single<List<Label>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadLabels(lastModifiedDate);
        }
    },
    COUNTRY(AnalysisDataAPI.COUNTRIES_JSON) {
        @Override
        public Single<List<Country>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadCountries(lastModifiedDate);
        }
    },
    CATEGORY(AnalysisDataAPI.CATEGORIES_JSON) {
        @Override
        public Single<List<Category>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadCategories(lastModifiedDate);
        }
    },
    ADDITIVE(AnalysisDataAPI.ADDITIVES_JSON) {
        @Override
        public Single<List<Additive>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAdditives(lastModifiedDate);
        }
    },
    INGREDIENT(AnalysisDataAPI.INGREDIENTS_JSON) {
        @Override
        public Single<List<Ingredient>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadIngredients(lastModifiedDate);
        }
    },
    ALLERGEN(AnalysisDataAPI.ALLERGENS_JSON) {
        @Override
        public Single<List<Allergen>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAllergens(lastModifiedDate);
        }
    },
    ANALYSIS_TAGS(AnalysisDataAPI.ANALYSIS_TAG_JSON) {
        @Override
        public Single<List<AnalysisTag>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAnalysisTags(lastModifiedDate);
        }
    },
    ANALYSIS_TAG_CONFIG(AnalysisDataAPI.ANALYSIS_TAG_CONFIG_JSON) {
        @Override
        public Single<List<AnalysisTagConfig>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadAnalysisTagConfigs(lastModifiedDate);
        }
    },
    TAGS(AnalysisDataAPI.TAGS_JSON) {
        @Override
        public Single<List<Tag>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadTags(lastModifiedDate);
        }
    },
    INVALID_BARCODES(AnalysisDataAPI.INVALID_BARCODES_JSON) {
        @Override
        public Single<List<InvalidBarcode>> load(ProductRepository repository, long lastModifiedDate) {
            return repository.loadInvalidBarcodes(lastModifiedDate);
        }
    };
    // -1 no internet connexion.
    private static final long TAXONOMY_NO_INTERNET = -9999L;
    public final String jsonUrl;

    Taxonomy(String jsonUrl) {
        this.jsonUrl = jsonUrl;
    }

    /**
     * This function check the last modified date of the taxonomy.json file on OF server.
     *
     * @param taxonomy The lowercase taxonomy to be check
     * @return lastModifierDate     The timestamp of the last changes date of the taxonomy.json on OF server
     *     Or TAXONOMY_NO_INTERNET if there is no connexion.
     */
    private static long getLastModifiedDateFromServer(Taxonomy taxonomy) {
        long lastModifiedDate;
        try {
            String baseUrl = BuildConfig.OFWEBSITE;
            URL url = new URL(baseUrl + taxonomy.getJsonUrl());
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            lastModifiedDate = httpCon.getLastModified();
            httpCon.disconnect();
        } catch (IOException e) {
            //Problem
            Log.e(Taxonomy.class.getName(), "getLastModifiedDate", e);
            Log.i(Taxonomy.class.getName(), "getLastModifiedDate for : " + taxonomy + " end, return " + TAXONOMY_NO_INTERNET);
            return TAXONOMY_NO_INTERNET;
        }
        Log.i(Taxonomy.class.getName(), "getLastModifiedDate for : " + taxonomy + " end, return " + lastModifiedDate);
        return lastModifiedDate;
    }

    /**
     * @param repository
     * @param checkUpdate checkUpdate defines if the source of data must be refresh from server if it has been update there.
     *     *     *     If checkUpdate is true (or local database is empty) then load it from the server,
     *     *     *     else from the local database.
     * @param loadFromLocalDatabase if true the values will be loaded from local database if no update to perform from server
     * @param dao used to check if locale data is empty
     * @param <T> type of taxonomy
     */
    <T> Single<List<T>> getTaxonomyData(ProductRepository repository,
                                        boolean checkUpdate,
                                        boolean loadFromLocalDatabase,
                                        AbstractDao<T, ?> dao) {
        //First check if this taxonomy is to be loaded.
        SharedPreferences mSettings = OFFApplication.getInstance().getSharedPreferences("prefs", 0);
        boolean isDownloadActivated = mSettings.getBoolean(getDownloadActivatePreferencesId(), false);
        long lastDownloadFromSettings = mSettings.getLong(getLastDownloadTimeStampPreferenceId(), 0L);
        //if the database scheme changed, this settings should be true
        boolean forceUpdate = mSettings.getBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false);

        // TODO: better approach
        if (isDownloadActivated) {
            //Taxonomy is marked to be download
            if (DaoUtils.isDaoEmpty(dao)) {
                //Table is empty, no check for update, just load taxonomy
                long lastModifiedDate = getLastModifiedDateFromServer(this);
                if (lastModifiedDate != TAXONOMY_NO_INTERNET) {
                    return DaoUtils.logDownload(load(repository, lastModifiedDate), this);
                }
            } else if (checkUpdate) {
                //It is ask to check for update - Test if file on server is more recent than last download.
                long lastModifiedDateFromServer = getLastModifiedDateFromServer(this);
                if (forceUpdate || lastModifiedDateFromServer == 0 || lastModifiedDateFromServer > lastDownloadFromSettings) {
                    return DaoUtils.logDownload(load(repository, lastModifiedDateFromServer), this);
                }
            }
        }
        if (loadFromLocalDatabase) {
            //If we are here then just get the information from the local database
            return Single.just(dao.loadAll());
        }
        return Single.just(Collections.emptyList());
    }

    public String getJsonUrl() {
        return jsonUrl;
    }

    public String getLastDownloadTimeStampPreferenceId() {
        return "taxonomy_lastDownloadTimeStamp_" + name();
    }

    public String getDownloadActivatePreferencesId() {
        return "taxonomy_download_" + name();
    }

    public abstract <T> Single<List<T>> load(ProductRepository repository, long lastModifiedDate);
}
