package openfoodfacts.github.scrachx.openfood.utils;

import android.util.Log;

import androidx.fragment.app.FragmentManager;

import org.json.JSONException;
import org.json.JSONObject;

import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName;
import openfoodfacts.github.scrachx.openfood.views.product.ProductAttributeDetailsFragment;

public class BottomScreenCommon {
    private BottomScreenCommon() {
    }

    public static void showBottomScreen(JSONObject result, AdditiveName additive,
                                        FragmentManager fragmentManager) {
        showBottomSheet(result, additive.getId(),
            additive.getName(),
            SearchType.ADDITIVE, "additive_details_fragment",
            fragmentManager);
    }

    public static void showBottomScreen(JSONObject result, LabelName label,
                                        FragmentManager fragmentManager) {
        showBottomSheet(result, label.getId(),
            label.getName(),
            SearchType.LABEL, "label_details_fragment",
            fragmentManager);
    }

    public static void showBottomScreen(JSONObject result, CategoryName category,
                                        FragmentManager fragmentManager) {
        showBottomSheet(result, category.getId(),
            category.getName(),
            SearchType.CATEGORY, "category_details_fragment",
            fragmentManager);
    }

    public static void showBottomScreen(JSONObject result, AllergenName allergen,
                                        FragmentManager fragmentManager) {
        showBottomSheet(result, allergen.getId(),
            allergen.getName(),
            SearchType.ALLERGEN, "allergen_details_fragment",
            fragmentManager);
    }

    private static void showBottomSheet(JSONObject result, Long id, String name,
                                        SearchType searchType, String fragmentTag,
                                        FragmentManager fragmentManager) {
        try {
            String jsonObjectStr = null;
            if ((result != null)) {
                final JSONObject entities = result.getJSONObject("entities");
                if (entities.length() > 0) {
                    jsonObjectStr = entities
                        .getJSONObject(entities.keys().next()).toString();
                }
            }
            ProductAttributeDetailsFragment fragment =
                ProductAttributeDetailsFragment.newInstance(jsonObjectStr, id, searchType, name);
            fragment.show(fragmentManager, fragmentTag);
        } catch (JSONException e) {
            Log.e(BottomScreenCommon.class.getSimpleName(), "showBottomSheet for " + name, e);
        }
    }
}
