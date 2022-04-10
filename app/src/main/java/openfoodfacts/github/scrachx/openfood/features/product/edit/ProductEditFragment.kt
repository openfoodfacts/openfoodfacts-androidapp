package openfoodfacts.github.scrachx.openfood.features.product.edit

import androidx.fragment.app.activityViewModels
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.Fields
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct

abstract class ProductEditFragment : BaseFragment() {

    protected val editViewModel: ProductEditViewModel by activityViewModels()

    fun nextFragment() {
        if (allValid()) {
            editViewModel.nextFragment()
        }
    }

    protected fun getProductFromArgs() = arguments?.getSerializable("product") as Product?

    protected fun getEditOfflineProductFromArgs() = arguments?.getSerializable("edit_offline_product") as OfflineSavedProduct?

    protected val isEditingFromArgs get() = arguments?.getBoolean(ProductEditActivity.KEY_IS_EDITING) ?: false

    fun anyInvalid() = !allValid()
    protected abstract fun allValid(): Boolean

    /**
     * adds only those fields to the query map which have changed.
     */
    abstract fun getUpdatedFields(): Fields

    abstract fun hideImageProgress(errorInUploading: Boolean, message: String)

    abstract fun showImageProgress()
}