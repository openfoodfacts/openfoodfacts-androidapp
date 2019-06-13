package openfoodfacts.github.scrachx.openfood.utils;

public class FileUtils {
    public static final String LOCALE_FILE_SCHEME = "file://";

    public static boolean isLocaleFile(String url) {
        return url != null && url.startsWith(LOCALE_FILE_SCHEME);
    }
}
