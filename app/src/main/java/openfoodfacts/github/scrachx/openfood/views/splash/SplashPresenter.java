package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.LoadTaxonomiesService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

/**
 * Created by Lobster on 03.03.18.
 */
public class SplashPresenter implements ISplashPresenter.Actions {
    /**
     * Mutiplied by 6*30* to reduce the issue. TODO: fix https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/1616
     */
    private final Long REFRESH_PERIOD = 6 * 30 * 24 * 60 * 60 * 1000L;
    private ISplashPresenter.View view;
    private SharedPreferences settings;
    private Context context;

    public SplashPresenter(SharedPreferences settings, ISplashPresenter.View view, Context context) {
        this.view = view;
        this.settings = settings;
        this.context = context;
    }

    private void activateDownload(Taxonomy taxonomy) {
        settings.edit().putBoolean(taxonomy.getDownloadActivatePreferencesId(), true).apply();
    }

    private void activateDownload(Taxonomy taxonomy, String... flavors) {
        if (OFFApplication.isFlavor(flavors)) {
            settings.edit().putBoolean(taxonomy.getDownloadActivatePreferencesId(), true).apply();
        }
    }

    @Override
    public void refreshData() {
        activateDownload(Taxonomy.CATEGORY);
        activateDownload(Taxonomy.ADDITIVE,OFFApplication.OFF, OFFApplication.OBF);
        activateDownload(Taxonomy.COUNTRY,OFFApplication.OFF, OFFApplication.OBF);
        activateDownload(Taxonomy.LABEL,OFFApplication.OFF, OFFApplication.OBF);
        activateDownload(Taxonomy.ALLERGEN,OFFApplication.OFF);
        activateDownload(Taxonomy.ANALYSIS_TAGS,OFFApplication.OFF);
        activateDownload(Taxonomy.ANALYSIS_TAG_CONFIG,OFFApplication.OFF);

        //first run ever off this application, whatever the version
        boolean firstRun = settings.getBoolean("firstRun", true);
        if (firstRun) {
            settings.edit()
                .putBoolean("firstRun", false)
                .apply();
        }
        if (isNeedToRefresh()) { //true if data was refreshed more than 1 day ago
            Intent intent = new Intent(context, LoadTaxonomiesService.class);
            context.startService(intent);
        }
        if (firstRun) {
            new Handler().postDelayed(() -> view.navigateToMainActivity(), 6000);
        } else {
            view.navigateToMainActivity();
        }
    }

    /*
     * This method checks if data was refreshed more than 1 day ago
     */
    private Boolean isNeedToRefresh() {
        return System.currentTimeMillis() - settings.getLong(Utils.LAST_REFRESH_DATE, 0) > REFRESH_PERIOD;
    }
}
