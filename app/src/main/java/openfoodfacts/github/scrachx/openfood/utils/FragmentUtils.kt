package openfoodfacts.github.scrachx.openfood.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.models.ProductState

fun <T : Fragment> T.applyBundle(bundle: Bundle): T {
    this.arguments = bundle
    return this
}

fun getStateFromArguments(fragment: Fragment): ProductState? =
        fragment.arguments?.getSerializable(ProductEditActivity.KEY_STATE) as ProductState?

fun requireStateFromArguments(fragment: Fragment) = getStateFromArguments(fragment)
        ?: throw IllegalStateException("Fragment" + fragment + "started without without product state (not passed as argument).")

fun Fragment.requireProductState() = requireStateFromArguments(this)