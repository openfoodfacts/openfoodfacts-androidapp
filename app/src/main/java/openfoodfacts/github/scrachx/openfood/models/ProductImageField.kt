package openfoodfacts.github.scrachx.openfood.models

import com.fasterxml.jackson.annotation.JsonValue
import java.util.Locale.ROOT

/**
 * Kind of Product Image
 */
enum class ProductImageField(val apiKey: String) {
    // DO NOT CHANGE ENUM NAMES
    FRONT("front"),
    INGREDIENTS("ingredients"),
    NUTRITION("nutrition"),
    PACKAGING("packaging"),
    OTHER("other");


    @JsonValue
    override fun toString() = name.lowercase(ROOT)
}
