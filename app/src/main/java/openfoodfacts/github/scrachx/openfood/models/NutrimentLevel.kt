package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.util.*

/**
 * @author herau
 */
enum class NutrimentLevel {
    LOW, MODERATE, HIGH;

    @JsonValue
    override fun toString() = name.lowercase(Locale.ROOT)

    companion object {
        @JsonCreator
        fun fromJson(level: String): NutrimentLevel? {
            if (level.isBlank()) return null
            return valueOf(level.uppercase())
        }
    }
}