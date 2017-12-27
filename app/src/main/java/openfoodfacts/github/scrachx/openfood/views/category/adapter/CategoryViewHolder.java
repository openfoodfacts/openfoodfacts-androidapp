package openfoodfacts.github.scrachx.openfood.views.category.adapter;

import android.support.v7.widget.RecyclerView;

import openfoodfacts.github.scrachx.openfood.category.model.Category;
import openfoodfacts.github.scrachx.openfood.databinding.CategoryRecyclerItemBinding;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class CategoryViewHolder extends RecyclerView.ViewHolder {
    private final CategoryRecyclerItemBinding binding;

    public CategoryViewHolder(CategoryRecyclerItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Category category) {
        binding.setCategory(category);
        binding.executePendingBindings();
    }
}
