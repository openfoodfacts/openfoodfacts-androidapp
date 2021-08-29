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
package openfoodfacts.github.scrachx.openfood.features.categories.fragment

import android.util.Log
import android.view.View
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.models.entities.category.Category
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CategoryFragmentViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager
) : ViewModel() {
    private val allCategories = mutableListOf<CategoryName>()
    val shownCategories = ObservableArrayList<CategoryName>()
    val showProgress = ObservableInt(View.VISIBLE)
    val showOffline = ObservableInt(View.GONE)

    init {
        refreshCategories()
    }

    /**
     * Generates a network call for showing categories in CategoryFragment
     */
    fun refreshCategories() {
        viewModelScope.launch {
            showOffline.set(View.GONE)
            showProgress.set(View.VISIBLE)
            val categoryList = try {
                withContext(Dispatchers.IO) {
                    productRepository.getAllCategoriesByLanguageCode(localeManager.getLanguage()).await()
                        .takeUnless { it.isEmpty() }
                        ?: productRepository.getAllCategoriesByDefaultLanguageCode().await()
                            .takeUnless { it.isEmpty() }
                        ?: extractCategoriesNames(productRepository.getCategories())
                }
            } catch (err: Exception) {
                Log.e(CategoryFragmentViewModel::class.simpleName, "Error loading categories", err)
                if (err is UnknownHostException) {
                    showOffline.set(View.VISIBLE)
                    showProgress.set(View.GONE)
                }
                return@launch
            }

            allCategories += categoryList

            shownCategories.clear()
            shownCategories += categoryList

            showProgress.set(View.GONE)
        }

    }

    /**
     * Generate a new array which lists all the category names
     *
     * @param categories list of all the categories loaded using API
     */
    private fun extractCategoriesNames(categories: List<Category>) = categories
        .flatMap { it.names }
        .filter { it.languageCode == localeManager.getLanguage() }
        .sortedWith { o1, o2 -> o1.name!!.compareTo(o2.name!!) }

    /**
     * Search for all the category names that or equal to/start with a given string
     *
     * @param query string which is used to query for category names
     */
    fun searchCategories(query: String) {
        shownCategories.clear()
        shownCategories += allCategories.filter { it.name?.lowercase(Locale.getDefault())?.startsWith(query) == true }
    }

}

