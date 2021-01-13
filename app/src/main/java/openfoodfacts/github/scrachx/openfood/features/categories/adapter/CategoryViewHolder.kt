package openfoodfacts.github.scrachx.openfood.features.categories.adapter

import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.databinding.CategoryRecyclerItemBinding
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.utils.SearchType

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
class CategoryViewHolder(private val binding: CategoryRecyclerItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(category: CategoryName) {
        binding.category = category
        binding.root.setOnClickListener {
            start(it.context, SearchType.CATEGORY, category.categoryTag!!, category.name!!)
        }
        binding.executePendingBindings()
    }
}