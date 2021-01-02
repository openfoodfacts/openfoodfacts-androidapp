package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class QuestionsState(
        @JsonProperty("status") val status: String?,
        @JsonProperty("questions") val questions: List<Question>?
) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}