package openfoodfacts.github.scrachx.openfood.features.productlists

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.databinding.YourProductListsItemBinding
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists

class ProductListsAdapter(
        internal val context: Context,
        private val productLists: MutableList<ProductLists>
) : RecyclerView.Adapter<ProductListsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListsViewHolder {
        val binding = YourProductListsItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductListsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductListsViewHolder, position: Int) {
        val listName = productLists[position].listName
        val numOfProducts = productLists[position].products.size
        productLists[position].numOfProducts = numOfProducts.toLong()

        holder.binding.tvProductListName.text = listName
        holder.binding.tvlistSize.text = numOfProducts.toString()
    }

    override fun getItemCount() = productLists.size

    fun remove(data: ProductLists) {
        val position = productLists.indexOf(data)
        productLists.removeAt(position)
        notifyItemRemoved(position)
    }

}

class ProductListsViewHolder(val binding: YourProductListsItemBinding) : RecyclerView.ViewHolder(binding.root)