package openfoodfacts.github.scrachx.openfood.utils

import android.widget.EditText
import com.hootsuite.nachos.NachoTextView
import openfoodfacts.github.scrachx.openfood.features.shared.views.CustomValidatingEditTextView
import openfoodfacts.github.scrachx.openfood.models.Nutriment

fun EditText?.getContent() = this?.text?.toString()

/**
 * @return true if the edit text string value is not empty
 */
fun EditText?.isNotEmpty() = !isEmpty()

fun EditText.isContentDifferent(toCompare: String?): Boolean {
    val fieldValue = getContent()
    return !(fieldValue.isNullOrEmpty() && toCompare.isNullOrEmpty()
            || !fieldValue.isNullOrEmpty() && fieldValue == toCompare)
}

fun EditText.isValueDifferent(toCompare: Float?): Boolean {
    val fieldValue = getContent()?.toFloatOrNull()
    return fieldValue != toCompare
}

/**
 * @return true if the edit text string value is empty or null
 */
fun EditText?.isEmpty() = this.getContent().isNullOrEmpty()

fun NachoTextView.areChipsEquals(toCompare: List<String>) =
    chipValues.toTypedArray() contentEquals toCompare.toTypedArray()

fun NachoTextView.areChipsDifferent(toCompare: List<String>) = !areChipsEquals(toCompare)

fun CustomValidatingEditTextView.hasUnit() = entryName != Nutriment.PH.key && entryName != Nutriment.ALCOHOL.key
