package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.*
import java.io.Serializable
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("page_size", "count", "skip", "page", "products")
data class Search(
        /**
         * @param pageSize
         * The page_size
         */
        @JsonProperty("page_size") val pageSize: String,
        /**
         * @param count
         * The count
         */
        @JsonProperty("count") val count: String,
        /**
         * The skip
         */
        @JsonProperty("skip") val skip: Int,
        /**
         * The page
         */
        @JsonProperty("page") val page: Int,
        /**
         * The products
         */
        @JsonProperty("products") val products: List<Product> = arrayListOf()
) : Serializable {

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any> = hashMapOf()

    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}