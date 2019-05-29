package openfoodfacts.github.scrachx.openfood.views.product.ingredients;

import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
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

public class IngredientsProductPresenter implements IIngredientsProductPresenter.Actions {

    private IProductRepository repository = ProductRepository.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();
    private IIngredientsProductPresenter.View view;

    private Product product;


    public IngredientsProductPresenter(Product product, IIngredientsProductPresenter.View view) {
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
                            .filter(additiveName -> additiveName.isNotNull())
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
                                Log.e(IngredientsProductPresenter.class.getSimpleName(),"loadAdditives",e);
                                view.showAdditivesState(ProductInfoState.EMPTY);
                            })
            );
        } else {
            view.showAdditivesState(ProductInfoState.EMPTY);
        }
    }

    @Override
    public void loadAllergens() {
        List<String> allergenTags = product.getAllergensTags();
        if (allergenTags != null && !allergenTags.isEmpty()) {
            final String languageCode = LocaleHelper.getLanguage(OFFApplication.getInstance());
            disposable.add(
                    Observable.fromArray(allergenTags.toArray(new String[allergenTags.size()]))
                              .flatMapSingle(tag -> repository.getAllergenByTagAndLanguageCode(tag, languageCode)
                                                              .flatMap(allergenName -> {
                                                                  if (allergenName.isNull()) {
                                                                      return repository.getAllergenByTagAndDefaultLanguageCode(tag);
                                                                  } else {
                                                                      return Single.just(allergenName);
                                                                  }
                                                              }))
                              .toList()
                              .subscribeOn(Schedulers.io())
                              .observeOn(AndroidSchedulers.mainThread())
                              .doOnSubscribe(d -> view.showAllergensState(ProductInfoState.LOADING))
                              .subscribe(allergens -> {
                                  if (allergens.isEmpty()) {
                                      view.showAllergensState(ProductInfoState.EMPTY);
                                  } else {
                                      view.showAllergens(allergens);
                                  }
                              }, e -> {
                                  Log.e(IngredientsProductPresenter.class.getSimpleName(),"loadAllergens",e);
                                  view.showAllergensState(ProductInfoState.EMPTY);
                              })
            );
        } else {
            view.showAllergensState(ProductInfoState.EMPTY);
        }
    }

    @Override
    public void dispose() {
        if (!disposable.isDisposed()) {
            disposable.clear();
        }
    }
}
