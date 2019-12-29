package openfoodfacts.github.scrachx.openfood.models;

import androidx.annotation.NonNull;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;

/**
 * Group of NutrimentListItem with bold values
 */
public class HeaderNutrimentListItem extends NutrimentListItem {
    /**
     * Header with bold values
     * @param title
     * @param value
     * @param servingValue
     * @param unit
     */
    public HeaderNutrimentListItem(@NonNull CharSequence title, @NonNull CharSequence value,
                                   @NonNull CharSequence servingValue, @NonNull CharSequence unit,
                                   @NonNull CharSequence modifier) {
        super(bold(title), bold(value), bold(servingValue), bold(unit), bold(modifier));
    }


    /**
     * Header with only bold title
     * @param title
     */
    public HeaderNutrimentListItem(@NonNull CharSequence title) {
        super(bold(title), "", "", "", "");
    }
}
