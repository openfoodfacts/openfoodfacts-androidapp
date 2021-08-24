package openfoodfacts.github.scrachx.openfood.utils

import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit
import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit.*
import openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber


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
        UNIT_MILLIGRAM -> value / 1000f
        UNIT_MICROGRAM -> value / 1000000f
        UNIT_KILOGRAM -> value * 1000f
        UNIT_LITER -> value * 1000f
        UNIT_DECILITRE -> value * 100f
        UNIT_CENTILITRE -> value * 10f
        UNIT_MILLILITRE, UNIT_GRAM -> value
        //TODO : what about % DV and IU
        else -> value
    }, UNIT_GRAM
)

object UnitUtils {
    /**
     * Function which returns volume in oz if parameter is in cl, ml, or l
     *
     * @param servingSize value to transform
     * @return volume in oz if servingSize is a volume parameter else return the the parameter unchanged
     */
    fun getServingInOz(servingSize: String): Measurement? {
        val match = Regex("(\\d+(?:[.,]\\d+)?)").find(servingSize) ?: return null
        var value = match.value.toFloat()
        value *= when {
            servingSize.contains("ml", true) -> OZ_PER_L / 1000
            servingSize.contains("cl", true) -> OZ_PER_L / 100
            servingSize.contains("l", true) -> OZ_PER_L
            servingSize.contains("oz", true) -> 1f
            //TODO: HANDLE OTHER CASES, NOT L NOR OZ NOR ML NOR CL
            else -> return null
        }
        return Measurement(value, UNIT_OZ)
    }

    /**
     * Function that returns the volume in liters if input parameter is in oz
     *
     * @param servingSize the value to transform: not null
     * @return volume in liter if input parameter is a volume parameter else return the parameter unchanged
     */
    fun getServingInL(servingSize: String): Measurement? {
        val match = Regex("(\\d+(?:\\.\\d+)?)").find(servingSize) ?: return null
        var value = match.value.toFloat()
        value /= when {
            servingSize.contains("oz", true) -> OZ_PER_L
            servingSize.contains("l", true) -> 1f
            // TODO: HANDLE OTHER CASES eg. not in L nor oz
            else -> return null
        }
        return Measurement(value, UNIT_LITER)
    }
}


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
            UNIT_OZ -> value / 1000f * OZ_PER_L

            UNIT_MICROGRAM -> value * 1000000f
            UNIT_MILLIGRAM -> value * 1000f

            UNIT_GRAM, UNIT_MILLILITRE -> value // 1g of water == 1ml

            UNIT_CENTILITRE -> value / 10f
            UNIT_DECILITRE -> value / 100f
            UNIT_KILOGRAM, UNIT_LITER -> value / 1000f
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