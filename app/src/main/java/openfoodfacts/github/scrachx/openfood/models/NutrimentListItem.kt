package openfoodfacts.github.scrachx.openfood.models

import openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber
import org.apache.commons.lang.StringUtils

open class NutrimentListItem {
    val displayVolumeHeader: Boolean
    val modifier: String?
    val servingValue: String?
    val title: String?
    val unit: String?
    val value: String?

    constructor(displayVolumeHeader: Boolean) {
        this.displayVolumeHeader = displayVolumeHeader
        title = null
        value = null
        servingValue = null
        unit = null
        modifier = null
    }

    /**
     * Use a round value for value and servingValue parameters
     *
     * @param title name of nutriment
     * @param value value of nutriment per 100g
     * @param servingValue value of nutriment per serving
     * @param unit unit of nutriment
     * @param modifier one of the following: "<", ">", or "~"
     */
    constructor(title: String?, value: String?, servingValue: String?, unit: String?, modifier: String?) {
        this.title = title
        this.value = getRoundNumber(value!!)
        this.servingValue = if (StringUtils.isBlank(servingValue)) StringUtils.EMPTY else getRoundNumber(servingValue!!)
        this.unit = unit
        this.modifier = modifier
        displayVolumeHeader = false
    }
}