package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class Question(
    @JsonProperty("barcode") val code: String? = null,
    @JsonProperty("type") val type: String? = null,
    @JsonProperty("value") val value: String? = null,
    @JsonProperty("question") val questionText: String? = null,
    @JsonProperty("insight_id") val insightId: String? = null,
    @JsonProperty("insight_type") val insightType: String? = null,
    @JsonProperty("source_image_url") val sourceImageUrl: String? = null,
    @JsonProperty("image_url") val imageUrl: String? = null,
) : Serializable {
    @JsonIgnore
    fun isEmpty() = questionText.isNullOrEmpty()

    companion object {
        private const val serialVersionUID = 1L
    }
}