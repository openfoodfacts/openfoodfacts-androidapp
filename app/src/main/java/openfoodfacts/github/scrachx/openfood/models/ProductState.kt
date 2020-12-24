package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.*
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import java.io.Serializable
import java.util.*

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
    /**
     * The status
     */
    var status: Long = 0
    /**
     * The product
     */
    /**
     * The product
     */
    var product: Product? = null
    /**
     * The code
     */
    /**
     * The code
     */
    var code: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any> = HashMap()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return additionalProperties
    }

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