package openfoodfacts.github.scrachx.openfood.utils

import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.BuildConfig
import org.jetbrains.annotations.Contract

object FileUtils {
    const val LOCALE_FILE_SCHEME = "file://"
    fun isLocaleFile(url: String?): Boolean {
        return url?.startsWith(LOCALE_FILE_SCHEME) == true
    }

    @JvmStatic
    fun isAbsolute(url: String?): Boolean {
        return url?.startsWith("/") == true
    }

    @JvmStatic
    @Contract(pure = true)
    fun getCsvFolderName() = when (BuildConfig.FLAVOR) {
        OPFF -> "Open Pet Food Facts"
        OPF -> "Open Products Facts"
        OBF -> "Open Beauty Facts"
        OFF -> "Open Food Facts"
        else -> "Open Food Facts"
    }
}