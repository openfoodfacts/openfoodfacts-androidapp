package openfoodfacts.github.scrachx.openfood.features.changelog

import com.fasterxml.jackson.annotation.JsonProperty

data class Changelog(
        @JsonProperty("changelog") val versions: List<Version>,
) {
    data class Version(
            @JsonProperty("date") var date: String,
            @JsonProperty("code") var code: Long,
            @JsonProperty("name") var name: String,
            @JsonProperty("items") var items: List<String>,
    )
}
