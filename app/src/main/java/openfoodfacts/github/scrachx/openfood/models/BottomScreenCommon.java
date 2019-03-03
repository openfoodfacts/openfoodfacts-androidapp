package openfoodfacts.github.scrachx.openfood.models;


import android.support.v4.app.FragmentManager;

import org.json.JSONException;
import org.json.JSONObject;

import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.views.product.ProductAttributeDetailsFragment;

public class BottomScreenCommon {
    public static void showBottomScreen(JSONObject result, AdditiveName additive,
                                        FragmentManager fragmentManager) {
        showBottomSheet(result, additive.getId(),
                additive.getName(), additive.getWikiDataId(),
                SearchType.ADDITIVE, "additive_details_fragment",
                fragmentManager);
    }

    public static void showBottomScreen(JSONObject result, LabelName label,
                                        FragmentManager fragmentManager) {
        showBottomSheet(result, label.getId(),
                label.getName(), label.getWikiDataId(),
                SearchType.LABEL, "label_details_fragment",
                fragmentManager);
    }

    public static void showBottomScreen(JSONObject result, CategoryName category,
                                        FragmentManager fragmentManager) {
        showBottomSheet(result, category.getId(),
                category.getName(), category.getWikiDataId(),
                SearchType.CATEGORY, "category_details_fragment",
                fragmentManager);
    }

    public static void showBottomScreen(JSONObject result, AllergenName allergen,
                                        FragmentManager fragmentManager) {
        showBottomSheet(result, allergen.getId(),
                allergen.getName(), allergen.getWikiDataId(),
                SearchType.ALLERGEN, "allergen_details_fragment",
                fragmentManager);
    }

    public static void showBottomSheet(JSONObject result, Long id, String name,
                                       String wikidataId, String searchType, String fragmentTag,
                                       FragmentManager fragmentManager) {
        try {
            String jsonObjectStr = (result != null) ? result.getJSONObject("entities")
                    .getJSONObject(wikidataId).toString() : null;
            ProductAttributeDetailsFragment fragment =
                    ProductAttributeDetailsFragment.newInstance(jsonObjectStr, id, searchType, name);
            fragment.show(fragmentManager, fragmentTag);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
