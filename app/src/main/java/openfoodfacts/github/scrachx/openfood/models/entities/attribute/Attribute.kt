package openfoodfacts.github.scrachx.openfood.models.entities.attribute

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Attribute(
        @JsonProperty("match") var match: Float,
        @JsonProperty("id") var id: String,
        @JsonProperty("description_short") var descriptionShort: String?,
        @JsonProperty("status") var status: String,
        @JsonProperty("icon_url") var iconUrl: String?,
        @JsonProperty("title") var title: String,
        @JsonProperty("name") var name: String?,
        @JsonProperty("description") var description: String?,
        @JsonProperty("debug") var debug: String?
)