package openfoodfacts.github.scrachx.openfood.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct

fun <T : Fragment> T.applyBundle(bundle: Bundle): T {
    this.arguments = bundle
    return this
}

fun Fragment.getProductState() = arguments?.getSerializable(ProductEditActivity.KEY_STATE) as ProductState?

fun Fragment.requireProductState() = this.getProductState()
        ?: error("Fragment ${this::class.simpleName} started without without 'state' argument.")

fun Fragment.getSendProduct() = arguments?.getSerializable("sendProduct") as SendProduct?

fun Fragment.requireSendProduct() = this.getSendProduct()
        ?: error("Fragment ${this::class.simpleName} started without 'sendProduct' argument.")