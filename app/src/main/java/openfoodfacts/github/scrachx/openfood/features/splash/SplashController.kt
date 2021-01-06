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
package openfoodfacts.github.scrachx.openfood.features.splash

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.features.splash.ISplashActivity.Controller
import openfoodfacts.github.scrachx.openfood.jobs.LoadTaxonomiesWorker
import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy
import java.util.concurrent.TimeUnit

/**
 * Created by Lobster on 03.03.18.
 */
class SplashController internal constructor(
        private val settings: SharedPreferences,
        private val view: ISplashActivity.View,
        private val activity: SplashActivity
) : Controller {
    private val disp = CompositeDisposable()

    private fun activateDownload(taxonomy: Taxonomy) {
        settings.edit { putBoolean(taxonomy.downloadActivatePreferencesId, true) }
    }

    private fun activateDownload(taxonomy: Taxonomy, vararg flavors: String) {
        if (isFlavors(*flavors)) {
            activateDownload(taxonomy)
        }
    }

    override fun refreshData() {
        activateDownload(Taxonomy.CATEGORY)
        activateDownload(Taxonomy.TAGS)
        activateDownload(Taxonomy.INVALID_BARCODES)
        activateDownload(Taxonomy.ADDITIVE, AppFlavors.OFF, AppFlavors.OBF)
        activateDownload(Taxonomy.COUNTRY, AppFlavors.OFF, AppFlavors.OBF)
        activateDownload(Taxonomy.LABEL, AppFlavors.OFF, AppFlavors.OBF)
        activateDownload(Taxonomy.ALLERGEN, AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF)
        activateDownload(Taxonomy.ANALYSIS_TAGS, AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF)
        activateDownload(Taxonomy.ANALYSIS_TAG_CONFIG, AppFlavors.OFF, AppFlavors.OBF, AppFlavors.OPFF)

        //first run ever off this application, whatever the version
        val firstRun = settings.getBoolean("firstRun", true)
        if (firstRun) settings.edit { putBoolean("firstRun", false) }

        // The service will load server resources only if newer than already downloaded...
        val request = OneTimeWorkRequest.from(LoadTaxonomiesWorker::class.java)
        WorkManager.getInstance(activity).let {
            it.enqueue(request)
            it.getWorkInfoByIdLiveData(request.id).observe(activity, { workInfo: WorkInfo? ->
                if (workInfo != null && workInfo.state == WorkInfo.State.RUNNING) {
                    view.showLoading()
                } else if (workInfo != null) {
                    view.hideLoading(workInfo.state == WorkInfo.State.FAILED)
                }
            })
        }

        // The 6000 delay is to show one loop of the multilingual logo. I asked for it ~ Pierre
        if (firstRun) {
            disp.add(Completable.timer(6, TimeUnit.SECONDS).subscribe { view.navigateToMainActivity() })
        } else {
            view.navigateToMainActivity()
        }
    }

    override fun dispose() = disp.dispose()

    override fun isDisposed() = disp.isDisposed
}