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
package openfoodfacts.github.scrachx.openfood.features.product.view.summary

import android.util.Log
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.awaitSingleOrNull
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.models.AnnotationAnswer
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState

class SummaryProductPresenter(
    private val languageCode: String,
    private val product: Product,
    private val view: ISummaryProductPresenter.View,
    private val productRepository: ProductRepository
) : ISummaryProductPresenter.Actions {

    override suspend fun loadAdditives() {
        val additivesTags = product.additivesTags
        if (additivesTags.isEmpty()) {
            view.showAdditivesState(ProductInfoState.Empty)
            return
        }
        view.showAdditivesState(ProductInfoState.Loading)
        val additives = try {
            additivesTags.map { tag ->
                val categoryName = productRepository.getAdditiveByTagAndLanguageCode(tag, languageCode).await()
                if (categoryName.isNotNull) categoryName
                else productRepository.getAdditiveByTagAndDefaultLanguageCode(tag).await()
            }.filter { it.isNotNull }
        } catch (err: Exception) {
            Log.e(SummaryProductPresenter::class.simpleName, "loadAdditives", err)
            view.showAdditivesState(ProductInfoState.Empty)
            return
        }
        if (additives.isEmpty()) view.showAdditivesState(ProductInfoState.Empty)
        else view.showAdditivesState(ProductInfoState.Data(additives))
    }

    override suspend fun loadAllergens() {
        val allergens = productRepository.getAllergensByEnabledAndLanguageCode(true, languageCode).await()
        withContext(Main) { view.showAllergens(allergens) }
    }

    override suspend fun loadCategories() {
        val categoriesTags = product.categoriesTags
        if (!categoriesTags.isNullOrEmpty()) {
            view.showCategoriesState(ProductInfoState.Loading)
            val categories = try {
                categoriesTags.map { tag ->
                    val categoryName = productRepository.getCategoryByTagAndLanguageCode(tag, languageCode).await()
                    if (categoryName.isNotNull) categoryName
                    else productRepository.getCategoryByTagAndLanguageCode(tag).await()
                }
            } catch (err: Exception) {
                Log.e(SummaryProductPresenter::class.java.simpleName, "loadCategories", err)
                view.showCategoriesState(ProductInfoState.Empty)
                return
            }
            if (categories.isEmpty()) {
                view.showCategoriesState(ProductInfoState.Empty)
            } else {
                view.showCategoriesState(ProductInfoState.Data(categories))
            }

        } else {
            view.showCategoriesState(ProductInfoState.Empty)
        }
    }

    override suspend fun loadLabels() {
        val labelsTags = product.labelsTags
        if (labelsTags != null && labelsTags.isNotEmpty()) {
            view.showLabelsState(ProductInfoState.Loading)
            val labels = try {
                labelsTags.map { tag ->
                    val labelName = productRepository.getLabelByTagAndLanguageCode(tag, languageCode).await()
                    if (labelName.isNotNull) labelName
                    else productRepository.getLabelByTagAndDefaultLanguageCode(tag).await()
                }.filter { it.isNotNull }
            } catch (err: Exception) {
                Log.e(SummaryProductPresenter::class.java.simpleName, "loadLabels", err)
                view.showLabelsState(ProductInfoState.Empty)
                return
            }

            if (labels.isEmpty()) view.showLabelsState(ProductInfoState.Empty)
            else view.showLabelsState(ProductInfoState.Data(labels))

        } else {
            view.showLabelsState(ProductInfoState.Empty)
        }
    }

    override suspend fun loadProductQuestion() {
        val question = withContext(IO) { productRepository.getProductQuestion(product.code, languageCode) }
            .awaitSingleOrNull() ?: return
        withContext(Main) { view.showProductQuestion(question) }
    }

    override suspend fun loadAnalysisTags() {
        if (!isFlavors(OFF, OBF, OPFF)) return

        val knownTags = product.ingredientsAnalysisTags

        view.showAnalysisTags(ProductInfoState.Loading)

        if (knownTags.isNotEmpty()) {
            val configs = try {
                knownTags.mapNotNull {
                    productRepository.getAnalysisTagConfigByTagAndLanguageCode(it, languageCode)
                }
            } catch (err: Exception) {
                Log.e(SummaryProductPresenter::class.simpleName, "loadAnalysisTags", err)
                view.showAnalysisTags(ProductInfoState.Empty)
                return
            }

            if (configs.isEmpty()) view.showAnalysisTags(ProductInfoState.Empty)
            else view.showAnalysisTags(ProductInfoState.Data(configs))

        } else {
            val configs = try {
                productRepository.getUnknownAnalysisTagConfigsByLanguageCode(languageCode).await()
            } catch (err: Exception) {
                Log.e(SummaryProductPresenter::class.simpleName, "loadAnalysisTags", err)
                view.showLabelsState(ProductInfoState.Empty)
                return
            }

            if (configs.isEmpty()) view.showAnalysisTags(ProductInfoState.Empty)
            else view.showAnalysisTags(ProductInfoState.Data(configs))

        }
    }

    override suspend fun annotateInsight(insightId: String, annotation: AnnotationAnswer) {
        val response = withContext(IO) { productRepository.annotateInsight(insightId, annotation).await() }
        withContext(Main) { view.showAnnotatedInsightToast(response) }
    }
}
