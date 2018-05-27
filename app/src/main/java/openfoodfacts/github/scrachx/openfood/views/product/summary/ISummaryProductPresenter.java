
package org.openfoodfacts.scanner.views.product.summary;

import java.util.List;

import org.openfoodfacts.scanner.models.AllergenName;
import org.openfoodfacts.scanner.models.CategoryName;
import org.openfoodfacts.scanner.models.CountryName;
import org.openfoodfacts.scanner.models.LabelName;
import org.openfoodfacts.scanner.utils.ProductInfoState;

/**
 * Created by Lobster on 17.03.18.
 */

public interface ISummaryProductPresenter {

    interface Actions {
        void loadAllergens();

        void loadCategories();

        void loadLabels();

        void loadCountries();

        void dispose();
    }

    interface View {
        void showAllergens(List<AllergenName> allergens);

        void showCategories(List<CategoryName> categories);

        void showLabels(List<LabelName> labels);

        void showCountries(List<CountryName> countries);

        void showCategoriesState(@ProductInfoState String state);

        void showLabelsState(@ProductInfoState String state);

        void showCountriesState(@ProductInfoState String state);
    }

}