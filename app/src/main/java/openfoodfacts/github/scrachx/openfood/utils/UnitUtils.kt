package openfoodfacts.github.scrachx.openfood.utils

import openfoodfacts.github.scrachx.openfood.models.Units
import openfoodfacts.github.scrachx.openfood.models.Units.ENERGY_KCAL
import openfoodfacts.github.scrachx.openfood.models.Units.ENERGY_KJ
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_CENTILITRE
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_DECILITRE
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_KILOGRAM
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_LITER
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MICROGRAM
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MILLIGRAM
import openfoodfacts.github.scrachx.openfood.models.Units.UNIT_MILLILITRE
import openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber
import java.util.*
import java.util.regex.Pattern

object UnitUtils {
    const val UNIT_IU = "IU"
    private const val SALT_PER_SODIUM = 2.54
    private const val KJ_PER_KCAL = 4.184f
    private const val OZ_PER_L = 33.814f

    /**
     * Converts a give quantity's unit to kcal
     *
     * @param value The value to be converted
     * @param originalUnit [Units.ENERGY_KCAL] or [Units.ENERGY_KJ]
     * @return return the converted value
     */
    fun convertToKiloCalories(value: Int, originalUnit: String) = when {
        originalUnit.equals(ENERGY_KJ, true) -> (value / KJ_PER_KCAL).toInt()
        originalUnit.equals(ENERGY_KCAL, true) -> value
        else -> throw IllegalArgumentException("energyUnit is neither Units.ENERGY_KCAL nor Units.ENERGY_KJ")
    }

    fun convertToGrams(value: Float, unit: String?) = convertToGrams(value.toDouble(), unit).toFloat()

    /**
     * Converts a given quantity's unitOfValue to grams.
     *
     * @param value The value to be converted
     * @param unitOfValue must be a unit from [Units]
     * @return return the converted value
     */
    fun convertToGrams(value: Double, unitOfValue: String?) = when {
        UNIT_MILLIGRAM.equals(unitOfValue, true) -> value / 1000
        UNIT_MICROGRAM.equals(unitOfValue, true) -> value / 1000000
        UNIT_KILOGRAM.equals(unitOfValue, true) -> value * 1000
        UNIT_LITER.equals(unitOfValue, true) -> value * 1000
        UNIT_DECILITRE.equals(unitOfValue, true) -> value * 100
        UNIT_CENTILITRE.equals(unitOfValue, true) -> value * 10
        UNIT_MILLILITRE.equals(unitOfValue, true) -> value
        //TODO : what about % DV and IU
        else -> value
    }

    fun convertFromGram(valueInGramOrMl: Float, targetUnit: String?) =
            convertFromGram(valueInGramOrMl.toDouble(), targetUnit).toFloat()

    fun convertFromGram(valueInGramOrMl: Double, targetUnit: String?) = when (targetUnit) {
        UNIT_KILOGRAM, UNIT_LITER -> valueInGramOrMl / 1000
        UNIT_MILLIGRAM -> valueInGramOrMl * 1000
        UNIT_MICROGRAM -> valueInGramOrMl * 1000000
        UNIT_DECILITRE -> valueInGramOrMl / 100
        UNIT_CENTILITRE -> valueInGramOrMl / 10
        else -> valueInGramOrMl
    }

    fun saltToSodium(saltValue: Double) = saltValue / SALT_PER_SODIUM

    fun sodiumToSalt(sodiumValue: Double) = sodiumValue * SALT_PER_SODIUM

    /**
     * Function which returns volume in oz if parameter is in cl, ml, or l
     *
     * @param servingSize value to transform
     * @return volume in oz if servingSize is a volume parameter else return the the parameter unchanged
     */
    fun getServingInOz(servingSize: String, locale: Locale = Locale.getDefault()): String {
        val regex = Pattern.compile("(\\d+(?:\\.\\d+)?)")
        val matcher = regex.matcher(servingSize)
        matcher.find()
        var value = matcher.group(1)!!.toFloat()
        value *= when {
            servingSize.contains("ml", true) -> OZ_PER_L / 1000
            servingSize.contains("cl", true) -> OZ_PER_L / 100
            servingSize.contains("l", true) -> OZ_PER_L
            servingSize.contains("oz", true) -> 1f
            //TODO: HANDLE OTHER CASES, NOT L NOR OZ NOR ML NOR CL
            else -> return servingSize
        }
        return "${getRoundNumber(value, locale)} oz"
    }

    /**
     * Function that returns the volume in liters if input parameter is in oz
     *
     * @param servingSize the value to transform: not null
     * @return volume in liter if input parameter is a volume parameter else return the parameter unchanged
     */
    fun getServingInL(servingSize: String, locale: Locale = Locale.getDefault()): String {
        val regex = Pattern.compile("(\\d+(?:\\.\\d+)?)")
        val matcher = regex.matcher(servingSize)
        matcher.find()
        var value = matcher.group(1)!!.toFloat()
        value /= when {
            servingSize.contains("oz", true) -> OZ_PER_L
            servingSize.contains("l", true) -> 1f
            // TODO: HANDLE OTHER CASES eg. not in L nor oz
            else -> return servingSize
        }
        return "${getRoundNumber(value, locale)} l"
    }
}