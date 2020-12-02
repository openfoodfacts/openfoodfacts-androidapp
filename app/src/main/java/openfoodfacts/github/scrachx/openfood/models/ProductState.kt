package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.*
import org.apache.commons.lang.builder.ToStringBuilder
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
    fun withStatusVerbose(statusVerbose: String?): ProductState {
        this.statusVerbose = statusVerbose
        return this
    }

    fun withStatus(status: Long): ProductState {
        this.status = status
        return this
    }

    fun withProduct(product: Product?): ProductState {
        this.product = product
        return this
    }

    fun withCode(code: String?): ProductState {
        this.code = code
        return this
    }

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }

    fun withAdditionalProperty(name: String, value: Any): ProductState {
        additionalProperties[name] = value
        return this
    }

    override fun toString() = ToStringBuilder(this)
            .append("statusVerbose", statusVerbose)
            .append("status", status)
            .append("code", code)
            .append("additionalProperties", additionalProperties)
            .toString()

    companion object {
        private const val serialVersionUID = 1L
    }
}