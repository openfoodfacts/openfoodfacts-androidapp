package openfoodfacts.github.scrachx.openfood.features.product.edit

import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct

abstract class ProductEditFragment : BaseFragment() {
    open operator fun next() {
        if (allValid()) (activity as? ProductEditActivity)?.proceed()
    }

    protected fun getProductFromArgs() = arguments?.getSerializable("product") as Product?

    protected fun getEditOfflineProductFromArgs() = arguments?.getSerializable("edit_offline_product") as OfflineSavedProduct?

    protected val isEditingFromArgs get() = arguments?.getBoolean(ProductEditActivity.KEY_IS_EDITING) ?: false

    fun anyInvalid() = !allValid()
    protected abstract fun allValid(): Boolean

    /**
     * adds only those fields to the query map which have changed.
     */
    abstract fun getUpdatedFieldsMap(): Map<String, String?>

    abstract fun hideImageProgress(errorInUploading: Boolean, message: String)

    abstract fun showImageProgress()
}