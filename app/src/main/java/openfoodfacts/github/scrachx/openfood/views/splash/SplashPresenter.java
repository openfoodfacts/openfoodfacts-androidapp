package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.SharedPreferences;

import java.util.Arrays;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.repositories.DietRepository;
import openfoodfacts.github.scrachx.openfood.repositories.IDietRepository;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

/**
 * Created by Lobster on 03.03.18.
 */

public class SplashPresenter implements ISplashPresenter.Actions {

    /**
     * Mutiplied by 6*30* to reduce the issue. TODO: fix https://github.com/openfoodfacts/openfoodfacts-androidapp/issues/1616
     */
    private final Long REFRESH_PERIOD = 6 * 30 * 24 * 60 * 60 * 1000L;

    private ISplashPresenter.View view;
    private SharedPreferences settings;
    private IProductRepository productRepository;
    private IDietRepository dietRepository;

    public SplashPresenter(SharedPreferences settings, ISplashPresenter.View view) {
        this.view = view;
        this.settings = settings;
        productRepository = ProductRepository.getInstance();
        dietRepository = DietRepository.getInstance();
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
                        productRepository.getIngredients(false),
                        productRepository.getCountries(true),
                        productRepository.getAdditives(true),
                        productRepository.getCategories(true), (labels, tags, allergens, ingredients, countries, additives, categories) -> {
                            Completable.merge(
                                    Arrays.asList(
                                            Completable.fromAction(() -> productRepository.saveLabels(labels)),
                                            Completable.fromAction(() -> productRepository.saveTags(tags)),
                                            Completable.fromAction(() -> productRepository.saveAllergens(allergens)),
                                            Completable.fromAction(() -> productRepository.saveIngredients(ingredients, true)),
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
                            view.navigateToMainActivity();
                        }, e -> {
                            e.printStackTrace();
                            //view.hideLoading(true);
                            view.navigateToMainActivity();
                        });
            } else {
                view.navigateToMainActivity();
            }
        } else if (BuildConfig.FLAVOR.equals("obf")) {
            boolean firstRun = settings.getBoolean("firstRun", true);
            if (firstRun) {
                settings.edit()
                        .putBoolean("firstRun", false)
                        .apply();
            }

            if (isNeedToRefresh()) { //true if data was refreshed more than 1 day ago
                Single.zip(
                        productRepository.getIngredients(false),
                        productRepository.getCountries(true), (ingredients, countries) -> {
                            Completable.merge(
                                    Arrays.asList(
                                            Completable.fromAction(() -> productRepository.saveIngredients(ingredients)),
                                            Completable.fromAction(() -> productRepository.saveCountries(countries))
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
                            view.navigateToMainActivity();
                        }, e -> {
                            e.printStackTrace();
                            //view.hideLoading(true);
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
        return System.currentTimeMillis() - settings.getLong(Utils.LAST_REFRESH_DATE, 0) > REFRESH_PERIOD;
    }
}
