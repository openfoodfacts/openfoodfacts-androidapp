package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import com.squareup.picasso.Picasso

inline fun Picasso(
    context: Context,
    builderAction: Picasso.Builder.() -> Unit = {},
): Picasso {
    return Picasso.Builder(context).apply(builderAction).build()
}