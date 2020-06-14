package openfoodfacts.github.scrachx.openfood.utils;

import openfoodfacts.github.scrachx.openfood.BuildConfig;

public class FileUtils {
    private FileUtils() {

    }

    public static final String LOCALE_FILE_SCHEME = "file://";

    public static boolean isLocaleFile(String url) {
        return url != null && url.startsWith(LOCALE_FILE_SCHEME);
    }

    public static boolean isAbsolute(String url) {
        return url != null && url.startsWith("/");
    }

    public static String getCsvFolderName() {
        if (BuildConfig.FLAVOR.equals("off")) {
            return "Open Food Facts";
        } else if (BuildConfig.FLAVOR.equals("opff")) {
            return "Open Pet Food Facts";
        } else if (BuildConfig.FLAVOR.equals("opf")) {
            return "Open Products Facts";
        } else {
            return "Open Beauty Facts";
        }
    }
}
