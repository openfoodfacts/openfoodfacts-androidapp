package openfoodfacts.github.scrachx.openfood.models;

import android.support.annotation.NonNull;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;

/**
 * Group of NutrimentItem with bold values
 */
public class HeaderNutrimentItem extends NutrimentItem {
    /**
     * Header with bold values
     * @param title
     * @param value
     * @param servingValue
     * @param unit
     */
    public HeaderNutrimentItem(@NonNull CharSequence title, @NonNull CharSequence value,
                               @NonNull CharSequence servingValue, @NonNull CharSequence unit,
                               @NonNull CharSequence modifier) {
        super(bold(title), bold(value), bold(servingValue), bold(unit), bold(modifier));
    }


    /**
     * Header with only bold title
     * @param title
     */
    public HeaderNutrimentItem(@NonNull CharSequence title) {
        super(bold(title), "", "", "", "");
    }
}
