package openfoodfacts.github.scrachx.openfood.models

import openfoodfacts.github.scrachx.openfood.utils.bold

/**
 * Header with bold values
 * @param title
 * @param value
 * @param servingValue
 * @param unit
 */
class BoldNutrimentListItem(
        title: CharSequence,
        value: CharSequence = "",
        servingValue: CharSequence = "",
        unit: CharSequence = "",
        modifier: CharSequence = ""
) : NutrimentListItem(
        bold(title),
        bold(value),
        bold(servingValue),
        bold(unit),
        bold(modifier)
)