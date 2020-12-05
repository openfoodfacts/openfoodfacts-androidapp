package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.*
import java.io.Serializable
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
class NutrientLevels : Serializable {

    var salt: NutrimentLevel? = null

    var fat: NutrimentLevel? = null

    var sugars: NutrimentLevel? = null

    @JsonProperty("saturated-fat")
    var saturatedFat: NutrimentLevel? = null

    @JsonIgnore
    private val additionalProperties: MutableMap<String, Any> = HashMap()
    @JsonAnyGetter
    fun getAdditionalProperties(): Map<String, Any> {
        return additionalProperties
    }

    @JsonAnySetter
    fun setAdditionalProperty(name: String, value: Any) {
        additionalProperties[name] = value
    }

    override fun toString(): String {
        return """NutrientLevels{
            |salt='$salt', 
            |fat='$fat', 
            |sugars='$sugars', 
            |saturatedFat='$saturatedFat', 
            |additionalProperties=$additionalProperties
            |}""".trimMargin()
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}