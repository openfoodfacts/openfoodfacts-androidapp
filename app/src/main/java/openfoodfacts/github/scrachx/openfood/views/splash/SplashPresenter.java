package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.SharedPreferences;
import android.os.Handler;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import openfoodfacts.github.scrachx.openfood.AppFlavors;
import openfoodfacts.github.scrachx.openfood.jobs.LoadTaxonomiesWorker;
import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

/**
 * Created by Lobster on 03.03.18.
 */
public class SplashPresenter implements ISplashPresenter.Actions {
    private final SplashActivity activity;
    private final SharedPreferences settings;
    private final ISplashPresenter.View view;

    SplashPresenter(SharedPreferences settings, ISplashPresenter.View view, SplashActivity activity) {
        this.view = view;
        this.settings = settings;
        this.activity = activity;
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
        activateDownload(Taxonomy.ADDITIVE, AppFlavors.OFF, AppFlavors.OBF);
        activateDownload(Taxonomy.COUNTRY, AppFlavors.OFF, AppFlavors.OBF);
        activateDownload(Taxonomy.LABEL, AppFlavors.OFF, AppFlavors.OBF);
        activateDownload(Taxonomy.ALLERGEN, AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF);
        activateDownload(Taxonomy.ANALYSIS_TAGS, AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF);
        activateDownload(Taxonomy.ANALYSIS_TAG_CONFIG, AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF);

        //first run ever off this application, whatever the version
        boolean firstRun = settings.getBoolean("firstRun", true);
        if (firstRun) {
            settings.edit()
                .putBoolean("firstRun", false)
                .apply();
        }

        // The service will load server resources only if newer than already downloaded...
        OneTimeWorkRequest request = OneTimeWorkRequest.from(LoadTaxonomiesWorker.class);
        WorkManager manager = WorkManager.getInstance(activity);
        manager.enqueue(request);
        manager.getWorkInfoByIdLiveData(request.getId()).observe(activity, workInfo -> {
            if (workInfo != null && workInfo.getState() == WorkInfo.State.RUNNING) {
                view.showLoading();
            } else if (workInfo != null) {
                view.hideLoading(workInfo.getState() == WorkInfo.State.FAILED);
            }
        });

        if (firstRun) {
            new Handler().postDelayed(view::navigateToMainActivity, 6000);
        } else {
            view.navigateToMainActivity();
        }
    }
}
