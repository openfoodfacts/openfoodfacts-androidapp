package openfoodfacts.github.scrachx.openfood.models

import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import openfoodfacts.github.scrachx.openfood.models.Modifier.Companion.DEFAULT
import openfoodfacts.github.scrachx.openfood.utils.getRoundNumber

/**
 * Use a round value for value and servingValue parameters
 *
 * @param title name of nutriment
 * @param valueStr value of nutriment per 100g
 * @param servingValueStr value of nutriment per serving
 * @param unitStr unit of nutriment
 * @param modifierStr one of the following: "<", ">", or "~"
 */
data class NutrimentListItem(
    val title: CharSequence?,
    val valueStr: CharSequence?,
    val servingValueStr: CharSequence?,
    val unitStr: CharSequence?,
    val modifierStr: CharSequence?,
    val displayVolumeHeader: Boolean = false,
) {

    constructor(volumeHeader: Boolean) : this(
        null,
        null,
        null,
        null,
        null,
        volumeHeader
    )

    constructor(title: CharSequence) : this(
        title,
        null,
        null,
        null,
        null
    )

    constructor(
        title: CharSequence,
        nutriment: ProductNutriments.ProductNutriment,
    ) : this(
        title,
        nutriment.per100gInUnit?.value,
        nutriment.perServingInUnit?.value,
        nutriment.unit,
        nutriment.modifier
    )

    constructor(
        title: CharSequence?,
        value: Float?,
        servingValue: Float?,
        unit: MeasurementUnit,
        modifier: Modifier,
        displayVolumeHeader: Boolean = false,
    ) : this(
        title,
        value?.let { getRoundNumber(it) },
        servingValue?.let { getRoundNumber(it) },
        unit.sym,
        modifier.takeUnless { it == DEFAULT }?.sym ?: "",
        displayVolumeHeader
    )
}

fun NutrimentListItem.bold() = copy(
    title = title?.let { bold(it) },
    valueStr = valueStr?.let { bold(it) },
    servingValueStr = servingValueStr?.let { bold(it) },
    unitStr = unitStr?.let { bold(it) },
    modifierStr = modifierStr?.let { bold(it) },
)

private fun bold(msg: CharSequence) = buildSpannedString { bold { append(msg) } }