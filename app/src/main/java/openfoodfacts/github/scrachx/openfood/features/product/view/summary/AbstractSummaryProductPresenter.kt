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

import openfoodfacts.github.scrachx.openfood.models.AnnotationResponse
import openfoodfacts.github.scrachx.openfood.models.Question
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState

open class AbstractSummaryProductPresenter : ISummaryProductPresenter.View {
    override fun showAllergens(allergens: List<AllergenName>) {
        //empty impl
    }

    override fun showProductQuestion(question: Question) {
        //empty impl
    }

    override fun showAnnotatedInsightToast(annotationResponse: AnnotationResponse) {
        //empty impl
    }

    override fun showCategories(categories: List<CategoryName>) {
        //empty impl
    }

    override fun showLabels(labelNames: List<LabelName>) {
        //empty impl
    }

    override fun showCategoriesState(state: ProductInfoState) {
        //empty impl
    }

    override fun showLabelsState(state: ProductInfoState) {
        //empty impl
    }

    override fun showAdditives(additives: List<AdditiveName>) {
        //empty impl
    }

    override fun showAdditivesState(state: ProductInfoState) {
        //empty impl
    }

    override fun showAnalysisTags(analysisTags: List<AnalysisTagConfig>) {
        //empty impl
    }
}