package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonInclude
import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit.UNIT_GRAM
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Suffix
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.Serializable
import java.util.*

/**
 * JSON representation of the product `nutriments` entry.
 *
 * See [JSON Structure](http://en.wiki.openfoodfacts.org/API.JSON_interface)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class ProductNutriments : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    @get:JsonAnyGetter
    val additionalProperties = HashMap<String, Any?>()

    fun getEnergyKcalValue(perServing: Boolean) =
        if (perServing) getValuePerServing(Nutriment.ENERGY_KCAL)
        else getValuePer100g(Nutriment.ENERGY_KCAL)

    fun getEnergyKjValue(isDataPerServing: Boolean) =
        if (isDataPerServing) getValuePerServing(Nutriment.ENERGY_KJ)
        else getValuePer100g(Nutriment.ENERGY_KJ)

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any?) {
        additionalProperties[name] = value
        if (VITAMINS_MAP.any { it.key.key == name }) {
            hasVitamins = true
        } else if (MINERALS_MAP.any { it.key.key == name }) {
            hasMinerals = true
        }
    }

    operator fun get(nutriment: Nutriment): ProductNutriment? {
        val name = getName(nutriment)

        return if (name == null) null
        else ProductNutriment(
            nutriment,
            name,
            getValuePer100g(nutriment)!!,
            getValuePerServing(nutriment),
            getUnit(nutriment),
            getModifier(nutriment)
        )
    }


    private fun getName(nutriment: Nutriment) = getAdditionalProperty(nutriment)

    private fun getValuePerServing(nutriment: Nutriment) =
        getAdditionalProperty(nutriment, Suffix.SERVING)
            ?.takeIf { it.isNotEmpty() }
            ?.toFloat()
            ?.let { measure(it, UNIT_GRAM) }

    /**
     * @return null if the product is missing a value for the specified nutriment.
     */
    private fun getValuePer100g(nutriment: Nutriment) =
        getAdditionalProperty(nutriment, Suffix.VALUE_100G)
            ?.toFloatOrNull()
            ?.let { measure(it, UNIT_GRAM) }

    private fun getUnit(nutriment: Nutriment) = getAdditionalProperty(nutriment, Suffix.UNIT)
        ?.let { MeasurementUnit.findBySymbol(it) } ?: DEFAULT_UNIT

    private fun getModifier(nutriment: Nutriment) = getAdditionalProperty(nutriment, Suffix.MODIFIER)
        ?.let { Modifier.findBySymbol(it) } ?: DEFAULT_MODIFIER


    private fun getAdditionalProperty(nutrient: Nutriment, suffix: String = "") =
        additionalProperties[nutrient.key + suffix]?.toString()

    operator fun contains(nutrimentName: String) = additionalProperties.containsKey(nutrimentName)
    operator fun contains(nutriment: Nutriment) = additionalProperties.containsKey(nutriment.key)


    var hasMinerals = false
        private set

    var hasVitamins = false
        private set


    /**
     * @param perServingInG can be null if the product serving size is not specified
     */
    class ProductNutriment internal constructor(
        val nutriment: Nutriment,
        val name: String,
        val per100gInG: Measurement,
        val perServingInG: Measurement?,
        unit: MeasurementUnit,
        val modifier: Modifier
    ) {
        val unit = unit.getRealUnit()

        fun isEnergy() = unit in ENERGY_UNITS

        fun getPer100gDisplayString() = buildString {
            modifier.ifNotDefault {
                append(it.sym)
                append(" ")
            }
            append(per100gInUnit.displayString())
        }

        /**
         * Returns the amount of nutriment per 100g
         * of product in the units stored in [ProductNutriment.unit]
         */
        val per100gInUnit: Measurement
            get() {
                return if (isEnergy()) per100gInG
                else per100gInG.convertTo(unit)
            }

        /**
         * Returns the amount of nutriment per serving
         * of product in the units stored in [ProductNutriment.unit].
         *
         * Can be null if [perServingInG] is null.
         */
        val perServingInUnit: Measurement?
            get() {
                return if (isEnergy()) perServingInG
                else perServingInG?.convertTo(unit)
            }

        /**
         * Calculates the nutriment value for a given amount of this product. For example,
         * calling getForAnyValue(1, "kg") will give you the amount of this nutriment
         * given 1 kg of the product.
         *
         * @param portion a measurement of the portion
         * @return a nutrient measurement for a the given amount of this product
         */
        fun getForPortion(portion: Measurement) = Measurement(
            value = per100gInUnit.value / 100 * portion.grams.value,
            unit = per100gInUnit.unit
        )
    }

}
