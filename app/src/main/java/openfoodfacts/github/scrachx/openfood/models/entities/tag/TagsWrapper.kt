package openfoodfacts.github.scrachx.openfood.models.entities.tag

import com.fasterxml.jackson.annotation.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("tags")
class TagsWrapper {
    @JsonProperty("tags")
    lateinit var tags: List<Tag>

    @JsonIgnore
    @get:JsonAnyGetter
    val additionalProperties = HashMap<String, Any>()


    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }
}