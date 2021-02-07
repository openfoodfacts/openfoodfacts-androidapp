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

import android.util.Log
import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.features.viewmodel.BaseViewModel
import openfoodfacts.github.scrachx.openfood.models.entities.category.Category
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository.getAllCategoriesByDefaultLanguageCode
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository.getAllCategoriesByLanguageCode
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import java.net.UnknownHostException
import java.util.*

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
class CategoryFragmentViewModel : BaseViewModel() {
    private val allCategories = mutableListOf<CategoryName>()
    val shownCategories = ObservableField(mutableListOf<CategoryName>())
    val showProgress = ObservableInt(View.VISIBLE)
    val showOffline = ObservableInt(View.GONE)


    override fun subscribe(subscriptions: CompositeDisposable) = refreshCategories()

    /**
     * Generates a network call for showing categories in CategoryFragment
     */
    fun refreshCategories() {
        getAllCategoriesByLanguageCode(getLanguage(OFFApplication.instance))
                .doOnSubscribe {
                    showOffline.set(View.GONE)
                    showProgress.set(View.VISIBLE)
                }
                .flatMap {
                    if (it.isEmpty()) {
                        getAllCategoriesByDefaultLanguageCode()
                    } else {
                        Single.just(it)
                    }
                }
                .flatMap {
                    if (it.isEmpty()) {
                        ProductRepository.getCategories().map(this::extractCategoriesNames)
                    } else {
                        Single.just(it)
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError {
                    Log.e(CategoryFragmentViewModel::class.java.canonicalName, "Error loading categories", it)
                    if (it is UnknownHostException) {
                        showOffline.set(View.VISIBLE)
                        showProgress.set(View.GONE)
                    }
                }
                .subscribe { categoryList ->
                    allCategories.addAll(categoryList)
                    shownCategories.set(categoryList.toMutableList())
                    showProgress.set(View.GONE)
                }.addTo(subscriptions ?: error("Cannot refresh view while not binded."))
    }

    /**
     * Generate a new array which lists all the category names
     *
     * @param categories list of all the categories loaded using API
     */
    private fun extractCategoriesNames(categories: List<Category>) = categories
            .flatMap { it.names }
            .filter { it.languageCode == getLanguage(OFFApplication.instance) }
            .sortedWith { o1, o2 -> o1.name!!.compareTo(o2.name!!) }

    /**
     * Search for all the category names that or equal to/start with a given string
     *
     * @param query string which is used to query for category names
     */
    fun searchCategories(query: String) = shownCategories.set(
            allCategories
                    .filter { it.name?.toLowerCase(Locale.getDefault())?.startsWith(query) == true }
                    .toMutableList()
    )
}