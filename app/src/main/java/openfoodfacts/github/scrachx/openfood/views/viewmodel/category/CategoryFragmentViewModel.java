package openfoodfacts.github.scrachx.openfood.views.viewmodel.category;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.models.Category;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.viewmodel.ViewModel;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class CategoryFragmentViewModel extends ViewModel {
    private final IProductRepository repository;
    private final List<CategoryName> categories;
    private final ObservableField<List<CategoryName>> filteredCategories;
    private final ObservableInt showProgress;
    private final ObservableInt showOffline;

    public CategoryFragmentViewModel() {
        this.repository = ProductRepository.getInstance();
        this.categories = new ArrayList<>();
        this.filteredCategories = new ObservableField<>(Collections.emptyList());
        this.showProgress = new ObservableInt(View.VISIBLE);
        this.showOffline = new ObservableInt(View.GONE);
    }

    @Override
    protected void subscribe(@NonNull CompositeDisposable subscriptions) {
        loadCategories();
    }

    public void loadCategories() {
        subscriptions.add(repository.getAllCategoriesByLanguageCode(LocaleHelper.getLanguage(OFFApplication.getInstance()))
                .doOnSubscribe(disposable -> {
                    showOffline.set(View.GONE);
                    showProgress.set(View.VISIBLE);
                })
                .flatMap(categoryNames -> {
                    if (categoryNames.isEmpty()) {
                        return repository.getAllCategoriesByDefaultLanguageCode();
                    } else {
                        return Single.just(categoryNames);
                    }
                })
                .flatMap(categoryNames -> {
                    if (categoryNames.isEmpty()) {
                        return repository.getCategories(true)
                                .flatMap(categories -> {
                                    saveCategories(categories);
                                    return Single.just(extractCategoriesNames(categories));
                                });
                    } else {
                        return Single.just(categoryNames);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categoryList -> {
                            categories.addAll(categoryList);
                            filteredCategories.set(categoryList);
                            showProgress.set(View.GONE);
                        },
                        throwable -> {
                            Log.e(CategoryFragmentViewModel.class.getCanonicalName(), "Error loading categories", throwable);
                            if (throwable instanceof UnknownHostException) {
                                showOffline.set(View.VISIBLE);
                                showProgress.set(View.GONE);
                            }
                        }));
    }

    private void saveCategories(List<Category> categories) {
        Completable.fromAction(() -> repository.saveCategories(categories))
                .subscribeOn(Schedulers.computation())
                .subscribe(() -> {
                }, e->Log.e(CategoryFragmentViewModel.class.getSimpleName(),"saveCategories",e));
    }

    private List<CategoryName> extractCategoriesNames(List<Category> categories) {
        List<CategoryName> categoryNames = new ArrayList<>();
        final String languageCode=LocaleHelper.getLanguage(OFFApplication.getInstance());
        for (Category category : categories) {
            for (CategoryName categoryName : category.getNames()) {
                if (categoryName.getLanguageCode().equals(languageCode)) {
                    categoryNames.add(categoryName);
                }
            }
        }

        Collections.sort(categoryNames, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        return categoryNames;
    }

    public ObservableField<List<CategoryName>> getFilteredCategories() {
        return filteredCategories;
    }

    public ObservableInt getShowProgress() {
        return showProgress;
    }

    public ObservableInt getShowOffline() {
        return showOffline;
    }
  
    public void searchCategories(String query) {
        List<CategoryName> newFilteredCategories = new ArrayList<>();
        for (CategoryName categoryName : categories) {
            if (categoryName.getName()!=null && categoryName.getName().toLowerCase().startsWith(query)) {
                newFilteredCategories.add(categoryName);
            }
        }

        filteredCategories.set(newFilteredCategories);
    }
}
