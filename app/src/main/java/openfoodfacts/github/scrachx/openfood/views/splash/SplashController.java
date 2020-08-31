/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.SharedPreferences;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import openfoodfacts.github.scrachx.openfood.AppFlavors;
import openfoodfacts.github.scrachx.openfood.jobs.LoadTaxonomiesWorker;
import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy;

/**
 * Created by Lobster on 03.03.18.
 */
public class SplashController implements ISplashActivity.Controller {
    private final SplashActivity activity;
    private final SharedPreferences settings;
    private final ISplashActivity.View view;

    SplashController(SharedPreferences settings, ISplashActivity.View view, SplashActivity activity) {
        this.view = view;
        this.settings = settings;
        this.activity = activity;
    }

    private void activateDownload(@NonNull Taxonomy taxonomy) {
        settings.edit().putBoolean(taxonomy.getDownloadActivatePreferencesId(), true).apply();
    }

    private void activateDownload(@NonNull Taxonomy taxonomy, String... flavors) {
        if (AppFlavors.isFlavors(flavors)) {
            activateDownload(taxonomy);
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
        // The 6000 delay is to show one loop of the multilingual logo. I asked for it ~ Pierre
        if (firstRun) {
            new Handler().postDelayed(view::navigateToMainActivity, 6000);
        } else {
            view.navigateToMainActivity();
        }
    }
}
