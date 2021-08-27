package openfoodfacts.github.scrachx.openfood.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.models.ProductState

fun Activity.hideKeyboard() {
    val view = currentFocus ?: return
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.getProductState() = intent.getSerializableExtra(ProductEditActivity.KEY_STATE) as ProductState?
fun Activity.requireProductState() = this.getProductState()
    ?: error("Activity ${this::class.simpleName} started without '${ProductEditActivity.KEY_STATE}' serializable in intent.")