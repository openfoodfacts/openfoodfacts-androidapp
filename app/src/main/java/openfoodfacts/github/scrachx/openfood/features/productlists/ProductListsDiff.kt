package openfoodfacts.github.scrachx.openfood.features.productlists

import androidx.recyclerview.widget.DiffUtil
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists

class ProductListsDiffCallback(
    private val oldItems: List<ProductLists>,
    private val newItems: List<ProductLists>,
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldItems.size

    override fun getNewListSize(): Int = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].id == newItems[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldItemPosition, newItemPosition)
    }
}