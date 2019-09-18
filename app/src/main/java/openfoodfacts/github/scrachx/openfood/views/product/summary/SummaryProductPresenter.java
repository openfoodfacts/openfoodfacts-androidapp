package openfoodfacts.github.scrachx.openfood.views.product.summary;

import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

import java.util.List;

/**
 * Created by Lobster on 17.03.18.
 */
public class SummaryProductPresenter implements ISummaryProductPresenter.Actions {
    private IProductRepository repository = ProductRepository.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();
    private ISummaryProductPresenter.View view;
    private Product product;

    public SummaryProductPresenter(Product product, ISummaryProductPresenter.View view) {
        this.product = product;
        this.view = view;
    }

    @Override
    public void loadAdditives() {
        List<String> additivesTags = product.getAdditivesTags();
        if (additivesTags != null && !additivesTags.isEmpty()) {
            final String languageCode = LocaleHelper.getLanguage(OFFApplication.getInstance());
            disposable.add(
                Observable.fromArray(additivesTags.toArray(new String[0]))
                    .flatMapSingle(tag -> repository.getAdditiveByTagAndLanguageCode(tag, languageCode)
                        .flatMap(categoryName -> {
                            if (categoryName.isNull()) {
                                return repository.getAdditiveByTagAndDefaultLanguageCode(tag);
                            } else {
                                return Single.just(categoryName);
                            }
                        }))
                    .filter(AdditiveName::isNotNull)
                    .toList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(d -> view.showAdditivesState(ProductInfoState.LOADING))
                    .subscribe(additives -> {
                        if (additives.isEmpty()) {
                            view.showAdditivesState(ProductInfoState.EMPTY);
                        } else {
                            view.showAdditives(additives);
                        }
                    }, e -> {
                        Log.e(SummaryProductPresenter.class.getSimpleName(), "loadAdditives", e);
                        view.showAdditivesState(ProductInfoState.EMPTY);
                    })
            );
        } else {
            view.showAdditivesState(ProductInfoState.EMPTY);
        }
    }


    @Override
    public void loadAllergens(Runnable runIfError) {
        final String languageCode = LocaleHelper.getLanguage(OFFApplication.getInstance());
        disposable.add(
            repository.getAllergensByEnabledAndLanguageCode(true, languageCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(allergens -> view.showAllergens(allergens), e -> {
                    if(runIfError!=null){
                        runIfError.run();
                    }
                    Log.e(SummaryProductPresenter.class.getSimpleName(), "loadAllergens", e);
                })
        );
    }

    @Override
    public void loadCategories() {
        List<String> categoriesTags = product.getCategoriesTags();
        if (categoriesTags != null && !categoriesTags.isEmpty()) {
            final String languageCode = LocaleHelper.getLanguage(OFFApplication.getInstance());
            disposable.add(
                Observable.fromArray(categoriesTags.toArray(new String[0]))
                    .flatMapSingle(tag -> repository.getCategoryByTagAndLanguageCode(tag, languageCode)
                        .flatMap(categoryName -> {
                            if (categoryName.isNull()) {
                                return repository.getCategoryByTagAndDefaultLanguageCode(tag);
                            } else {
                                return Single.just(categoryName);
                            }
                        }))
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
                        Log.e(SummaryProductPresenter.class.getSimpleName(), "loadCategories", e);
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
            final String languageCode = LocaleHelper.getLanguage(OFFApplication.getInstance());
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
                    .filter(LabelName::isNotNull)
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
                        Log.e(SummaryProductPresenter.class.getSimpleName(), "loadLabels", e);
                        view.showLabelsState(ProductInfoState.EMPTY);
                    })
            );
        } else {
            view.showLabelsState(ProductInfoState.EMPTY);
        }
    }

    @Override
    public void loadProductQuestion() {
        final String languageCode = LocaleHelper.getLanguage(OFFApplication.getInstance());
        disposable.add(
            repository.getSingleProductQuestion(product.getCode(), languageCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showProductQuestion,e->Log.e(SummaryProductPresenter.this.getClass().getSimpleName(),"loadProductQuestion",e))
        );
    }

    @Override
    public void annotateInsight(String insightId, int annotation) {
        disposable.add(
            repository.annotateInsight(insightId, annotation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showAnnotatedInsightToast, e->Log.e(SummaryProductPresenter.this.getClass().getSimpleName(),"annotateInsight",e))
        );
    }

    @Override
    public void dispose() {
        if (!disposable.isDisposed()) {
            disposable.clear();
        }
    }

}
