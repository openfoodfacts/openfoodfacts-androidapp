package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import android.widget.Spinner
import android.widget.TextView
import openfoodfacts.github.scrachx.openfood.features.shared.views.CustomValidatingEditTextView

fun CustomValidatingEditTextView.isModifierEqualsToGreaterThan() = modSpinner!!.isModifierEqualsToGreaterThan()

fun Spinner.isModifierEqualsToGreaterThan() = GREATER_THAN == MODIFIERS[selectedItemPosition]

fun TextView.isBlank() = text.toString().isBlank()

fun TextView.isNotBlank() = !isBlank()

/**
 * Retrieve the float value from strings like "> 1.03"
 *
 * @param initText value to parse
 * @return the float value or null if not correct
 */
fun getFloatValue(initText: String?) = getDoubleValue(initText)?.toFloat()

/**
 * @return the float value or null if not correct
 * @see .getFloatValue
 */
fun TextView.getFloatValue(): Float? {
    if (text == null) return null
    return getFloatValue(text.toString())
}

fun TextView.getFloatValueOr(defaultValue: Float) = getFloatValue() ?: defaultValue

/**
 * Retrieve the float value from strings like "> 1.03"
 *
 * @param initText value to parse
 * @return the float value or null if not correct
 */
fun getDoubleValue(initText: String?) = if (initText.isNullOrBlank()) null
else try {
    initText.trim().replace(",", ".").toDouble()
} catch (ex: NumberFormatException) {
    Log.w("Utils", "Can't parse text '$initText'")
    null
}
/**
 * @return the float value or null if not correct
 * @see [getFloatValue]
 */
fun TextView.getDoubleValue() = getDoubleValue(text?.toString())
