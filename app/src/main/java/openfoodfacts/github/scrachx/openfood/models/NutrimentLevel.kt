package openfoodfacts.github.scrachx.openfood.models

import android.content.Context
import androidx.annotation.IntegerRes
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import openfoodfacts.github.scrachx.openfood.R
import java.util.*

/**
 * @author herau
 */
enum class NutrimentLevel {
    LOW, MODERATE, HIGH;

    @JsonValue
    override fun toString() = name.toLowerCase(Locale.getDefault())

    /**
     * get the localize text of a nutriment level
     * @param context to fetch localised strings
     * @return The localised word for the nutrition amount. If nutritionAmount is neither low,
     * moderate nor high, return nutritionAmount
     */
    fun getLocalize(context: Context) = when (this) {
        LOW -> context.getString(R.string.txtNutritionLevelLow)
        MODERATE -> context.getString(R.string.txtNutritionLevelModerate)
        HIGH -> context.getString(R.string.txtNutritionLevelHigh)
    }

    @IntegerRes
    fun getImageLevel() = when (this) {
        MODERATE -> R.drawable.moderate
        LOW -> R.drawable.low
        HIGH -> R.drawable.high
    }

    companion object {
        @JsonCreator
        fun fromJson(level: String) = if (level.isBlank()) null else valueOf(level.toUpperCase(Locale.getDefault()))
    }
}