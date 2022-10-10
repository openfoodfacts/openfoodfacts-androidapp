package openfoodfacts.github.scrachx.openfood.models.tagline

import com.fasterxml.jackson.annotation.JsonProperty

data class TagLineLanguage(
    @JsonProperty("language") val language: String,
    @JsonProperty("data") val tagLine: TagLine,
)