package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class TagLineLanguage(
        @JsonProperty("language") val language: String,
        @JsonProperty("data") val tagLine: TagLine
)

data class TagLine(
        @JsonProperty("message") val message: String? = null,
        @JsonProperty("url") val url: String? = null
) : Serializable
