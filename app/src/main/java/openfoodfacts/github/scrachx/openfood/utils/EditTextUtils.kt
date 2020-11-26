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
    @JvmStatic
    fun isEmpty(editText: EditText?) = getContent(editText).isNullOrEmpty()

    /**
     * @return true if the edit text string value is not empty
     */
    @JvmStatic
    fun isNotEmpty(editText: EditText?) = !isEmpty(editText)

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
    fun areChipsDifferent(nachoTextView: NachoTextView, toCompare: MutableList<String>): Boolean {
        val nachoValues = nachoTextView.chipValues
        nachoValues.sortWith { obj, anotherString -> obj.compareTo(anotherString) }
        toCompare.sortWith { obj, anotherString -> obj.compareTo(anotherString) }

        // Using StringUtils because null element -> ""
        val nachoString = nachoTextView.chipValues.joinToString (",")
        val toCompareString = toCompare.joinToString(",")
        return nachoString != toCompareString
    }

    @JvmStatic
    fun hasUnit(editTextView: CustomValidatingEditTextView): Boolean {
        val shortName = editTextView.entryName
        return Nutriments.PH != shortName && Nutriments.ALCOHOL != shortName
    }
}