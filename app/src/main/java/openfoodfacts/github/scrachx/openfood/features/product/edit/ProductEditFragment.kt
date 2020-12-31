package openfoodfacts.github.scrachx.openfood.features.product.edit

import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment

abstract class ProductEditFragment : BaseFragment() {
    open operator fun next() {
        if (allValid()) (activity as? ProductEditActivity)?.proceed()
    }

    fun anyInvalid() = !allValid()
    protected abstract fun allValid(): Boolean

    /**
     * adds only those fields to the query map which have changed.
     */
    abstract fun addUpdatedFieldsToMap(targetMap: MutableMap<String, String?>)

    abstract fun hideImageProgress(errorInUploading: Boolean, message: String)
}