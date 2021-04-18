package openfoodfacts.github.scrachx.openfood.models

import android.text.SpannableStringBuilder
import androidx.core.text.bold

/**
 * Header with bold values
 * @param title
 * @param value
 * @param servingValue
 * @param unit
 */
class BoldNutrimentListItem(
        title: CharSequence,
        value: CharSequence = "",
        servingValue: CharSequence = "",
        unit: CharSequence = "",
        modifier: CharSequence = ""
) : NutrimentListItem(
        SpannableStringBuilder().bold { append(title) },
        SpannableStringBuilder().bold { append(value) },
        SpannableStringBuilder().bold { append(servingValue) },
        SpannableStringBuilder().bold { append(unit) },
        SpannableStringBuilder().bold { append(modifier) }
)