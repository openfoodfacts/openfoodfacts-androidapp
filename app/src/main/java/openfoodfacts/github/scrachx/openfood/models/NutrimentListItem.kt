package openfoodfacts.github.scrachx.openfood.models

import openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber

/**
 * Use a round value for value and servingValue parameters
 *
 * @param title name of nutriment
 * @param value value of nutriment per 100g
 * @param servingValue value of nutriment per serving
 * @param unit unit of nutriment
 * @param modifier one of the following: "<", ">", or "~"
 */
open class NutrimentListItem(
        internal val title: CharSequence?,
        value: CharSequence?,
        servingValue: CharSequence?,
        val unit: CharSequence?,
        val modifier: CharSequence?,
        val displayVolumeHeader: Boolean = false
) {
    val servingValue = if (servingValue.isNullOrBlank()) "" else getRoundNumber(servingValue)
    val value = value?.let { getRoundNumber(it) } ?: ""

    constructor(displayVolumeHeader: Boolean) :
            this(null, null, null, null, null, displayVolumeHeader)

}