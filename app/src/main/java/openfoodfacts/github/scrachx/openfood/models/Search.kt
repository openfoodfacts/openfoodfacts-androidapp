package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.*
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("page_size", "count", "skip", "page", "products")
data class Search(
        @JsonProperty("page_size") val pageSize: String,
        @JsonProperty("count") val count: String,
        @JsonProperty("skip") val skip: Int,
        @JsonProperty("page") val page: Int,
        @JsonProperty("products") val products: List<SearchProduct> = arrayListOf()
) : Serializable {

    @JsonIgnore
    @get:JsonAnyGetter
    val additionalProperties = hashMapOf<String, Any?>()

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any?) {
        additionalProperties[name] = value
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}