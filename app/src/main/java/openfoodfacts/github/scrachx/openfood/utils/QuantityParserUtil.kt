package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import android.widget.Spinner
import android.widget.TextView
import openfoodfacts.github.scrachx.openfood.models.Modifier

val Spinner.modifier: Modifier?
    get() = (selectedItem as? String)?.let { Modifier.findBySymbol(it) }

fun TextView.isBlank() = text.toString().isBlank()

/**
 * @return the float value or null if not correct
 * @see .getFloatValue
 */
fun TextView.getFloatValue(): Float? = getFloatValue(text?.toString())

fun TextView.getFloatValueOr(defaultValue: Float) = getFloatValue() ?: defaultValue

/**
 * Retrieve the float value from strings like "> 1.03"
 *
 * @param initText value to parse
 * @return the float value or null if not correct
 */
fun getFloatValue(initText: String?) = getDoubleValue(initText)?.toFloat()

/**
 * Retrieve the float value from strings like "1.03" or "1,03"
 *
 * @param text value to parse
 * @return the float value or null if not correct
 */
fun getDoubleValue(text: String?) = if (text.isNullOrBlank()) null
else try {
    text.trim().replace(",", ".").toDouble()
} catch (ex: NumberFormatException) {
    Log.w("Utils", "Can't parse text '$text'")
    null
}

/**
 * @return the float value or null if not correct
 * @see [getFloatValue]
 */
fun TextView.getDoubleValue() = getDoubleValue(text?.toString())
