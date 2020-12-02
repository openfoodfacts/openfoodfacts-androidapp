package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonProperty

class TaglineLanguageModel {
    @JsonProperty("language")
    lateinit var language: String

    @JsonProperty("data")
    lateinit var taglineModel: TaglineModel
}