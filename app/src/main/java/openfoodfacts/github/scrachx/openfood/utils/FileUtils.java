package openfoodfacts.github.scrachx.openfood.utils;

import openfoodfacts.github.scrachx.openfood.BuildConfig;

public class FileUtils {
    public static final String LOCALE_FILE_SCHEME = "file://";

    private FileUtils() {
        // Utility class
    }

    public static boolean isLocaleFile(String url) {
        return url != null && url.startsWith(LOCALE_FILE_SCHEME);
    }

    public static boolean isAbsolute(String url) {
        return url != null && url.startsWith("/");
    }

    public static String getCsvFolderName() {
        switch (BuildConfig.FLAVOR) {
            case "off":
                return "Open Food Facts";
            case "opff":
                return "Open Pet Food Facts";
            case "opf":
                return "Open Products Facts";
            default:
                return "Open Beauty Facts";
        }
    }
}
