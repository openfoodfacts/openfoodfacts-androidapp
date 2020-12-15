package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.*
import org.apache.commons.lang.builder.ToStringBuilder
import org.apache.commons.lang.builder.ToStringStyle
import java.io.Serializable
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("text", "id", "rank", "percent")
class ProductIngredient : Serializable {
    /**
     * The text
     */
    @JsonProperty("text")
    lateinit var text: String

    /**
     * The id
     */
    @JsonProperty("id")
    lateinit var id: String

    /**
     * The rank, set -1 if no rank returned
     */
    @JsonProperty("rank")
    var rank: Long = 0

    /**
     * The percent
     */
    @JsonProperty("percent")
    var percent: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any> = HashMap()

    @JsonAnyGetter
    fun getAdditionalProperties() = additionalProperties

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