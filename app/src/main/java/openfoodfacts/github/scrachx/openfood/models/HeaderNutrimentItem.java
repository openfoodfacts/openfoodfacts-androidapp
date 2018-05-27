package openfoodfacts.github.scrachx.openfood.models;

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
    public HeaderNutrimentItem(CharSequence title, CharSequence value, CharSequence servingValue, CharSequence unit) {
        super(bold(title), bold(value), bold(servingValue), bold(unit));
    }


    /**
     * Header with only bold title
     * @param title
     */
    public HeaderNutrimentItem(CharSequence title) {
        super(bold(title), "", "", "");
    }
}
