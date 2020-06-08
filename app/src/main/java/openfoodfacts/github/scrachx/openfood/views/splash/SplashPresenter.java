package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.LoadTaxonomiesService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

/**
 * Created by Lobster on 03.03.18.
 */
public class SplashPresenter implements ISplashPresenter.Actions {
    private final Context context;
    private final SharedPreferences settings;
    private final ISplashPresenter.View view;

    SplashPresenter(SharedPreferences settings, ISplashPresenter.View view, Context context) {
        this.view = view;
        this.settings = settings;
        this.context = context;
    }

    private void activateDownload(Taxonomy taxonomy) {
        settings.edit().putBoolean(taxonomy.getDownloadActivatePreferencesId(), true).apply();
    }

    private void activateDownload(Taxonomy taxonomy, String... flavors) {
        if (Utils.isFlavor(flavors)) {
            settings.edit().putBoolean(taxonomy.getDownloadActivatePreferencesId(), true).apply();
        }
    }

    @Override
    public void refreshData() {
        activateDownload(Taxonomy.CATEGORY);
        activateDownload(Taxonomy.TAGS);
        activateDownload(Taxonomy.INVALID_BARCODES);
        activateDownload(Taxonomy.ADDITIVE, OFFApplication.OFF, OFFApplication.OBF);
        activateDownload(Taxonomy.COUNTRY, OFFApplication.OFF, OFFApplication.OBF);
        activateDownload(Taxonomy.LABEL, OFFApplication.OFF, OFFApplication.OBF);
        activateDownload(Taxonomy.ALLERGEN, OFFApplication.OFF, OFFApplication.OBF, OFFApplication.OPFF);
        activateDownload(Taxonomy.ANALYSIS_TAGS, OFFApplication.OFF, OFFApplication.OBF, OFFApplication.OPFF);
        activateDownload(Taxonomy.ANALYSIS_TAG_CONFIG, OFFApplication.OFF, OFFApplication.OBF, OFFApplication.OPFF);

        //first run ever off this application, whatever the version
        boolean firstRun = settings.getBoolean("firstRun", true);
        if (firstRun) {
            settings.edit()
                .putBoolean("firstRun", false)
                .apply();
        }
        final ResultReceiver receiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == LoadTaxonomiesService.STATUS_RUNNING) {
                    view.showLoading();
                } else {
                    view.hideLoading(resultCode == LoadTaxonomiesService.STATUS_ERROR);
                }
            }
        };

        // The service will load server resources only if newer than already downloaded...
        Intent intent = new Intent(context, LoadTaxonomiesService.class);
        intent.putExtra(LoadTaxonomiesService.RECEIVER_KEY, receiver);
        context.startService(intent);

        if (firstRun) {
            new Handler().postDelayed(view::navigateToMainActivity, 6000);
        } else {
            view.navigateToMainActivity();
        }
    }
}
