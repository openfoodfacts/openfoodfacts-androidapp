/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.views.viewmodel.category;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.models.entities.category.Category;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.viewmodel.ViewModel;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
public class CategoryFragmentViewModel extends ViewModel {
    private final ProductRepository repository;
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

    /**
     * Generates a network call for showing categories in CategoryFragment
     */
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
                    return repository.getCategories()
                        .flatMap(categories -> Single.just(extractCategoriesNames(categories)));
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

    /**
     * Generate a new array which lists all the category names
     *
     * @param categories list of all the categories loaded using API
     */
    private List<CategoryName> extractCategoriesNames(List<Category> categories) {
        List<CategoryName> categoryNames = new ArrayList<>();
        final String languageCode = LocaleHelper.getLanguage(OFFApplication.getInstance());
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

    /**
     * Search for all the category names that or equal to/start with a given string
     *
     * @param query string which is used to query for category names
     */
    public void searchCategories(String query) {
        List<CategoryName> newFilteredCategories = new ArrayList<>();
        for (CategoryName categoryName : categories) {
            if (categoryName.getName() != null && categoryName.getName().toLowerCase().startsWith(query)) {
                newFilteredCategories.add(categoryName);
            }
        }

        filteredCategories.set(newFilteredCategories);
    }
}
