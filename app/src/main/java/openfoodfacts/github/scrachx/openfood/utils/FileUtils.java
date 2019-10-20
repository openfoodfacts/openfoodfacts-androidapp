package openfoodfacts.github.scrachx.openfood.utils;

import openfoodfacts.github.scrachx.openfood.BuildConfig;

public class FileUtils {
    public static final String LOCALE_FILE_SCHEME = "file://";

    public static boolean isLocaleFile(String url) {
        return url != null && url.startsWith(LOCALE_FILE_SCHEME);
    }
    public static boolean isAbsolute(String url) {
        return url != null && url.startsWith("/");
    }

    public static String getCsvFolderName() {
           String folderMain;
           if ((BuildConfig.FLAVOR.equals("off"))) {
               folderMain = "Open Food Facts";
           } else if ((BuildConfig.FLAVOR.equals("opff"))) {
               folderMain = "Open Pet Food Facts";
           } else if ((BuildConfig.FLAVOR.equals("opf"))) {
               folderMain = "Open Products Facts";
           } else {
               folderMain = "Open Beauty Facts";
           }
           return folderMain;
       }
}
