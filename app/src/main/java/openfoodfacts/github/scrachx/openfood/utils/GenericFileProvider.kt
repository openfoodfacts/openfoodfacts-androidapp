package openfoodfacts.github.scrachx.openfood.utils

import androidx.core.content.FileProvider
import openfoodfacts.github.scrachx.openfood.BuildConfig

/**
 * Created by prajwalm on 05/04/18.
 */
class GenericFileProvider : FileProvider() {

    companion object {

        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.provider"

    }


}