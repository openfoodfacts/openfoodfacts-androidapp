package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.io.Serializable

/**
 * Represents an ingredient of the product
 *
 * @param rank The rank, set -1 if no rank returned
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("text", "id", "rank", "percent")
data class ProductIngredient(
    @JsonProperty("text") val text: String,
    @JsonProperty("id") val id: String,
    @JsonProperty("rank") val rank: Long = 0,
    @JsonProperty("percent") val percent: String? = null,
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