package openfoodfacts.github.scrachx.openfood.utils

import android.text.style.ClickableSpan
import android.view.View

inline fun ClickableSpan(
    crossinline onClick: (View) -> Unit,
) = object : ClickableSpan() {
    override fun onClick(widget: View) = onClick(widget)
}