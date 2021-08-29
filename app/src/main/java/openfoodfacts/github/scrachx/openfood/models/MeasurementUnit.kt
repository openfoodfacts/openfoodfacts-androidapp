package openfoodfacts.github.scrachx.openfood.models

import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit.*
import org.jetbrains.annotations.Contract

/**
 * @param sym The symbol of the unit. The symbol of GRAM is "g".
 */
enum class MeasurementUnit(val sym: String) {
    ENERGY_KJ("kj"),
    ENERGY_KCAL("kcal"),
    UNIT_KILOGRAM("kg"),
    UNIT_GRAM("g"),
    UNIT_MILLIGRAM("mg"),
    UNIT_MICROGRAM("µg"),
    UNIT_DV("% DV"),
    UNIT_LITER("l"),
    UNIT_DECILITRE("dl"),
    UNIT_CENTILITRE("cl"),
    UNIT_MILLILITRE("ml"),
    UNIT_OZ("oz"),
    UNIT_IU("IU");

    companion object {
        fun findBySymbol(symbol: String) = values().find { it.sym == symbol }
        fun requireBySymbol(symbol: String) = findBySymbol(symbol)
            ?: throw IllegalArgumentException("Could not find unit with symbol '$symbol'.")
    }
}

val DEFAULT_UNIT = UNIT_GRAM
val ENERGY_UNITS = listOf(ENERGY_KCAL, ENERGY_KJ)

/**
 * All the values given by the api are in gram. For all unit it's possible to convert back to th
 *
 * @receiver the initial unit
 * @return if the unit is % DV, the api gives the value in g
 */
@Contract(pure = true)
internal fun MeasurementUnit.getRealUnit(): MeasurementUnit = when (this) {
    UNIT_DV, UNIT_IU -> UNIT_GRAM
    else -> this
}