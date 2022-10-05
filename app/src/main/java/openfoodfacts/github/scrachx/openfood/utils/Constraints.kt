package openfoodfacts.github.scrachx.openfood.utils

import androidx.work.Constraints
import androidx.work.WorkRequest

inline fun Constraints(buildAction: Constraints.Builder.() -> Unit): Constraints {
    return Constraints.Builder().apply(buildAction).build()
}

