package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import openfoodfacts.github.scrachx.openfood.R;

public class CustomValidatingEditTextView extends AppCompatEditText {
    private TextInputLayout textInputLayout;
    private Spinner attachedSpinner;
    private int textInputLayoutId = NO_ID;
    private int attachedSpinnerId = NO_ID;
    private String entryName;

    public CustomValidatingEditTextView(Context context) {
        super(context);
    }

    public CustomValidatingEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public CustomValidatingEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public String getEntryName() {
        return entryName==null?getResources().getResourceEntryName(getId()):entryName;
    }

    void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.CustomValidatingEditView);
            textInputLayoutId = attributeArray.getResourceId(R.styleable.CustomValidatingEditView_parentTextInputLayout, NO_ID);
            attachedSpinnerId = attributeArray.getResourceId(R.styleable.CustomValidatingEditView_attachedSpinner, NO_ID);
            attributeArray.recycle();
        }
    }


    public void showError(String message) {
        final TextInputLayout currentTil = getTextInputLayout();
        if (currentTil == null) {
            Log.e(CustomValidatingEditTextView.class.getSimpleName(), "can show outbound error message as the TextInputLayout is null");
        } else {
            currentTil.setErrorEnabled(true);
            currentTil.setError(message);
        }
    }

    public void cancelError() {
        if (textInputLayout != null) {
            textInputLayout.setErrorEnabled(false);
            textInputLayout.setError(null);
        }
    }

    public boolean hasError() {
        return textInputLayout != null && textInputLayout.getError() != null;
    }

    public boolean isValid(){
        return !hasError();
    }

    public void setTextInputLayout(TextInputLayout textInputLayout) {
        this.textInputLayout = textInputLayout;
    }

    private TextInputLayout getTextInputLayout() {
        if (textInputLayout == null && textInputLayoutId != NO_ID) {
            View view = getRootView().findViewById(textInputLayoutId);
            if (view instanceof TextInputLayout) {
                textInputLayout = (TextInputLayout) view;
            } else {
                //configuration error we reset the id
                textInputLayoutId = NO_ID;
                String attachedTo = view == null ? "null" : view.getClass().getName();
                Log.e(CustomValidatingEditTextView.class.getSimpleName(),
                    String.format("the id {0} used in parentTextInputLayout should be linked to a TextInputLayout and not to {1}", textInputLayoutId, attachedTo));
            }
        }
        return textInputLayout;
    }

    public Spinner getAttachedSpinner() {
        if (attachedSpinner == null && attachedSpinnerId != NO_ID) {
            View view = getRootView().findViewById(attachedSpinnerId);
            if (view instanceof Spinner) {
                attachedSpinner = (Spinner) view;
            } else {
                //configuration error we reset the id
                attachedSpinnerId = NO_ID;
                String attachedTo = view == null ? "null" : view.getClass().getName();
                Log.e(CustomValidatingEditTextView.class.getSimpleName(),
                    String.format("the id {0} used in attachedSpinner  should be linked to a Spinner and not to {1}", attachedSpinnerId, attachedTo));
            }
        }
        return attachedSpinner;
    }

    public void setAttachedSpinner(Spinner attachedSpinner) {
        this.attachedSpinner = attachedSpinner;
    }

}
