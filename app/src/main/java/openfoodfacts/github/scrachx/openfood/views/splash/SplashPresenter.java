package openfoodfacts.github.scrachx.openfood.views.splash;

import android.content.SharedPreferences;
import android.util.Log;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
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

            /*productRepository.getCategories(false)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(categories -> {
                        Log.e("SIZE", String.valueOf(categories.size()));
                    }, throwable -> {
                        throwable.printStackTrace();
                    });*/

            CategoryName categoryName1 = productRepository.getCategoryByTagAndDefaultLanguageCode("en:beverages");
            CategoryName categoryName2 = productRepository.getCategoryByTagAndDefaultLanguageCode("en:artificially-sweetened-beverages");
            CategoryName categoryName3 = productRepository.getCategoryByTagAndDefaultLanguageCode("en:carbonated-drinks");
            CategoryName categoryName4 = productRepository.getCategoryByTagAndDefaultLanguageCode("en:diet-beverages");
            CategoryName categoryName5 = productRepository.getCategoryByTagAndDefaultLanguageCode("en:sodas");
            CategoryName categoryName6 = productRepository.getCategoryByTagAndDefaultLanguageCode("en:colas");
            CategoryName categoryName7 = productRepository.getCategoryByTagAndDefaultLanguageCode("en:diet-sodas");
            CategoryName categoryName8 = productRepository.getCategoryByTagAndDefaultLanguageCode("en:diet-cola-soft-drink");

            if (categoryName1 != null)
                Log.e("CAT_1", categoryName1.getName());

            if (categoryName2 != null)
                Log.e("CAT_2", categoryName2.getName());

            if (categoryName3 != null)
                Log.e("CAT_3", categoryName3.getName());

            if (categoryName4 != null)
                Log.e("CAT_4", categoryName4.getName());

            if (categoryName5 != null)
                Log.e("CAT_5", categoryName5.getName());

            if (categoryName6 != null)
                Log.e("CAT_6", categoryName6.getName());

            if (categoryName7 != null)
                Log.e("CAT_7", categoryName7.getName());

            if (categoryName8 != null)
                Log.e("CAT_8", categoryName8.getName());

            if (true) { //true if data was refreshed more than 1 day ago
                Single.zip(
                        productRepository.getLabels(true),
                        productRepository.getTags(true),
                        productRepository.getAllergens(true),
                        productRepository.getCountries(true),
                        productRepository.getAdditives(true),
                        productRepository.getCategories(true), (labels, tags, allergens, countries, additives, categories) -> {
                            Completable.fromAction(() -> productRepository.saveLabels(labels))
                                    .subscribeOn(Schedulers.computation())
                                    .subscribe(() -> {
                                    }, Throwable::printStackTrace);

                            Completable.fromAction(() -> productRepository.saveTags(tags))
                                    .subscribeOn(Schedulers.computation())
                                    .subscribe(() -> {
                                    }, Throwable::printStackTrace);

                            Completable.fromAction(() -> productRepository.saveAllergens(allergens))
                                    .subscribeOn(Schedulers.computation())
                                    .subscribe(() -> {
                                    }, Throwable::printStackTrace);

                            Completable.fromAction(() -> productRepository.saveCountries(countries))
                                    .subscribeOn(Schedulers.computation())
                                    .subscribe(() -> {
                                    }, Throwable::printStackTrace);

                            Completable.fromAction(() -> productRepository.saveAdditives(additives))
                                    .subscribeOn(Schedulers.computation())
                                    .subscribe(() -> {
                                    }, Throwable::printStackTrace);

                            Completable.fromAction(() -> productRepository.saveCategories(categories))
                                    .subscribeOn(Schedulers.computation())
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
