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
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.jobs.LoadTaxonomiesWorker
import openfoodfacts.github.scrachx.openfood.models.entities.TaxonomyEntity
import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy
import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/**
 * Created by Lobster on 03.03.18.
 */
class SplashController internal constructor(
    private val settings: SharedPreferences,
    private val view: SplashActivity,
    private val activity: SplashActivity,
) {

    private fun <T : TaxonomyEntity> Taxonomy<T>.activateDownload(vararg flavors: String) {
        if (flavors.isEmpty() || isFlavors(*flavors)) {
            settings.edit { putBoolean(downloadActivatePreferencesId, true) }
        }
    }

    @ExperimentalTime
    suspend fun refreshData() = withContext(Dispatchers.Default) {
        Categories.activateDownload()
        Tags.activateDownload()
        InvalidBarcodes.activateDownload()
        Additives.activateDownload(OFF, OBF)
        Countries.activateDownload(OFF, OBF)
        Labels.activateDownload(OFF, OBF)
        Allergens.activateDownload(OFF, OBF, OPFF)
        AnalysisTags.activateDownload(OFF, OBF, OPFF)
        AnalysisTagConfigs.activateDownload(OFF, OBF, OPFF)
        ProductStates.activateDownload(OFF, OBF, OPFF)
        Stores.activateDownload(OFF, OBF, OPFF)
        Brands.activateDownload(OFF, OBF)

        //first run ever off this application, whatever the version
        val firstRun = settings.getBoolean("firstRun", true)
        if (firstRun) settings.edit { putBoolean("firstRun", false) }

        // The service will load server resources only if newer than already downloaded...
        withContext(Dispatchers.Main) {
            val request = OneTimeWorkRequest.from(LoadTaxonomiesWorker::class.java)
            WorkManager.getInstance(activity).let {
                it.enqueue(request)
                it.getWorkInfoByIdLiveData(request.id).observe(activity) { workInfo: WorkInfo? ->
                    if (workInfo != null && workInfo.state == WorkInfo.State.RUNNING) {
                        activity.lifecycleScope.launch { view.showLoading() }
                    } else if (workInfo != null) {
                        activity.lifecycleScope.launch { view.hideLoading(workInfo.state == WorkInfo.State.FAILED) }
                    }
                }
            }
        }

        // The 6000 delay is to show one loop of the multilingual logo. I asked for it ~ Pierre
        if (firstRun) {
            delay(6.seconds)
            view.navigateToMainActivity()
        } else {
            view.navigateToMainActivity()
        }
    }
}