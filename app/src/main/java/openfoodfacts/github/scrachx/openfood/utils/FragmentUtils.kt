package openfoodfacts.github.scrachx.openfood.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.models.ProductState

object FragmentUtils {
    @JvmStatic
    fun getStateFromArguments(fragment: Fragment): ProductState? =
            fragment.arguments?.getSerializable(ProductEditActivity.KEY_STATE) as ProductState?

    @JvmStatic
    fun requireStateFromArguments(fragment: Fragment): ProductState = getStateFromArguments(fragment)
            ?: throw IllegalStateException("Fragment" + fragment + "started without without product state (not passed as argument).")

    fun <T : Fragment> T.applyBundle(bundle: Bundle): T {
        this.arguments = bundle
        return this
    }

    fun Fragment.requireProductState(): ProductState = requireStateFromArguments(this)
}

