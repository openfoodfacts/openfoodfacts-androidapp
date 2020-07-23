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

package openfoodfacts.github.scrachx.openfood.views.product.summary;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.AnnotationResponse;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.Question;
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState;

public class SummaryProductPresenterView implements ISummaryProductPresenter.View {
    @Override
    public void showAllergens(List<AllergenName> allergens) {
        //empty impl
    }

    @Override
    public void showProductQuestion(Question question) {
        //empty impl
    }

    @Override
    public void showAnnotatedInsightToast(AnnotationResponse annotationResponse) {
        //empty impl
    }

    @Override
    public void showCategories(List<CategoryName> categories) {
        //empty impl
    }

    @Override
    public void showLabels(List<LabelName> labels) {
        //empty impl
    }

    @Override
    public void showCategoriesState(ProductInfoState state) {
        //empty impl
    }

    @Override
    public void showLabelsState(ProductInfoState state) {
        //empty impl
    }

    @Override
    public void showAdditives(List<AdditiveName> additives) {
        //empty impl
    }

    @Override
    public void showAdditivesState(ProductInfoState state) {
        //empty impl
    }

    @Override
    public void showAnalysisTags(List<AnalysisTagConfig> analysisTags) {
        //empty impl
    }
}
