package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.io.Serializable

/**
 * Represents an ingredient of the product
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("text", "id", "rank", "percent")
data class ProductIngredient(
    @JsonProperty("text") val text: String? = null,
    @JsonProperty("id") val id: String,
    @JsonProperty("rank") val rank: Long = -1,
    @JsonProperty("percent") val percent: String? = null,
    @JsonProperty("percent_estimate") val percentEstimate: Float? = null,
    @JsonProperty("percent_min") val percentMin: Float? = null,
    @JsonProperty("percent_max") val percentMax: Float? = null,
    @JsonProperty("vegan") val vegan: String? = null,
    @JsonProperty("vegetarian") val vegetarian: String? = null,
    @JsonProperty("has_sub_ingredients") val hasSubIngredients: String? = null,
) : Serializable {


    @get:JsonAnyGetter
    val additionalProperties = HashMap<String, Any>()

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}