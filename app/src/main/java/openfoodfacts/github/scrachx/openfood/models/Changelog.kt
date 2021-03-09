package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class Changelog(
        @JsonProperty("changelog") val versions: List<ChangelogVersion>,
) : Serializable

data class ChangelogVersion(
        @JsonProperty("date") val date: String,
        @JsonProperty("code") val code: Long,
        @JsonProperty("name") val name: String,
        @JsonProperty("items") val items: List<String>,
) : Serializable
