package openfoodfacts.github.scrachx.openfood.utils

import androidx.work.Data

inline fun buildData(buildAction: Data.Builder.() -> Unit): Data {
    return Data.Builder().apply(buildAction).build()
}