package openfoodfacts.github.scrachx.openfood.views;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import java.util.Arrays;

public class LoadTaxonomiesService extends IntentService {
    private IProductRepository productRepository;
    private SharedPreferences settings;

    public LoadTaxonomiesService() {
        super("LoadTaxonomiesService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        productRepository = ProductRepository.getInstance();
        settings = getSharedPreferences("prefs", 0);
        try {
            doTask();
        } catch (Throwable throwable) {
            handleError(throwable);
        }
    }

    private void doTask() {
        final Consumer<Throwable> throwableConsumer = throwable -> handleError(throwable);

        if (BuildConfig.FLAVOR.equals("off")) {
            Single.zip(
                productRepository.getLabels(true),
                productRepository.getTags(true),
                productRepository.getAllergens(true),
                productRepository.getCountries(true),
                productRepository.getAdditives(true),
                productRepository.getCategories(true), (labels, tags, allergens,  countries, additives, categories) -> {
                    Completable.merge(
                        Arrays.asList(
                            Completable.fromAction(() -> productRepository.saveLabels(labels)),
                            Completable.fromAction(() -> productRepository.saveTags(tags)),
                            Completable.fromAction(() -> productRepository.saveAllergens(allergens)),
                            Completable.fromAction(() -> productRepository.saveCountries(countries)),
                            Completable.fromAction(() -> productRepository.saveAdditives(additives)),
                            Completable.fromAction(() -> productRepository.saveCategories(categories))
                        )
                    ).doOnError(throwableConsumer)
                        .subscribeOn(Schedulers.computation())
                        .subscribe(() -> {
                            settings.edit().putLong(Utils.LAST_REFRESH_DATE, System.currentTimeMillis()).apply();
                        }, throwableConsumer);

                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwableConsumer)
                .toCompletable()
                .subscribe();
        } else if (BuildConfig.FLAVOR.equals("obf")) {
            Single.zip(
                productRepository.getLabels(true),
                productRepository.getTags(true),
                productRepository.getIngredients(false), //TODO : have a test on last-modified-date of ingredients.json before download it. Then pass the parameter to true
                productRepository.getCountries(true),
                productRepository.getAdditives(true),
                productRepository.getCategories(true), (labels, tags, ingredients, countries, additives, categories) -> {
                    Completable.merge(
                        Arrays.asList(
                            Completable.fromAction(() -> productRepository.saveLabels(labels)),
                            Completable.fromAction(() -> productRepository.saveTags(tags)),
                            Completable.fromAction(() -> productRepository.saveIngredients(ingredients)),
                            Completable.fromAction(() -> productRepository.saveCountries(countries)),
                            Completable.fromAction(() -> productRepository.saveAdditives(additives)),
                            Completable.fromAction(() -> productRepository.saveCategories(categories))
                        )
                    ).doOnError(throwableConsumer).subscribeOn(Schedulers.computation())
                        .subscribe(() -> {
                            settings.edit().putLong(Utils.LAST_REFRESH_DATE, System.currentTimeMillis()).apply();
                        }, throwableConsumer);

                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwableConsumer)
                .toCompletable()
                .subscribe();
        } else if (BuildConfig.FLAVOR.equals("opf") || BuildConfig.FLAVOR.equals("opff")) {
            Single.zip(
                productRepository.getTags(true),
                productRepository.getCategories(true), (tags, categories) -> {
                    Completable.merge(
                        Arrays.asList(
                            Completable.fromAction(() -> productRepository.saveTags(tags)),
                            Completable.fromAction(() -> productRepository.saveCategories(categories))
                        )
                    ).subscribeOn(Schedulers.computation())
                        .doOnError(throwableConsumer)
                        .subscribe(() -> {
                            settings.edit().putLong(Utils.LAST_REFRESH_DATE, System.currentTimeMillis()).apply();
                        }, throwableConsumer);

                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwableConsumer)
                .toCompletable()
                .subscribe();
        }
    }

    private void handleError(Throwable throwable) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(OFFApplication.getInstance(), R.string.errorWeb, Toast.LENGTH_LONG).show());
        Log.e(LoadTaxonomiesService.class.getSimpleName(), "can't load products", throwable);
    }
}
