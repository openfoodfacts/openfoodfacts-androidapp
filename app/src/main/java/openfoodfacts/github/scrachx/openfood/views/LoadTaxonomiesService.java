package openfoodfacts.github.scrachx.openfood.views;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import java.util.Arrays;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

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
        doTask();
    }

    private void doTask() {
               Single.zip(
                productRepository.getLabels(true),
                productRepository.getTags(true),
                productRepository.getAllergens(true),
                productRepository.getCountries(true),
                productRepository.getAdditives(true),
                productRepository.getCategories(true), (labels, tags, allergens, countries, additives, categories) -> {
                    Completable.merge(
                            Arrays.asList(
                                    Completable.fromAction(() -> productRepository.saveLabels(labels)),
                                    Completable.fromAction(() -> productRepository.saveTags(tags)),
                                    Completable.fromAction(() -> productRepository.saveAllergens(allergens)),
                                    Completable.fromAction(() -> productRepository.saveCountries(countries)),
                                    Completable.fromAction(() -> productRepository.saveAdditives(additives)),
                                    Completable.fromAction(() -> productRepository.saveCategories(categories))
                            )
                    ).subscribeOn(Schedulers.computation())
                            .subscribe(() -> {
                                settings.edit().putLong(Utils.LAST_REFRESH_DATE, System.currentTimeMillis()).apply();
                            }, Throwable::printStackTrace);

                    return true;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toCompletable()
                 .subscribe(() -> {
                    //view.hideLoading(false);
                }, e -> {
                   //  e.printStackTrace();
                    //view.hideLoading(true);
                   //  builder.setContentText(getString(R.string.txtConnectionError))
                    //         .setOngoing(false);
                    // notificationManager.notify(17, builder.build());
                 });
    }

}
