package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.*
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("status_verbose", "status", "product", "code")
class ProductState : Serializable {

    /**
     * The status_verbose
     */
    @JsonProperty("status_verbose")
    var statusVerbose: String? = null

    /**
     * The status
     */
    @JsonProperty("status")
    var status: Long = 0

    /**
     * The product
     */
    @JsonProperty("product")
    var product: Product? = null

    /**
     * The code
     */
    @JsonProperty("code")
    var code: String? = null

    @JsonIgnore
    @get:JsonAnyGetter
    val additionalProperties = mutableMapOf<String, Any>()


    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }

    override fun toString() = ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("statusVerbose", statusVerbose)
            .append("status", status)
            .append("product", product)
            .append("code", code)
            .append("additionalProperties", additionalProperties)
            .toString()

    companion object {
        private const val serialVersionUID = 1L
    }
}