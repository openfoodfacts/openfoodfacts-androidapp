package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import android.widget.Spinner
import android.widget.TextView
import org.apache.commons.lang.StringUtils

object QuantityParserUtil {
    @JvmStatic
    fun isModifierEqualsToGreaterThan(view: CustomValidatingEditTextView): Boolean {
        return isModifierEqualsToGreaterThan(view.modSpinner)
    }

    @JvmStatic
    fun isModifierEqualsToGreaterThan(text: Spinner): Boolean {
        return Modifier.GREATER_THAN == Modifier.MODIFIERS[text.selectedItemPosition]
    }

    @JvmStatic
    fun isBlank(editText: TextView): Boolean {
        return StringUtils.isBlank(editText.text.toString())
    }

    fun isNotBlank(editText: TextView): Boolean {
        return !isBlank(editText)
    }

    /**
     * @param editText the textview
     * @return the float value or null if not correct
     * @see .getFloatValue
     */
    @JvmStatic
    fun getFloatValue(editText: TextView): Float? {
        if (editText.text == null) {
            return null
        }
        val text = editText.text.toString()
        return getFloatValue(text)
    }

    @JvmStatic
    fun getFloatValueOrDefault(editText: TextView, defaultValue: Float): Float {
        val res = getFloatValue(editText)
        return res ?: defaultValue
    }

    /**
     * @param editText the textview
     * @return the float value or null if not correct
     * @see .getFloatValue
     */
    @JvmStatic
    fun getDoubleValue(editText: TextView): Double? {
        if (editText.text == null) {
            return null
        }
        val text = editText.text.toString()
        return getDoubleValue(text)
    }

    @JvmStatic
    fun containFloatValue(editText: TextView?): Boolean {
        return editText != null && containFloatValue(editText.text.toString())
    }

    @JvmStatic
    fun containFloatValue(text: String?) = getFloatValue(text) != null

    fun containDoubleValue(text: String?): Boolean {
        return getDoubleValue(text) != null
    }

    /**
     * Retrieve the float value from strings like "> 1.03"
     *
     * @param initText value to parse
     * @return the float value or null if not correct
     */
    @JvmStatic
    fun getFloatValue(initText: String?): Float? {
        val result = getDoubleValue(initText)
        return result?.toFloat()
    }

    /**
     * Retrieve the float value from strings like "> 1.03"
     *
     * @param initText value to parse
     * @return the float value or null if not correct
     */
    fun getDoubleValue(initText: String?): Double? {
        if (StringUtils.isBlank(initText)) {
            return null
        }
        var text = StringUtils.trim(initText)
        text = replaceCommaByDot(text)
        try {
            return text.toDouble()
        } catch (ex: NumberFormatException) {
            Log.d("Utils", "can't parse text: $text")
        }
        return null
    }

    /**
     * For french input "," is used instead of "."
     *
     * @param text
     * @return text with , replaced by .
     */
    private fun replaceCommaByDot(text: String): String {
        if (text.contains(",")) {
            return StringUtils.replace(text, ",", ".")
        }
        return text
    }

}