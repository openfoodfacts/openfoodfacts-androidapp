package openfoodfacts.github.scrachx.openfood.models

import openfoodfacts.github.scrachx.openfood.utils.bold

/**
 * Group of NutrimentListItem with bold values
 */
class HeaderNutrimentListItem : NutrimentListItem {
    /**
     * Header with bold values
     * @param title
     * @param value
     * @param servingValue
     * @param unit
     */
    constructor(
            title: CharSequence,
            value: CharSequence,
            servingValue: CharSequence,
            unit: CharSequence,
            modifier: CharSequence
    ) : super(bold(title), bold(value), bold(servingValue), bold(unit), bold(modifier))

    /**
     * Header with only bold title
     * @param title
     */
    constructor(title: CharSequence) : super(bold(title), "", "", "", "")
}