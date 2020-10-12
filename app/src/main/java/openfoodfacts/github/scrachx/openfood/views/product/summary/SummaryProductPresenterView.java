package openfoodfacts.github.scrachx.openfood.views.product.summary;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.AnalysisTagConfig;
import openfoodfacts.github.scrachx.openfood.models.AnnotationResponse;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.Question;

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
    public void showCategoriesState(String state) {
        //empty impl
    }

    @Override
    public void showLabelsState(String state) {
        //empty impl
    }

    @Override
    public void showAdditives(List<AdditiveName> additives) {
        //empty impl
    }

    @Override
    public void showAdditivesState(String state) {
        //empty impl
    }

    @Override
    public void showAnalysisTags(List<AnalysisTagConfig> analysisTags) {
        //empty impl
    }
}
