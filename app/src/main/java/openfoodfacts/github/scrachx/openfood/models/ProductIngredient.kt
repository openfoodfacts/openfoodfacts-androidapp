package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.*
import org.apache.commons.lang.builder.ToStringBuilder
import java.io.Serializable
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("text", "id", "rank", "percent")
class ProductIngredient : Serializable {
    /**
     * The text
     */
    var text: String? = null

    /**
     * The id
     */
    var id: String? = null

    /**
     * The rank, set -1 if no rank returned
     */
    var rank: Long = 0

    /**
     * The percent
     */
    var percent: String? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any> = HashMap()
    fun withText(text: String?): ProductIngredient {
        this.text = text
        return this
    }

    fun withId(id: String?): ProductIngredient {
        this.id = id
        return this
    }

    fun withRank(rank: Long): ProductIngredient {
        this.rank = rank
        return this
    }

    fun withPercent(percent: String?): ProductIngredient {
        this.percent = percent
        return this
    }

    @JsonAnyGetter
    fun getAdditionalProperties() = additionalProperties

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }

    fun withAdditionalProperty(name: String, value: Any): ProductIngredient {
        additionalProperties[name] = value
        return this
    }

    override fun toString() = ToStringBuilder(this)
            .append("text", text)
            .append("id", id)
            .append("rank", rank)
            .append("percent", percent)
            .append("additionalProperties", additionalProperties)
            .toString()

    companion object {
        private const val serialVersionUID = 1L
    }
}