
package openfoodfacts.github.scrachx.openfood.views.product.summary;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.utils.ProductInfoState;

/**
 * Created by Lobster on 17.03.18.
 */

public interface ISummaryProductPresenter {

    interface Actions {
        void loadAllergens();

        void loadCategories();

        void loadLabels();

        void dispose();

        void loadAdditives();
    }

    interface View {
        void showAllergens(List<AllergenName> allergens);

        void showCategories(List<CategoryName> categories);

        void showLabels(List<LabelName> labels);

        void showCategoriesState(@ProductInfoState String state);

        void showLabelsState(@ProductInfoState String state);

        void showAdditives(List<AdditiveName> additives);

        void showAdditivesState(@ProductInfoState String state);
    }

}