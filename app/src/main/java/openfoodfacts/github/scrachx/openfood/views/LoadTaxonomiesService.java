package openfoodfacts.github.scrachx.openfood.views;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class LoadTaxonomiesService extends IntentService {
    private ProductRepository productRepository;
    private SharedPreferences settings;

    public LoadTaxonomiesService() {
        super("LoadTaxonomiesService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        productRepository = (ProductRepository) ProductRepository.getInstance();
        settings = getSharedPreferences("prefs", 0);
        try {
            doTask();
        } catch (Throwable throwable) {
            handleError(throwable);
        }
    }

    private void doTask() {

        final Consumer<Throwable> throwableConsumer = this::handleError;
        List<SingleSource<?>> syncObservables = new ArrayList<>();
        syncObservables.add(productRepository.reloadLabelsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadTagsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadAllergensFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadIngredientsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadAnalysisTagConfigsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadAnalysisTagsFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.relodCountriesFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadAdditivesFromServer().subscribeOn(Schedulers.io()));
        syncObservables.add(productRepository.reloadCategoriesFromServer().subscribeOn(Schedulers.io()));
        Single.zip(syncObservables, objects -> {
            //we do nothing there. Maybe there is a better solution to launch these singles in //
            return true;
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(throwableConsumer)
            .ignoreElement()
            .subscribe();
    }

    private void handleError(Throwable throwable) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(OFFApplication.getInstance(), R.string.errorWeb, Toast.LENGTH_LONG).show());
        Log.e(LoadTaxonomiesService.class.getSimpleName(), "can't load products", throwable);
    }
}
