package openfoodfacts.github.scrachx.openfood.models.tagline

import com.fasterxml.jackson.annotation.JsonProperty

data class TagLine(
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("url") val url: String? = null,
)
