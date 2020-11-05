package openfoodfacts.github.scrachx.openfood.models.entities.attribute

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AttributeGroup(
        @JsonProperty("id") var id: String?,
        @JsonProperty("name") var name: String?,
        @JsonProperty("warning") var warning: String?,
        @JsonProperty("attributes") var attributes: Array<Attribute>?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttributeGroup

        if (id != other.id) return false
        if (name != other.name) return false
        if (warning != other.warning) return false
        if (attributes != null) {
            if (other.attributes == null) return false
            if (!attributes.contentEquals(other.attributes)) return false
        } else if (other.attributes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (warning?.hashCode() ?: 0)
        result = 31 * result + (attributes?.contentHashCode() ?: 0)
        return result
    }
}