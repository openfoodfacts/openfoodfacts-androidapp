package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Spinner
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import openfoodfacts.github.scrachx.openfood.R

class CustomValidatingEditTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private val textInputLayout: TextInputLayout? by lazy {
        if (textInputLayoutId != NO_ID) {
            val view = rootView.findViewById<View>(textInputLayoutId)
            if (view is TextInputLayout) {
                return@lazy view
            } else {
                //configuration error we reset the id
                textInputLayoutId = NO_ID
                val attachedTo = if (view == null) "null" else view.javaClass.simpleName
                error("The id $textInputLayoutId used in parentTextInputLayout should be linked to a TextInputLayout and not to $attachedTo")
            }
        }
        return@lazy null
    }
    private var attachedModSpinnerId = NO_ID
    private var attachedUnitSpinnerId = NO_ID
    private var textInputLayoutId = NO_ID

    //configuration error we reset the id
    val modSpinner: Spinner? by lazy {
        val view = rootView.findViewById<View>(attachedModSpinnerId)
        if (view is Spinner) {
            return@lazy view
        } else {
            //configuration error we reset the id
            attachedModSpinnerId = NO_ID
            val attachedTo = if (view == null) "null" else view.javaClass.simpleName
            Log.e(CustomValidatingEditTextView::class.simpleName, "The id $attachedUnitSpinnerId used in attachedSpinner should be linked to a Spinner and not to $attachedTo")
        }
        return@lazy null
    }


    //configuration error we reset the id
    val unitSpinner: Spinner? by lazy {
        if (attachedUnitSpinnerId != NO_ID) {
            val view = rootView.findViewById<View>(attachedUnitSpinnerId)
            if (view is Spinner) {
                return@lazy view
            } else {
                //configuration error we reset the id
                attachedUnitSpinnerId = NO_ID
                val attachedTo = if (view == null) "null" else view.javaClass.name
                error("The id $attachedUnitSpinnerId used in attachedSpinner  should be linked to a Spinner and not to $attachedTo")
            }
        }
        return@lazy null
    }

    var entryName: String = resources.getResourceEntryName(id)

    private var fieldName: String? = null


    fun showError(message: String?) {
        val currentTil = textInputLayout
        if (currentTil == null) {
            Log.e(CustomValidatingEditTextView::class.java.simpleName, "can show outbound error message as the TextInputLayout is null")
        } else {
            currentTil.isErrorEnabled = true
            currentTil.error = message
        }
    }

    fun cancelError() {
        textInputLayout?.isErrorEnabled = false
        textInputLayout?.error = null
    }

    fun isError() = textInputLayout != null && textInputLayout!!.error != null

    fun isValid() = !isError()

    init {
        if (attrs != null) {
            val attributeArray = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.CustomValidatingEditTextView
            )
            textInputLayoutId = attributeArray.getResourceId(R.styleable.CustomValidatingEditTextView_parentTextInputLayout, NO_ID)
            attachedUnitSpinnerId = attributeArray.getResourceId(R.styleable.CustomValidatingEditTextView_attachedUnitSpinner, NO_ID)
            attachedModSpinnerId = attributeArray.getResourceId(R.styleable.CustomValidatingEditTextView_attachedModSpinner, NO_ID)
            fieldName = attributeArray.getString(R.styleable.CustomValidatingEditTextView_fieldName)
            attributeArray.recycle()
        }
    }


}