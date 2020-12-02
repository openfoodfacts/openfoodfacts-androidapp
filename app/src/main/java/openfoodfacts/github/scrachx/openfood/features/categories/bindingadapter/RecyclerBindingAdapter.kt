package openfoodfacts.github.scrachx.openfood.features.categories.bindingadapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.features.categories.adapter.CategoryListRecyclerAdapter
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
object RecyclerBindingAdapter {
    @JvmStatic
    @BindingAdapter("categories")
    fun setStations(recyclerView: RecyclerView?, categoryList: List<CategoryName>?) {
        if (recyclerView != null && categoryList != null) {
            recyclerView.adapter = CategoryListRecyclerAdapter(categoryList)
        }
    }
}