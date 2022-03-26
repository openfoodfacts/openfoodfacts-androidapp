package openfoodfacts.github.scrachx.openfood.models

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
    override fun toString() = name.lowercase(Locale.ROOT)

    @StringRes
    fun getDescRes() = when (this) {
        LOW -> R.string.txtNutritionLevelLow
        MODERATE -> R.string.txtNutritionLevelModerate
        HIGH -> R.string.txtNutritionLevelHigh
    }

    @DrawableRes
    fun getImgRes() = when (this) {
        MODERATE -> R.drawable.moderate
        LOW -> R.drawable.low
        HIGH -> R.drawable.high
    }

    companion object {
        @JsonCreator
        fun fromJson(level: String) = if (level.isNotBlank()) valueOf(level.uppercase(Locale.getDefault())) else null
    }
}

/**
 * Get the localized text of a nutriment level
 *
 * @receiver to fetch localised strings
 * @return The localised word for the nutrition amount.
 */
fun Context.getString(nutrimentLevel: NutrimentLevel) = getString(nutrimentLevel.getDescRes())