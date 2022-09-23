package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.content.Intent

inline fun <reified T> Intent(context: Context, builderAction: Intent.() -> Unit = {}): Intent {
    return Intent(context, T::class.java).apply(builderAction)
}

inline fun Intent(action: String?, builderAction: Intent.() -> Unit): Intent {
    return Intent(action).apply(builderAction)
}

inline fun Intent(builderAction: Intent.() -> Unit): Intent {
    return Intent().apply(builderAction)
}