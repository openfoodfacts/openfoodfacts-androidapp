package openfoodfacts.github.scrachx.openfood.features.categories.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.databinding.CategoryRecyclerItemBinding
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
class CategoryListRecyclerAdapter(private val categories: List<CategoryName>) : RecyclerView.Adapter<CategoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = CategoryRecyclerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) = holder.bind(categories[position])

    override fun getItemCount() = categories.count()
}