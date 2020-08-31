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

package openfoodfacts.github.scrachx.openfood.jobs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public class LoadTaxonomiesWorker extends RxWorker {
    private static final String LOG_TAG = LoadTaxonomiesWorker.class.getSimpleName();

    /**
     * @param appContext The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public LoadTaxonomiesWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public Single<Result> createWork() {
        ProductRepository productRepository = ProductRepository.getInstance();
        SharedPreferences settings = OFFApplication.getInstance().getSharedPreferences("prefs", 0);

        // We use completable because we only care about state (error or completed), not returned value
        List<CompletableSource> syncObservables = new ArrayList<>();
        syncObservables.add(productRepository.reloadLabelsFromServer().subscribeOn(Schedulers.io()).ignoreElement());
        syncObservables.add(productRepository.reloadTagsFromServer().subscribeOn(Schedulers.io()).ignoreElement());
        syncObservables.add(productRepository.reloadInvalidBarcodesFromServer().subscribeOn(Schedulers.io()).ignoreElement());
        syncObservables.add(productRepository.reloadAllergensFromServer().subscribeOn(Schedulers.io()).ignoreElement());
        syncObservables.add(productRepository.reloadIngredientsFromServer().subscribeOn(Schedulers.io()).ignoreElement());
        syncObservables.add(productRepository.reloadAnalysisTagConfigsFromServer().subscribeOn(Schedulers.io()).ignoreElement());
        syncObservables.add(productRepository.reloadAnalysisTagsFromServer().subscribeOn(Schedulers.io()).ignoreElement());
        syncObservables.add(productRepository.reloadCountriesFromServer().subscribeOn(Schedulers.io()).ignoreElement());
        syncObservables.add(productRepository.reloadAdditivesFromServer().subscribeOn(Schedulers.io()).ignoreElement());
        syncObservables.add(productRepository.reloadCategoriesFromServer().subscribeOn(Schedulers.io()).ignoreElement());

        return Completable.merge(syncObservables)
            .toSingle(() -> {
                settings.edit().putBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false).apply();
                return Result.success();
            }).onErrorReturn(throwable -> {
                Log.e(LOG_TAG, "Cannot download taxonomies from server.", throwable);
                return Result.failure();
            });
    }
}
