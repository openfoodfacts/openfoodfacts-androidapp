package openfoodfacts.github.scrachx.openfood.models;

import android.support.v4.app.FragmentManager;
import android.util.Log;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.views.product.ProductAttributeDetailsFragment;
import org.json.JSONException;
import org.json.JSONObject;

public class BottomScreenCommon {
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
                                        String searchType, String fragmentTag,
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
