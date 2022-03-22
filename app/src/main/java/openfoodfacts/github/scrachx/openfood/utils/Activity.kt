package openfoodfacts.github.scrachx.openfood.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

fun Activity.getRootView(): View? = findViewById(android.R.id.content)

fun Activity.listenToKeyboardVisibilityChanges(listener: OnKeyboardVisibilityChanged) {
    getRootView()?.let {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            if (insets.isVisible(WindowInsetsCompat.Type.ime())) listener.onKeyboardVisible() else listener.onKeyboardDismissed()
            insets
        }
    }
}

fun Activity.stopListeningToKeyboardVisibilityChanges() {
    getRootView()?.let {
        ViewCompat.setOnApplyWindowInsetsListener(it, null)
    }
}

interface OnKeyboardVisibilityChanged {
    fun onKeyboardVisible()
    fun onKeyboardDismissed()
}