package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class NumberParserUtils {
    private NumberParserUtils() {

    }

    public static float getAsFloat(@Nullable Object in, float defaultValue) {
        if (in == null) {
            return defaultValue;
        }
        if (in instanceof Number) {
            return ((Number) in).floatValue();
        }
        return (float) parseDouble(in.toString(), defaultValue);
    }

    public static float getAsFloat(@Nullable Map<String, ?> imgDetails, @Nullable String key, float defaultValue) {
        if (imgDetails == null || key == null) {
            return defaultValue;
        }
        return NumberParserUtils.getAsFloat(imgDetails.get(key),defaultValue);
    }

    public static int getAsInt(@Nullable Map<String, ?> imgDetails, @Nullable String key, int defaultValue) {
        if (imgDetails == null || key == null) {
            return defaultValue;
        }
        return NumberParserUtils.getAsInt(imgDetails.get(key),defaultValue);
    }

    public static int getAsInt(@Nullable Object in, int defaultValue) {
        if (in == null) {
            return defaultValue;
        }
        if (in instanceof Number) {
            return ((Number) in).intValue();
        }
        return (int) parseDouble(in.toString(), defaultValue);
    }

    private static double parseDouble(@Nullable String in, double defaultValue) {
        if (StringUtils.isBlank(in)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(in);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
