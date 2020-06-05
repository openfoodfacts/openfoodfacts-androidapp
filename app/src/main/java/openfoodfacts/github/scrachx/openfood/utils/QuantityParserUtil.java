package openfoodfacts.github.scrachx.openfood.utils;

import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.lang.StringUtils;

public class QuantityParserUtil {
    public enum EntryFormat {
        //value can start with <>~
        WITH_KNOWN_PREFIX,
        //no prefix
        NO_PREFIX
    }

    private QuantityParserUtil() {

    }

    public static boolean isModifierEqualsToGreaterThan(CustomValidatingEditTextView view) {
        return isModifierEqualsToGreaterThan(view.getModSpinner());
    }

    public static boolean isModifierEqualsToGreaterThan(Spinner text) {
        return Modifier.GREATER_THAN.equals(Modifier.MODIFIERS[text.getSelectedItemPosition()]);
    }

    public static boolean isBlank(TextView editText) {
        return StringUtils.isBlank(editText.getText().toString());
    }

    public static boolean isNotBlank(TextView editText) {
        return !isBlank(editText);
    }

    /**
     * @param editText the textview
     * @return the float value or null if not correct
     * @see #getFloatValue(String)
     */
    public static Float getFloatValue(TextView editText) {
        if (editText.getText() == null) {
            return null;
        }
        final String text = editText.getText().toString();
        return getFloatValue(text);
    }

    public static float getFloatValueOrDefault(TextView editText, float defaultValue) {
        Float res = getFloatValue(editText);
        return res == null ? defaultValue : res;
    }

    /**
     * @param editText the textview
     * @return the float value or null if not correct
     * @see #getFloatValue(String)
     */
    public static Double getDoubleValue(TextView editText) {
        if (editText.getText() == null) {
            return null;
        }
        final String text = editText.getText().toString();
        return getDoubleValue(text);
    }

    public static boolean containFloatValue(TextView editText) {
        return editText != null && containFloatValue(editText.getText().toString());
    }

    public static boolean containFloatValue(String text) {
        return getFloatValue(text) != null;
    }

    public static boolean containDoubleValue(String text) {
        return getDoubleValue(text) != null;
    }

    /**
     * Retrieve the float value from strings like "> 1.03"
     *
     * @param initText value to parse
     * @return the float value or null if not correct
     */
    public static Float getFloatValue(String initText) {
        Double result = getDoubleValue(initText);
        if (result != null) {
            return result.floatValue();
        }
        return null;
    }

    /**
     * Retrieve the float value from strings like "> 1.03"
     *
     * @param initText value to parse
     * @return the float value or null if not correct
     */
    public static Double getDoubleValue(String initText) {
        if (StringUtils.isBlank(initText)) {
            return null;
        }
        String text = StringUtils.trim(initText);
        text = replaceCommonByDot(text);
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            Log.d("Utils", "can't parse text: " + text);
        }
        return null;
    }

    /**
     * For french input "," is used instead of "."
     *
     * @param text
     * @return text with , replaced by .
     */
    private static String replaceCommonByDot(String text) {
        if (text.contains(",")) {
            text = StringUtils.replace(text, ",", ".");
        }
        return text;
    }
}
