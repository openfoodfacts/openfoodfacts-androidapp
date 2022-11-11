package openfoodfacts.github.scrachx.openfood.features.productlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.databinding.YourProductListsItemBinding
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import openfoodfacts.github.scrachx.openfood.utils.AutoUpdatableAdapter

class ProductListsAdapter : RecyclerView.Adapter<ProductListsAdapter.ViewHolder>(), AutoUpdatableAdapter {
    var lists: List<ProductLists> by autoNotifying(comparator = compareBy { it.id })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = YourProductListsItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listName = lists[position].listName
        val numOfProducts = lists[position].products.size
        lists[position].numOfProducts = numOfProducts.toLong()

        holder.listName.text = listName
        holder.listSize.text = numOfProducts.toString()
    }

    override fun getItemCount() = lists.size

    fun add(productList: ProductLists) {
        lists = lists + productList
    }

    fun remove(data: ProductLists) {
        lists = lists - data
    }

    fun replaceWith(newList: MutableList<ProductLists>) {
        lists = newList
    }

    class ViewHolder(binding: YourProductListsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val listName = binding.tvProductListName
        val listSize = binding.tvlistSize
    }
}

