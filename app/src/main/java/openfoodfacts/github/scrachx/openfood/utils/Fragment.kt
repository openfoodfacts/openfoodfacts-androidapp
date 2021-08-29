package openfoodfacts.github.scrachx.openfood.utils

import androidx.fragment.app.Fragment
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct

internal const val KEY_SEND_PRODUCT = "sendProduct"

fun Fragment.getProductState() = arguments?.getSerializable(KEY_STATE) as ProductState?

fun Fragment.requireProductState() = this.getProductState()
    ?: error("Fragment ${this::class.simpleName} started without '$KEY_STATE' argument.")


fun Fragment.getSendProduct() = arguments?.getSerializable(KEY_SEND_PRODUCT) as SendProduct?

fun Fragment.requireSendProduct() = this.getSendProduct()
    ?: error("Fragment ${this::class.simpleName} started without 'sendProduct' argument.")
