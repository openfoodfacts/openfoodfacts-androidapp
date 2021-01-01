package openfoodfacts.github.scrachx.openfood.utils

import android.widget.EditText
import com.hootsuite.nachos.NachoTextView
import openfoodfacts.github.scrachx.openfood.models.Nutriments

fun EditText?.getContent() = this?.text?.toString()

/**
 * @return true if the edit text string value is not empty
 */
fun EditText?.isNotEmpty() = !isEmpty()
fun EditText.isContentDifferent(toCompare: String?): Boolean {
    val fieldValue = this.getContent()
    if (fieldValue.isNullOrEmpty() && toCompare.isNullOrEmpty()) return false
    return fieldValue.isNullOrEmpty() || fieldValue != toCompare
}

/**
 * @return true if the edit text string value is empty or null
 */
fun EditText?.isEmpty() = this.getContent().isNullOrEmpty()

fun NachoTextView.areChipsDifferent(toCompare: List<String>) =
        chipValues.toTypedArray().contentEquals(toCompare.toTypedArray())

fun CustomValidatingEditTextView.hasUnit() = entryName != Nutriments.PH && entryName != Nutriments.ALCOHOL
