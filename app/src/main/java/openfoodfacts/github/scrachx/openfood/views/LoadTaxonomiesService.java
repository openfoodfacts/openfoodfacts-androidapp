package openfoodfacts.github.scrachx.openfood.views;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class LoadTaxonomiesService extends IntentService {
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
        receiver = intent == null ? null : intent.getParcelableExtra("receiver");
        try {
            doTask();
        } catch (Throwable throwable) {
            handleError(throwable);
        }
    }

    private void doTask() {
        final Consumer<Throwable> throwableConsumer = this::handleError;
        showLoading();
        List<SingleSource<?>> syncObservables = new ArrayList<>();
        syncObservables.add(productRepository.reloadLabelsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadTagsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadAllergensFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadIngredientsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadAnalysisTagConfigsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadAnalysisTagsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadCountriesFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadAdditivesFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadCategoriesFromServer().subscribeOn(Schedulers.io()));

        disposable = Single.zip(syncObservables, objects -> {
            //we do nothing there. Maybe there is a better solution to launch these singles in //
            return true;
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(throwableConsumer)
            .ignoreElement()
            .subscribe(() -> {
                settings.edit().putBoolean(Utils.FORCE_REFRESH_TAXONOMIES, false).apply();
                hideLoading(false);
            }, throwableConsumer);
    }

    private void handleError(Throwable throwable) {
        Log.e(LoadTaxonomiesService.class.getSimpleName(), "can't load products", throwable);
        hideLoading(true);
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
