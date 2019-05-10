package openfoodfacts.github.scrachx.openfood.utils;

import android.util.Log;
import android.widget.TextView;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

public class QuantityParserUtil {
    public enum EntryFormat {
        //value can start with <>~
        WITH_KNOWN_PREFIX,
        //no prefix
        NO_PREFIX
    }

    private static final char[] PREFIX = new char[]{'<', '>', '~'};

    private QuantityParserUtil() {

    }

    /**
     * @param text init text
     */
    public static String getModifier(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        char firstChar = text.trim().charAt(0);
        if (ArrayUtils.contains(PREFIX, firstChar)) {
            return CharUtils.toString(firstChar);
        }
        return null;
    }

    /**
     * @param text init text
     */
    public static String getModifier(TextView text) {
        if (text.getText() == null) {
            return null;
        }
        return getModifier(text.getText().toString());
    }

    public static boolean isModifierEqualsToGreaterThan(TextView text) {
        return isModifierEqualsToGreaterThan(text.getText().toString());
    }

    public static boolean isModifierEqualsToGreaterThan(String text) {
        return ">".equals(getModifier(text));
    }

    public static boolean isBlank(TextView editText) {
        return StringUtils.isBlank(editText.getText().toString());
    }

    public static boolean isNotBlank(TextView editText) {
        return !isBlank(editText);
    }

    /**
     * @param editText the textview
     * @param entryFormat to remove prefix if present
     * @return the float value or null if not correct
     * @see #getFloatValue(String, EntryFormat)
     */
    public static Float getFloatValue(TextView editText, EntryFormat entryFormat) {
        if (editText.getText() == null) {
            return null;
        }
        final String text = editText.getText().toString();
        return getFloatValue(text, entryFormat);
    }

    public static float getFloatValueOrDefault(TextView editText, EntryFormat entryFormat, float defaultValue) {
        Float res = getFloatValue(editText, entryFormat);
        return res == null ? defaultValue : res;
    }

    /**
     * @param editText the textview
     * @param entryFormat to remove prefix if present
     * @return the float value or null if not correct
     * @see #getFloatValue(String, EntryFormat)
     */
    public static Double getDoubleValue(TextView editText, EntryFormat entryFormat) {
        if (editText.getText() == null) {
            return null;
        }
        final String text = editText.getText().toString();
        return getDoubleValue(text, entryFormat);
    }

    public static boolean containFloatValue(TextView editText, EntryFormat entryFormat) {
        return editText != null && containFloatValue(editText.getText().toString(), entryFormat);
    }

    public static boolean containFloatValue(String text, EntryFormat floatFormat) {
        return getFloatValue(text, floatFormat) != null;
    }

    public static boolean containDoubleValue(String text, EntryFormat floatFormat) {
        return getDoubleValue(text, floatFormat) != null;
    }

    /**
     * Retrieve the float value from strings like "> 1.03"
     *
     * @param initText value to parse
     * @param floatFormat to specify if a prefix (<>~) can be present
     * @return the float value or null if not correct
     */
    public static Float getFloatValue(String initText, EntryFormat floatFormat) {
        Double result = getDoubleValue(initText, floatFormat);
        if (result != null) {
            return Float.valueOf(result.floatValue());
        }
        return null;
    }

    /**
     * Retrieve the float value from strings like "> 1.03"
     *
     * @param initText value to parse
     * @param floatFormat to specify if a prefix (<>~) can be present
     * @return the float value or null if not correct
     */
    public static Double getDoubleValue(String initText, EntryFormat floatFormat) {
        if (StringUtils.isBlank(initText)) {
            return null;
        }
        String text = StringUtils.trim(initText);
        if (EntryFormat.WITH_KNOWN_PREFIX.equals(floatFormat)) {
            text = removeKnowPrefix(text);
        }
        text = replaceCommonByDot(text);
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            Log.d("Utils", "can't parse text: " + text);
        }
        return null;
    }

    private static String removeKnowPrefix(String text) {
        char first = text.charAt(0);
        if (ArrayUtils.contains(PREFIX, first)) {
            text = text.substring(1).trim();
        }
        return text;
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
