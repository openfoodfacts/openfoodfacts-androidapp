package openfoodfacts.github.scrachx.openfood.utils

import android.text.TextUtils
import android.widget.EditText
import com.hootsuite.nachos.NachoTextView
import openfoodfacts.github.scrachx.openfood.models.Nutriments

object EditTextUtils {
    @JvmStatic
    fun getContent(editText: EditText?): String? {
        return if (editText == null || editText.text == null) {
            null
        } else editText.text.toString()
    }

    /**
     * @return true if the edit text string value is empty
     */
    fun EditText?.isEmpty() = getContent(this).isNullOrEmpty()

    /**
     * @return true if the edit text string value is not empty
     */
    fun EditText?.isNotEmpty() = !isEmpty()

    @JvmStatic
    fun isDifferent(textView: EditText, toCompare: String?): Boolean {
        val fieldValue = getContent(textView)
        if (TextUtils.isEmpty(fieldValue) && TextUtils.isEmpty(toCompare)) {
            return false
        }
        return if (TextUtils.isEmpty(fieldValue)) {
            true
        } else fieldValue != toCompare
    }

    @JvmStatic
    fun areChipsDifferent(nachoTextView: NachoTextView, toCompare: List<String>): Boolean {
        return nachoTextView.chipValues.toTypedArray().contentEquals(toCompare.toTypedArray())
    }

    @JvmStatic
    fun hasUnit(editTextView: CustomValidatingEditTextView): Boolean {
        val shortName = editTextView.entryName
        return Nutriments.PH != shortName && Nutriments.ALCOHOL != shortName
    }
}