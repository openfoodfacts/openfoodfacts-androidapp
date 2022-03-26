package openfoodfacts.github.scrachx.openfood.models

import android.content.Context
import androidx.annotation.StringRes
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
        // If this is null, the product doesn't have this nutriment
        val name = getName(nutriment) ?: return null

        return ProductNutriment(
            nutriment,
            name,
            getValuePer100g(nutriment),
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

    fun getLevelItem(
        context: Context,
        nutriment: Nutriment,
        nutrimentLevel: NutrimentLevel?,
        @StringRes titleRes: Int
    ): NutrientLevelItem? {
        val productNutriment = this[nutriment] ?: return null
        val displayString = productNutriment.per100gDisplayString ?: return null
        val nutrimentLevelStr = nutrimentLevel?.let { context.getString(it) } ?: return null
        val category = context.getString(titleRes)

        return NutrientLevelItem(
            category,
            displayString,
            nutrimentLevelStr,
            nutrimentLevel.getImgRes()
        )
    }

    var hasMinerals = false
        private set

    var hasVitamins = false
        private set


    /**
     * @param nutriment type of nutriment (fat, sugar, etc...)
     */
    class ProductNutriment(
        val nutriment: Nutriment,
        val name: String,
        val per100gInG: Measurement?,
        val perServingInG: Measurement?,
        unit: MeasurementUnit,
        val modifier: Modifier
    ) {

        val unit = unit.getRealUnit()

        fun isEnergy() = unit in ENERGY_UNITS

        val per100gDisplayString by lazy {
            per100gInUnit?.let { measurement ->
                buildString {
                    modifier.ifNotDefault {
                        append(it.sym)
                        append(" ")
                    }
                    append(measurement.displayString())
                }
            }
        }

        /**
         * Returns the amount of nutriment per 100g
         * of product in the units stored in [ProductNutriment.unit]
         */
        val per100gInUnit: Measurement? by lazy { if (isEnergy()) per100gInG else per100gInG?.convertTo(unit) }

        /**
         * Returns the amount of nutriment per serving
         * of product in the units stored in [ProductNutriment.unit].
         *
         * Can be null if [perServingInG] is null.
         */
        val perServingInUnit: Measurement? by lazy { if (isEnergy()) perServingInG else perServingInG?.convertTo(unit) }

    }

}
