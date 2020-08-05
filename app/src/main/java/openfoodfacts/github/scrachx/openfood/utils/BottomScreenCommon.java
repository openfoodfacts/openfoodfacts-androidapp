package openfoodfacts.github.scrachx.openfood.utils;

import androidx.fragment.app.FragmentManager;

import com.fasterxml.jackson.databind.JsonNode;

import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName;
import openfoodfacts.github.scrachx.openfood.views.product.ProductAttributeDetailsFragment;

public class BottomScreenCommon {
    private BottomScreenCommon() {
    }

    public static void showBottomSheet(JsonNode result, AdditiveName additive,
                                       FragmentManager fragmentManager) {
        showBottomSheet(result, additive.getId(),
            additive.getName(),
            SearchType.ADDITIVE, "additive_details_fragment",
            fragmentManager);
    }

    public static void showBottomSheet(JsonNode result, LabelName label,
                                       FragmentManager fragmentManager) {
        showBottomSheet(result, label.getId(),
            label.getName(),
            SearchType.LABEL, "label_details_fragment",
            fragmentManager);
    }

    public static void showBottomSheet(JsonNode result, CategoryName category,
                                       FragmentManager fragmentManager) {
        showBottomSheet(result, category.getId(),
            category.getName(),
            SearchType.CATEGORY, "category_details_fragment",
            fragmentManager);
    }

    public static void showBottomSheet(JsonNode result, AllergenName allergen,
                                       FragmentManager fragmentManager) {
        showBottomSheet(result, allergen.getId(),
            allergen.getName(),
            SearchType.ALLERGEN, "allergen_details_fragment",
            fragmentManager);
    }

    private static void showBottomSheet(JsonNode result,
                                        Long id,
                                        String name,
                                        SearchType searchType,
                                        String fragmentTag,
                                        FragmentManager fragmentManager) {
        String jsonObjectStr = null;
        if ((result != null)) {
            final JsonNode entities = result.get("entities");
            if (entities.elements().hasNext()) {
                jsonObjectStr = entities.elements().next().toString();
            }
        }
        ProductAttributeDetailsFragment fragment = ProductAttributeDetailsFragment.newInstance(jsonObjectStr, id, searchType, name);
        fragment.show(fragmentManager, fragmentTag);
    }
}
