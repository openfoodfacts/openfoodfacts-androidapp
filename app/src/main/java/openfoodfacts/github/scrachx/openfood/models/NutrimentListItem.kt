package openfoodfacts.github.scrachx.openfood.models

import openfoodfacts.github.scrachx.openfood.utils.getRoundNumber

/**
 * Use a round value for value and servingValue parameters
 *
 * @param title name of nutriment
 * @param value value of nutriment per 100g
 * @param servingValue value of nutriment per serving
 * @param unitStr unit of nutriment
 * @param modifierStr one of the following: "<", ">", or "~"
 */
open class NutrimentListItem(
    internal val title: CharSequence?,
    value: CharSequence?,
    servingValue: CharSequence?,
    val unitStr: CharSequence?,
    val modifierStr: CharSequence?,
    val displayVolumeHeader: Boolean = false
) {
    val servingValueStr = servingValue?.takeIf { it.isNotBlank() }?.let { getRoundNumber(it) }
    val value = value?.let { getRoundNumber(it) }

    constructor(volumeHeader: Boolean) : this(
        null,
        null,
        null,
        null,
        null,
        volumeHeader
    )

    constructor(
        title: CharSequence?,
        value: Float?,
        servingValue: Float?,
        unit: MeasurementUnit,
        modifier: Modifier,
        displayVolumeHeader: Boolean = false
    ) : this(
        title,
        value?.let { getRoundNumber(it) },
        servingValue?.let { getRoundNumber(it) },
        unit.sym,
        modifier.nullIfDefault()?.sym ?: "",
        displayVolumeHeader
    )

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