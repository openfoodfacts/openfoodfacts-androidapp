package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import android.widget.Spinner
import android.widget.TextView

fun isModifierEqualsToGreaterThan(view: CustomValidatingEditTextView) = isModifierEqualsToGreaterThan(view.modSpinner!!)

fun isModifierEqualsToGreaterThan(text: Spinner) = GREATER_THAN == MODIFIERS[text.selectedItemPosition]

fun TextView.isBlank() = text.toString().isBlank()

fun TextView.isNotBlank() = !isBlank()

/**
 * @param this@getFloatValue the textview
 * @return the float value or null if not correct
 * @see .getFloatValue
 */
fun TextView.getFloatValue(): Float? {
    if (text == null) {
        return null
    }
    val text = text.toString()
    return getFloatValue(text)
}

/**
 * For french input "," is used instead of "."
 *
 * @param str
 * @return text with , replaced by .
 */
private fun replaceCommaByDot(str: String) = if (str.contains(",")) str.replace(",", ".") else str

/**
 * Retrieve the float value from strings like "> 1.03"
 *
 * @param initText value to parse
 * @return the float value or null if not correct
 */
fun getDoubleValue(initText: String?): Double? {
    if (initText.isNullOrBlank()) {
        return null
    }
    try {
        return replaceCommaByDot(initText.trim()).toDouble()
    } catch (ex: NumberFormatException) {
        Log.w("Utils", "can't parse text: ${replaceCommaByDot(initText.trim())}")
    }
    return null
}

/**
 * Retrieve the float value from strings like "> 1.03"
 *
 * @param initText value to parse
 * @return the float value or null if not correct
 */
fun getFloatValue(initText: String?) = getDoubleValue(initText)?.toFloat()

fun containFloatValue(text: String?) = getFloatValue(text) != null

fun containDoubleValue(text: String?) = getDoubleValue(text) != null

fun containFloatValue(editText: TextView?) = editText != null && containFloatValue(editText.text.toString())

/**
 * @param editText the textview
 * @return the float value or null if not correct
 * @see .getFloatValue
 */
fun getDoubleValue(editText: TextView): Double? {
    if (editText.text == null) {
        return null
    }
    val text = editText.text.toString()
    return getDoubleValue(text)
}

fun getFloatValueOrDefault(editText: TextView, defaultValue: Float) = editText.getFloatValue() ?: defaultValue