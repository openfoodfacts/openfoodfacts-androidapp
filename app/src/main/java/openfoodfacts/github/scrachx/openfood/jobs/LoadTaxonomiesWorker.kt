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
package openfoodfacts.github.scrachx.openfood.jobs

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.getAppPreferences

/**
 * @param appContext The application [Context]
 * @param workerParams Parameters to setup the internal state of this worker
 */
@HiltWorker
class LoadTaxonomiesWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repo: ProductRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val settings = appContext.getAppPreferences()

        return try {
            repo.reloadLabelsFromServer()
            repo.reloadTagsFromServer()
            repo.reloadInvalidBarcodesFromServer()
            repo.reloadAllergensFromServer()
            repo.reloadIngredientsFromServer()
            repo.reloadAnalysisTagConfigsFromServer()
            repo.reloadAnalysisTagsFromServer()
            repo.reloadCountriesFromServer()
            repo.reloadAdditivesFromServer()
            repo.reloadCategoriesFromServer()
            repo.reloadStatesFromServer()
            repo.reloadStoresFromServer()
            repo.reloadBrandsFromServer()

            settings.edit { putBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false) }
            Result.success()
        } catch (err: Throwable) {
            Log.e(LOG_TAG, "Cannot download taxonomies from server.", err)
            Result.failure()
        }
    }

    companion object {
        private val LOG_TAG = LoadTaxonomiesWorker::class.simpleName
    }
}