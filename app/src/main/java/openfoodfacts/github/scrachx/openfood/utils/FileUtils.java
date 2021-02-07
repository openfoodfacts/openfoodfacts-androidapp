package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import openfoodfacts.github.scrachx.openfood.BuildConfig;

import static openfoodfacts.github.scrachx.openfood.AppFlavors.OBF;
import static openfoodfacts.github.scrachx.openfood.AppFlavors.OFF;
import static openfoodfacts.github.scrachx.openfood.AppFlavors.OPF;
import static openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF;

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

    @NonNull
    @Contract(pure = true)
    public static String getCsvFolderName() {
        switch (BuildConfig.FLAVOR) {
            case OPFF:
                return "Open Pet Food Facts";
            case OPF:
                return "Open Products Facts";
            case OBF:
                return "Open Beauty Facts";
            case OFF:
            default:
                return "Open Food Facts";
        }
    }
}
