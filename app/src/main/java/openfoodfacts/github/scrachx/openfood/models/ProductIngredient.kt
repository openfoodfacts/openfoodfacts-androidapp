package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.*
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("text", "id", "rank", "percent")
data class ProductIngredient(
        @JsonProperty("text") val text: String,
        @JsonProperty("id") val id: String,
        /**
         * The rank, set -1 if no rank returned
         */
        @JsonProperty("rank") val rank: Long = 0,
        @JsonProperty("percent") val percent: String? = null
) : Serializable {


    @get:JsonAnyGetter
    val additionalProperties = HashMap<String, Any>()

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }

    override fun toString() = ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("text", text)
            .append("id", id)
            .append("rank", rank)
            .append("percent", percent)
            .append("additionalProperties", additionalProperties)
            .toString()

    companion object {
        private const val serialVersionUID = 1L
    }
}