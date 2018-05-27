package org.openfoodfacts.scanner.views.product.summary;

import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import org.openfoodfacts.scanner.models.Product;
import org.openfoodfacts.scanner.repositories.IProductRepository;
import org.openfoodfacts.scanner.repositories.ProductRepository;
import org.openfoodfacts.scanner.utils.ProductInfoState;

/**
 * Created by Lobster on 17.03.18.
 */

public class SummaryProductPresenter implements ISummaryProductPresenter.Actions {

    private IProductRepository repository = ProductRepository.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();
    private ISummaryProductPresenter.View view;

    private Product product;
    private String languageCode = Locale.getDefault().getLanguage();

    public SummaryProductPresenter(Product product, ISummaryProductPresenter.View view) {
        this.product = product;
        this.view = view;
    }

    @Override
    public void loadAllergens() {
        disposable.add(
                repository.getAllergensByEnabledAndLanguageCode(true, Locale.getDefault().getLanguage())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(allergens -> {
                            view.showAllergens(allergens);
                        }, Throwable::printStackTrace)
        );
    }

    @Override
    public void loadCategories() {
        List<String> categoriesTags = product.getCategoriesTags();
        if (categoriesTags != null && !categoriesTags.isEmpty()) {
            disposable.add(
                    Observable.fromArray(categoriesTags.toArray(new String[categoriesTags.size()]))
                            .flatMapSingle(tag -> repository.getCategoryByTagAndLanguageCode(tag, languageCode)
                                    .flatMap(categoryName -> {
                                        if (categoryName.isNull()) {
                                            return repository.getCategoryByTagAndDefaultLanguageCode(tag);
                                        } else {
                                            return Single.just(categoryName);
                                        }
                                    }))
                            .filter(categoryName -> categoryName.isNotNull())
                            .toList()
                            .doOnSubscribe(d -> view.showCategoriesState(ProductInfoState.LOADING))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(categories -> {
                                if (categories.isEmpty()) {
                                    view.showCategoriesState(ProductInfoState.EMPTY);
                                } else {
                                    view.showCategories(categories);
                                }
                            }, e -> {
                                e.printStackTrace();
                                view.showCategoriesState(ProductInfoState.EMPTY);
                            })
            );
        } else {
            view.showCategoriesState(ProductInfoState.EMPTY);
        }
    }

    @Override
    public void loadLabels() {
        List<String> labelsTags = product.getLabelsTags();
        if (labelsTags != null && !labelsTags.isEmpty()) {
            disposable.add(
                    Observable.fromArray(labelsTags.toArray(new String[labelsTags.size()]))
                            .flatMapSingle(tag -> repository.getLabelByTagAndLanguageCode(tag, languageCode)
                                    .flatMap(labelName -> {
                                        if (labelName.isNull()) {
                                            return repository.getLabelByTagAndDefaultLanguageCode(tag);
                                        } else {
                                            return Single.just(labelName);
                                        }
                                    }))
                            .filter(labelName -> labelName.isNotNull())
                            .toList()
                            .doOnSubscribe(d -> view.showLabelsState(ProductInfoState.LOADING))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(labels -> {
                                if (labels.isEmpty()) {
                                    view.showLabelsState(ProductInfoState.EMPTY);
                                } else {
                                    view.showLabels(labels);
                                }
                            }, e -> {
                                e.printStackTrace();
                                view.showLabelsState(ProductInfoState.EMPTY);
                            })
            );
        } else {
            view.showLabelsState(ProductInfoState.EMPTY);
        }
    }

    @Override
    public void loadCountries() {
        List<String> countriesTags = product.getCountriesTags();
        if (countriesTags != null && !countriesTags.isEmpty()) {
            disposable.add(
                    Observable.fromArray(countriesTags.toArray(new String[countriesTags.size()]))
                            .flatMapSingle(tag -> repository.getCountryByTagAndLanguageCode(tag, languageCode)
                                    .flatMap(countryName -> {
                                        if (countryName.isNull()) {
                                            return repository.getCountryByTagAndDefaultLanguageCode(tag);
                                        } else {
                                            return Single.just(countryName);
                                        }
                                    }))
                            .filter(countryName -> countryName.isNotNull())
                            .toList()
                            .doOnSubscribe(d -> view.showCountriesState(ProductInfoState.LOADING))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(countries -> {
                                if (countries.isEmpty()) {
                                    view.showCountriesState(ProductInfoState.EMPTY);
                                } else {
                                    view.showCountries(countries);
                                }
                            }, e -> {
                                e.printStackTrace();
                                view.showCountriesState(ProductInfoState.EMPTY);
                            })
            );
        } else {
            view.showCountriesState(ProductInfoState.EMPTY);
        }

    }

    @Override
    public void dispose() {
        if (!disposable.isDisposed()) {
            disposable.clear();
        }
    }
}