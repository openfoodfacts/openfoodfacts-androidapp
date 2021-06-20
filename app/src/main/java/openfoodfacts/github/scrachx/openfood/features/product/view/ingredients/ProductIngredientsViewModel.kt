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
package openfoodfacts.github.scrachx.openfood.features.product.view.ingredients

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.rx2.await
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import javax.inject.Inject

/**
 * Created by Lobster on 17.03.18.
 */
@HiltViewModel
class ProductIngredientsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val localeManager: LocaleManager
) : ViewModel() {

    val product = MutableLiveData<Product>()

    val additives = product.switchMap { product ->
        liveData<List<AdditiveName>> {
            val additivesTags = product.additivesTags
            if (additivesTags.isEmpty()) {
                emit(emptyList())
            } else {
                val languageCode = localeManager.getLanguage()

                additivesTags.map { tag ->
                    productRepository.getAdditiveByTagAndLanguageCode(tag, languageCode).await()
                        .takeUnless { it.isNull }
                        ?: productRepository.getAdditiveByTagAndDefaultLanguageCode(tag).await()
                }.filter { it.isNotNull }.let { emit(it) }
            }
        }
    }

    val allergens = product.switchMap { product ->
        liveData {
            val allergenTags = product.allergensTags
            if (allergenTags.isEmpty()) {
                emit(emptyList())
                return@liveData
            }

            val languageCode = localeManager.getLanguage()
            allergenTags.map { tag ->
                productRepository.getAllergenByTagAndLanguageCode(tag, languageCode)
                    .takeUnless { it.isNull }
                    ?: productRepository.getAllergenByTagAndDefaultLanguageCode(tag)
            }.filter { it.isNotNull }.let { emit(it) }
        }
    }


    val vitaminsTags = product.map(Product::vitaminTags)
    val mineralTags = product.map(Product::mineralTags)
    val otherNutritionTags = product.map(Product::otherNutritionTags)
    val aminoAcidTagsList = product.map(Product::aminoAcidTags)

}
