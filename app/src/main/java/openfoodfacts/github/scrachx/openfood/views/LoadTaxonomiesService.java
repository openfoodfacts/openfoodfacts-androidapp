package openfoodfacts.github.scrachx.openfood.views;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public class LoadTaxonomiesService extends IntentService {
    public static final String RECEIVER_KEY = "receiver";
    private ProductRepository productRepository;
    private SharedPreferences settings;
    private ResultReceiver receiver;
    private Disposable disposable;
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    public LoadTaxonomiesService() {
        super("LoadTaxonomiesService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        productRepository = (ProductRepository) ProductRepository.getInstance();
        settings = getSharedPreferences("prefs", 0);
        receiver = intent == null ? null : intent.getParcelableExtra(RECEIVER_KEY);

        doTask();
    }

    private void doTask() {
        showLoading();

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

        disposable = Completable.merge(syncObservables).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> {
                settings.edit().putBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false).apply();
                hideLoading(false);
            }, throwable -> {
                Log.e(LoadTaxonomiesService.class.getSimpleName(), "can't load products", throwable);
                hideLoading(true);
            });
    }

    @Override
    public boolean stopService(Intent name) {
        if (disposable != null) {
            disposable.dispose();
        }
        return super.stopService(name);
    }

    private void showLoading() {
        if (receiver != null) {
            receiver.send(STATUS_RUNNING, new Bundle());
        }
    }

    private void hideLoading(boolean isError) {
        if (receiver != null) {
            receiver.send(isError ? STATUS_ERROR : STATUS_FINISHED, new Bundle());
        }
    }
}
