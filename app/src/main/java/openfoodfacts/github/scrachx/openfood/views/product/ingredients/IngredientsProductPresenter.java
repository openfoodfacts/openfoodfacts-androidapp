package org.openfoodfacts.scanner.views.product.ingredients;

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

public class IngredientsProductPresenter implements IIngredientsProductPresenter.Actions {

    private IProductRepository repository = ProductRepository.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();
    private IIngredientsProductPresenter.View view;

    private Product product;
    private String languageCode = Locale.getDefault().getLanguage();

    public IngredientsProductPresenter(Product product, IIngredientsProductPresenter.View view) {
        this.product = product;
        this.view = view;
    }

    @Override
    public void loadAdditives() {
        List<String> additivesTags = product.getAdditivesTags();
        if (additivesTags != null && !additivesTags.isEmpty()) {
            disposable.add(
                    Observable.fromArray(additivesTags.toArray(new String[additivesTags.size()]))
                            .flatMapSingle(tag -> repository.getAdditiveByTagAndLanguageCode(tag, languageCode)
                                    .flatMap(categoryName -> {
                                        if (categoryName.isNull()) {
                                            return repository.getAdditiveByTagAndDefaultLanguageCode(tag);
                                        } else {
                                            return Single.just(categoryName);
                                        }
                                    }))
                            .filter(additiveName -> additiveName.isNotNull())
                            .toList()
                            .doOnSubscribe(d -> view.showAdditivesState(ProductInfoState.LOADING))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(additives -> {
                                if (additives.isEmpty()) {
                                    view.showAdditivesState(ProductInfoState.EMPTY);
                                } else {
                                    view.showAdditives(additives);
                                }
                            }, e -> {
                                e.printStackTrace();
                                view.showAdditivesState(ProductInfoState.EMPTY);
                            })
            );
        } else {
            view.showAdditivesState(ProductInfoState.EMPTY);
        }
    }

    @Override
    public void dispose() {
        if (!disposable.isDisposed()) {
            disposable.clear();
        }
    }
}