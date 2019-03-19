package openfoodfacts.github.scrachx.openfood.views;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

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
        doTask(intent);
    }

    private void doTask(Intent intent) {
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
                //.doOnSubscribe(d -> view.showLoading())
                 .subscribe(() -> {
                    //view.hideLoading(false);
                  //   builder.setContentText(getString(R.string.txtLoaded));
                 //    builder.setOngoing(false);
                 //    notificationManager.notify(17, builder.build());
                }, e -> {
                   //  e.printStackTrace();
                    //view.hideLoading(true);
                   //  builder.setContentText(getString(R.string.txtConnectionError))
                    //         .setOngoing(false);
                    // notificationManager.notify(17, builder.build());
                 });
    }

}
