package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.SharedPreferences;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;

/**
 * Created by Lobster on 03.03.18.
 */

public class SplashPresenter implements ISplashPresenter.Actions {

    private final String ADDITIVE_IMPORT = "ADDITIVE_IMPORT";
    private final String LAST_REFRESH_DATE = "last_refresh_date";
    private final Long REFRESH_PERIOD = 24 * 60 * 60 * 1000L;

    private ISplashPresenter.View view;
    private SharedPreferences settings;
    private IProductRepository productRepository;

    public SplashPresenter(SharedPreferences settings, ISplashPresenter.View view) {
        this.view = view;
        this.settings = settings;
        productRepository = ProductRepository.getInstance();
    }

    @Override
    public void refreshData() {
        if (BuildConfig.FLAVOR.equals("off")) {
            boolean firstRun = settings.getBoolean("firstRun", true);
            if (firstRun) {
                settings.edit()
                        .putBoolean("firstRun", false)
                        .apply();
            }

            if (isNeedToRefresh()) { //true if data was refreshed more than 1 day ago
                Single.zip(
                        productRepository.getLabels(true),
                        productRepository.getTags(true),
                        productRepository.getAllergens(true),
                        productRepository.getCountries(true),
                        productRepository.getAdditives(true), (labels, tags, allergens, countries, additives) -> {
                            Completable.fromAction(() -> productRepository.saveLabels(labels))
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(() -> {

                                    }, Throwable::printStackTrace);

                            Completable.fromAction(() -> productRepository.saveTags(tags))
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(() -> {
                                    }, Throwable::printStackTrace);

                            Completable.fromAction(() -> productRepository.saveAllergens(allergens))
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(() -> {
                                    }, Throwable::printStackTrace);

                            Completable.fromAction(() -> productRepository.saveCountries(countries))
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(() -> {
                                    }, Throwable::printStackTrace);

                            Completable.fromAction(() -> productRepository.saveAdditives(additives))
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(() -> {
                                    }, Throwable::printStackTrace);

                            settings.edit().putLong(LAST_REFRESH_DATE, System.currentTimeMillis()).apply();

                            return true;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .toCompletable()
                        .doOnSubscribe(d -> view.showLoading())
                        .subscribe(() -> {
                            view.hideLoading(false);
                            view.navigateToMainActivity();
                        }, e -> {
                            e.printStackTrace();
                            view.hideLoading(true);
                            view.navigateToMainActivity();
                        });
            } else {
                view.navigateToMainActivity();
            }
        } else {
            view.navigateToMainActivity();
        }
    }

    /*
    * This method checks if data was refreshed more than 1 day ago
     */
    private Boolean isNeedToRefresh() {
        return System.currentTimeMillis() - settings.getLong(LAST_REFRESH_DATE, 0) > REFRESH_PERIOD;
    }
}
