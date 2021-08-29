package openfoodfacts.github.scrachx.openfood.utils

import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit
import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit.*


data class Measurement(
    val value: Float,
    val unit: MeasurementUnit
)

fun measure(value: Float, unit: MeasurementUnit) = Measurement(value, unit)

/**
 * Converts a given measurement to grams.
 *
 * @receiver the measurement to convert.
 * @return the converted measurement.
 */
fun Measurement.convertToGrams() = Measurement(
    when (unit) {
        UNIT_MILLIGRAM -> value / 1e3f
        UNIT_MICROGRAM -> value / 1e6f
        UNIT_KILOGRAM, UNIT_LITER -> value * 1e3f
        UNIT_DECILITRE -> value * 1e2f
        UNIT_CENTILITRE -> value * 10f
        UNIT_MILLILITRE, UNIT_GRAM -> value
        UNIT_OZ -> value / OZ_PER_L * 1e3f
        //TODO : what about % DV and IU
        else -> value
    }, UNIT_GRAM
)

private const val KJ_PER_KCAL = 4.184f
private const val SALT_PER_SODIUM = 2.54f
private const val OZ_PER_L = 33.814f

val Measurement.grams get() = convertToGrams()

fun Measurement.convertEnergyTo(targetUnit: MeasurementUnit): Measurement = when {
    unit == targetUnit -> this
    unit == ENERGY_KJ && targetUnit == ENERGY_KCAL -> Measurement(value / KJ_PER_KCAL, targetUnit)
    unit == ENERGY_KCAL && targetUnit == ENERGY_KJ -> Measurement(value * KJ_PER_KCAL, targetUnit)
    else -> throw IllegalArgumentException("Cannot convert from/to NON energy. Use convertTo instead.")
}

fun Measurement.convertTo(unit: MeasurementUnit): Measurement {
    // First convert to grams/ml
    val value = grams.value

    // Then to desired unit
    return Measurement(
        when (unit) {
            ENERGY_KJ, ENERGY_KCAL ->
                throw IllegalArgumentException("Cannot convert from/to energy. Use convertEnergyTo instead.")

            UNIT_DV, UNIT_IU ->
                throw IllegalArgumentException("Cannot convert to DV or IU")

            UNIT_OZ -> value / 1e3f * OZ_PER_L

            UNIT_MICROGRAM -> value * 1e6f
            UNIT_MILLIGRAM -> value * 1e3f

            UNIT_GRAM, UNIT_MILLILITRE -> value // 1g of water == 1ml

            UNIT_CENTILITRE -> value / 10f
            UNIT_DECILITRE -> value / 1e2f
            UNIT_KILOGRAM, UNIT_LITER -> value / 1e3f
        }, unit
    )
}

fun Measurement.displayString() = buildString {
    append(getRoundNumber(value))
    append(" ")
    append(unit.sym)
}

fun Float.saltToSodium() = this / SALT_PER_SODIUM
fun Float.sodiumToSalt() = this * SALT_PER_SODIUM

fun Measurement.saltToSodium() = Measurement(value.saltToSodium(), unit)
fun Measurement.sodiumToSalt() = Measurement(value.sodiumToSalt(), unit)

fun getServingIn(servingSize: String, unit: MeasurementUnit): Measurement? {
    val match = Regex("(\\d+(?:\\.\\d+)?) *(\\w+)").find(servingSize) ?: return null

    val value = match.groupValues[1].toFloat()
    val servingUnit = match.groupValues[2].let { Companion.findBySymbol(it) } ?: return null

    val measurement = Measurement(value, servingUnit)
    return measurement.convertTo(unit)
}

fun getServingInOz(servingSize: String) = getServingIn(servingSize, UNIT_OZ)
fun getServingInL(servingSize: String) = getServingIn(servingSize, UNIT_LITER)