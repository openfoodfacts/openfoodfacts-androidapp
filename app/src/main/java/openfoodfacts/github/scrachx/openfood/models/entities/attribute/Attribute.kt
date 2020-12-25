package openfoodfacts.github.scrachx.openfood.models.entities.attribute

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Attribute(
        @JsonProperty("match") val match: Float,
        @JsonProperty("id") val id: String,
        @JsonProperty("description_short") val descriptionShort: String?,
        @JsonProperty("status") val status: String,
        @JsonProperty("icon_url") val iconUrl: String?,
        @JsonProperty("title") val title: String?,
        @JsonProperty("name") val name: String?,
        @JsonProperty("description") val description: String?,
        @JsonProperty("debug") val debug: String?
)