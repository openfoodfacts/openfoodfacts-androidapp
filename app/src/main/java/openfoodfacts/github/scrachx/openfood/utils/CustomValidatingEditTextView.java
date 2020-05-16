package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.textfield.TextInputLayout;

import openfoodfacts.github.scrachx.openfood.R;

public class CustomValidatingEditTextView extends AppCompatEditText {
    private TextInputLayout textInputLayout;
    private int attachedModSpinnerId = NO_ID;
    private int attachedUnitSpinnerId = NO_ID;
    private int textInputLayoutId = NO_ID;
    private Spinner modSpinner;
    private Spinner unitSpinner;
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
        return entryName == null ? getResources().getResourceEntryName(getId()) : entryName;
    }

    void initAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray attributeArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CustomValidatingEditTextView
        );
        textInputLayoutId = attributeArray.getResourceId(R.styleable.CustomValidatingEditTextView_parentTextInputLayout, NO_ID);
        attachedUnitSpinnerId = attributeArray.getResourceId(R.styleable.CustomValidatingEditTextView_attachedUnitSpinner, NO_ID);
        attachedModSpinnerId = attributeArray.getResourceId(R.styleable.CustomValidatingEditTextView_attachedModSpinner, NO_ID);
        attributeArray.recycle();
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

    public boolean isValid() {
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
                    String.format("the id %d used in parentTextInputLayout should be linked to a TextInputLayout and not to %s", textInputLayoutId, attachedTo));
            }
        }
        return textInputLayout;
    }

    public Spinner getUnitSpinner() {
        if (unitSpinner == null && attachedUnitSpinnerId != NO_ID) {
            View view = getRootView().findViewById(attachedUnitSpinnerId);
            if (view instanceof Spinner) {
                unitSpinner = (Spinner) view;
            } else {
                //configuration error we reset the id
                attachedUnitSpinnerId = NO_ID;
                String attachedTo = view == null ? "null" : view.getClass().getName();
                Log.e(CustomValidatingEditTextView.class.getSimpleName(),
                    String.format("the id %d used in attachedSpinner  should be linked to a Spinner and not to %s", attachedUnitSpinnerId, attachedTo));
            }
        }
        return unitSpinner;
    }

    public void setUnitSpinner(Spinner unitSpinner) {
        this.unitSpinner = unitSpinner;
    }

    public Spinner getModSpinner() {
        if (modSpinner == null && attachedModSpinnerId != NO_ID) {
            View view = getRootView().findViewById(attachedModSpinnerId);
            if (view instanceof Spinner) {
                modSpinner = (Spinner) view;
            } else {
                //configuration error we reset the id
                attachedModSpinnerId = NO_ID;
                String attachedTo = view == null ? "null" : view.getClass().getName();
                Log.e(CustomValidatingEditTextView.class.getSimpleName(),
                    String.format("the id %d used in attachedSpinner  should be linked to a Spinner and not to %s", attachedUnitSpinnerId, attachedTo));
            }
        }
        return modSpinner;
    }
}
