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
package openfoodfacts.github.scrachx.openfood.features.viewmodel.category

import android.app.Application
import android.util.Log
import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.models.entities.category.Category
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
@HiltViewModel
class CategoryFragmentViewModel @Inject constructor(
        val app: Application,
        private val productRepository: ProductRepository
) : AndroidViewModel(app) {
    private val allCategories = mutableListOf<CategoryName>()
    val shownCategories = ObservableArrayList<CategoryName>()
    val showProgress = ObservableInt(View.VISIBLE)
    val showOffline = ObservableInt(View.GONE)

    private val disposable = CompositeDisposable()

    init {
        refreshCategories()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    /**
     * Generates a network call for showing categories in CategoryFragment
     */
    fun refreshCategories() {
        productRepository.getAllCategoriesByLanguageCode(getLanguage(app))
                .doOnSubscribe {
                    showOffline.set(View.GONE)
                    showProgress.set(View.VISIBLE)
                }
                .flatMap {
                    if (it.isEmpty()) {
                        productRepository.getAllCategoriesByDefaultLanguageCode()
                    } else Single.just(it)
                }
                .flatMap {
                    if (it.isEmpty()) {
                        productRepository.getCategories().map(this::extractCategoriesNames)
                    } else Single.just(it)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.e(CategoryFragmentViewModel::class.simpleName, "Error loading categories", it)
                    if (it is UnknownHostException) {
                        showOffline.set(View.VISIBLE)
                        showProgress.set(View.GONE)
                    }
                }
                .subscribe { categoryList ->
                    allCategories += categoryList

                    shownCategories.clear()
                    shownCategories += categoryList

                    showProgress.set(View.GONE)
                }.addTo(disposable)
    }

    /**
     * Generate a new array which lists all the category names
     *
     * @param categories list of all the categories loaded using API
     */
    private fun extractCategoriesNames(categories: List<Category>) = categories
            .flatMap { it.names }
            .filter { it.languageCode == getLanguage(app) }
            .sortedWith { o1, o2 -> o1.name!!.compareTo(o2.name!!) }

    /**
     * Search for all the category names that or equal to/start with a given string
     *
     * @param query string which is used to query for category names
     */
    fun searchCategories(query: String) {
        shownCategories.clear()
        shownCategories += allCategories.filter { it.name?.toLowerCase(Locale.getDefault())?.startsWith(query) == true }
    }

}

