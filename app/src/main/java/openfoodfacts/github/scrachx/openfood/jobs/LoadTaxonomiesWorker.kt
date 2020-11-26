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
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.Utils

class LoadTaxonomiesWorker
/**
 * @param appContext The application [Context]
 * @param workerParams Parameters to setup the internal state of this worker
 */
(appContext: Context, workerParams: WorkerParameters) : RxWorker(appContext, workerParams) {
    override fun createWork(): Single<Result> {
        val productRepository = ProductRepository.instance
        val settings = OFFApplication.getInstance().getSharedPreferences("prefs", 0)

        // We use completable because we only care about state (error or completed), not returned value
        val syncObservables = mutableListOf<Completable>()
        syncObservables.add(productRepository.reloadLabelsFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        syncObservables.add(productRepository.reloadTagsFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        syncObservables.add(productRepository.reloadInvalidBarcodesFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        syncObservables.add(productRepository.reloadAllergensFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        syncObservables.add(productRepository.reloadIngredientsFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        syncObservables.add(productRepository.reloadAnalysisTagConfigsFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        syncObservables.add(productRepository.reloadAnalysisTagsFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        syncObservables.add(productRepository.reloadCountriesFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        syncObservables.add(productRepository.reloadAdditivesFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        syncObservables.add(productRepository.reloadCategoriesFromServer().subscribeOn(Schedulers.io()).ignoreElement())
        return Completable.merge(syncObservables)
                .toSingle {
                    settings.edit().putBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false).apply()
                    Result.success()
                }.onErrorReturn { throwable ->
                    Log.e(LOG_TAG, "Cannot download taxonomies from server.", throwable)
                    Result.failure()
                }
    }

    companion object {
        private val LOG_TAG = LoadTaxonomiesWorker::class.java.simpleName
    }
}