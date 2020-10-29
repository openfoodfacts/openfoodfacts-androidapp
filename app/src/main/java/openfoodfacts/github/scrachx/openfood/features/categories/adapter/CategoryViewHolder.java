package openfoodfacts.github.scrachx.openfood.features.categories.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import openfoodfacts.github.scrachx.openfood.databinding.CategoryRecyclerItemBinding;
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class CategoryViewHolder extends RecyclerView.ViewHolder {
    private final CategoryRecyclerItemBinding binding;

    public CategoryViewHolder(@NonNull CategoryRecyclerItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(CategoryName category) {
        binding.setCategory(category);
        binding.getRoot().setOnClickListener(v ->
            ProductSearchActivity.start(v.getContext(),
                category.getCategoryTag(),
                category.getName(),
                SearchType.CATEGORY));
        binding.executePendingBindings();
    }
}
