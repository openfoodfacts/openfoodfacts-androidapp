package openfoodfacts.github.scrachx.openfood.utils

import androidx.work.Constraints

inline fun buildConstraints(buildAction: Constraints.Builder.() -> Unit): Constraints {
    return Constraints.Builder().apply(buildAction).build()
}