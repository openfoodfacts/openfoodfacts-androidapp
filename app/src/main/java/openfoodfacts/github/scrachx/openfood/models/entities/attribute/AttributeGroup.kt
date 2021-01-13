package openfoodfacts.github.scrachx.openfood.models.entities.attribute

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AttributeGroup(
        @JsonProperty("id") val id: String?,
        @JsonProperty("name") val name: String?,
        @JsonProperty("warning") val warning: String?,
        @JsonProperty("attributes") val attributes: List<Attribute>?,
)