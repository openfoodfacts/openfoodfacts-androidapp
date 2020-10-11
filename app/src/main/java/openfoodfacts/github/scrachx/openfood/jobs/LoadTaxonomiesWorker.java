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
        ProductRepository productRepository = (ProductRepository) ProductRepository.getInstance();
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

        return Completable.merge(syncObservables).subscribeOn(Schedulers.io())
            .toSingle(() -> {
                settings.edit().putBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false).apply();
                return Result.success();
            }).onErrorReturn(throwable -> {
                Log.e(LoadTaxonomiesWorker.class.getSimpleName(), "can't load products", throwable);
                return Result.failure();
            });
    }
}
