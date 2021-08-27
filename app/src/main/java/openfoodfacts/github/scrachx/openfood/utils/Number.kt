package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.util.TypedValue

fun Number.toPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    context.resources.displayMetrics
).toInt()