package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang.builder.ToStringBuilder
import java.io.Serializable
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class NutrientLevels : Serializable {

    var salt: NutrimentLevel? = null

    var fat: NutrimentLevel? = null

    var sugars: NutrimentLevel? = null

    @JsonProperty("saturated-fat")
    var saturatedFat: NutrimentLevel? = null

    @get:JsonAnyGetter
    val additionalProperties: MutableMap<String, Any> = HashMap()

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }

    override fun toString() = ToStringBuilder(this)
            .append(salt)
            .append(fat)
            .append(sugars)
            .append(saturatedFat)
            .append(additionalProperties)
            .toString()

    companion object {
        private const val serialVersionUID = 1L
    }
}