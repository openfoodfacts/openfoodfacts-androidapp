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

import openfoodfacts.github.scrachx.openfood.models.AnnotationAnswer
import openfoodfacts.github.scrachx.openfood.models.AnnotationResponse
import openfoodfacts.github.scrachx.openfood.models.Question
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState

/**
 * Created by Lobster on 17.03.18.
 */
interface ISummaryProductPresenter {
    interface Actions {
        suspend fun loadProductQuestion()
        suspend fun loadAllergens()
        suspend fun loadCategories()
        suspend fun loadLabels()
        suspend fun loadAdditives()
        suspend fun loadAnalysisTags()
        suspend fun annotateInsight(annotation: AnnotationAnswer)
    }

    interface View {
        suspend fun showProductQuestion(question: Question)
        fun showAnnotatedInsightToast(annotationResponse: AnnotationResponse)

        fun showAllergens(allergens: List<AllergenName>)

        fun showCategoriesState(state: ProductInfoState<List<CategoryName>>)
        fun showLabelsState(state: ProductInfoState<List<LabelName>>)
        fun showAdditivesState(state: ProductInfoState<List<AdditiveName>>)

        suspend fun showAnalysisTags(state: ProductInfoState<List<AnalysisTagConfig>>)
    }
}