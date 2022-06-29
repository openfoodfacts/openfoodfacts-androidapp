package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.builder.ToStringBuilder
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NutrientLevels(
        @JsonProperty("salt") var salt: NutrimentLevel? = null,
        @JsonProperty("fat") var fat: NutrimentLevel? = null,
        @JsonProperty("sugars") var sugars: NutrimentLevel? = null,
        @JsonProperty("saturated-fat") var saturatedFat: NutrimentLevel? = null,
) : Serializable {

    @get:JsonAnyGetter
    val additionalProperties = HashMap<String, Any>()

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