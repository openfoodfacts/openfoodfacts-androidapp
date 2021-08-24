package openfoodfacts.github.scrachx.openfood.models

import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import openfoodfacts.github.scrachx.openfood.utils.getRoundNumber

/**
 * Header with bold values
 * @param title
 * @param value
 * @param servingValue
 * @param unit
 */
class BoldNutrimentListItem(
    title: CharSequence,
    value: Float? = null,
    servingValue: Float? = null,
    unit: MeasurementUnit? = null,
    modifier: Modifier? = null
) : NutrimentListItem(
    bold(title),
    value?.let { bold(getRoundNumber(it)) },
    servingValue?.let { bold(getRoundNumber(it)) },
    unit?.let { bold(it.sym) },
    modifier?.let { bold(it.nullIfDefault()?.sym ?: "") }
) {
    constructor(
        title: CharSequence,
        nutriment: ProductNutriments.ProductNutriment
    ) : this(
        title,
        nutriment.per100gInUnit.value,
        nutriment.perServingInUnit?.value,
        nutriment.unit,
        nutriment.modifier
    )
}

private fun bold(msg: CharSequence) = buildSpannedString { bold { append(msg) } }

