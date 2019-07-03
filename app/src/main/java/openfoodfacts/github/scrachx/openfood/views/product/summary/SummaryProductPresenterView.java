package openfoodfacts.github.scrachx.openfood.views.product.summary;

import openfoodfacts.github.scrachx.openfood.models.*;

import java.util.List;

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
    public void showAnnotatedInsightToast(InsightAnnotationResponse insightAnnotationResponse) {
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
}
