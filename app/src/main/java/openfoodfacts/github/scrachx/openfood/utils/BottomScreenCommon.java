package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.fasterxml.jackson.databind.JsonNode;

import openfoodfacts.github.scrachx.openfood.features.product.view.attribute.ProductAttributeFragment;
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName;

public class BottomScreenCommon {
    private BottomScreenCommon() {
    }

    public static void showBottomSheet(JsonNode result,
                                       @NonNull AdditiveName additive,
                                       @NonNull FragmentManager fragmentManager) {
        showBottomSheet(result, additive.getId(),
            additive.getName(),
            SearchType.ADDITIVE, "additive_details_fragment",
            fragmentManager);
    }

    public static void showBottomSheet(JsonNode result,
                                       @NonNull LabelName label,
                                       @NonNull FragmentManager fragmentManager) {
        showBottomSheet(result, label.getId(),
            label.getName(),
            SearchType.LABEL, "label_details_fragment",
            fragmentManager);
    }

    public static void showBottomSheet(@Nullable JsonNode result,
                                       @NonNull CategoryName category,
                                       @NonNull FragmentManager fragmentManager) {
        showBottomSheet(result, category.getId(),
            category.getName(),
            SearchType.CATEGORY, "category_details_fragment",
            fragmentManager);
    }

    public static void showBottomSheet(@Nullable JsonNode result,
                                       @NonNull AllergenName allergen,
                                       @NonNull FragmentManager fragmentManager) {
        showBottomSheet(result, allergen.getId(),
            allergen.getName(),
            SearchType.ALLERGEN, "allergen_details_fragment",
            fragmentManager);
    }

    private static void showBottomSheet(@Nullable JsonNode result,
                                        Long id,
                                        String name,
                                        SearchType searchType,
                                        String fragmentTag,
                                        @NonNull FragmentManager fragmentManager) {
        String jsonObjectStr = null;
        if (result != null) {
            final JsonNode entities = result.get("entities");
            if (entities.elements().hasNext()) {
                jsonObjectStr = entities.elements().next().toString();
            }
        }
        ProductAttributeFragment.newInstance(jsonObjectStr, id, searchType, name)
            .show(fragmentManager, fragmentTag);
    }
}
