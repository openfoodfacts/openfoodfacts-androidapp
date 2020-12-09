package openfoodfacts.github.scrachx.openfood.utils

import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.BuildConfig
import org.jetbrains.annotations.Contract

fun isLocaleFile(url: String?) = url?.startsWith(LOCALE_FILE_SCHEME) ?: false

fun isAbsoluteUrl(url: String?) = url?.startsWith("/") ?: false

@Contract(pure = true)
fun getCsvFolderName() = when (BuildConfig.FLAVOR) {
    OPFF -> "Open Pet Food Facts"
    OPF -> "Open Products Facts"
    OBF -> "Open Beauty Facts"
    OFF -> "Open Food Facts"
    else -> "Open Food Facts"
}

const val LOCALE_FILE_SCHEME = "file://"