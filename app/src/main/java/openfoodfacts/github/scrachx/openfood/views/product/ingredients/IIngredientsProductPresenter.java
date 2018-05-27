package org.openfoodfacts.scanner.views.product.ingredients;

import java.util.List;

import org.openfoodfacts.scanner.models.AdditiveName;
import org.openfoodfacts.scanner.utils.ProductInfoState;

/**
 * Created by Lobster on 17.03.18.
 */

public interface IIngredientsProductPresenter {

    interface Actions {
        void loadAdditives();

        void dispose();
    }

    interface View {
        void showAdditives(List<AdditiveName> additives);

        void showAdditivesState(@ProductInfoState String state);
    }

}