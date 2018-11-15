package openfoodfacts.github.scrachx.openfood.views.category.adapter;

import android.support.v7.widget.RecyclerView;

import openfoodfacts.github.scrachx.openfood.databinding.CategoryRecyclerItemBinding;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.views.ProductBrowsingListActivity;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class CategoryViewHolder extends RecyclerView.ViewHolder {
    private final CategoryRecyclerItemBinding binding;

    public CategoryViewHolder(CategoryRecyclerItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(CategoryName category) {
        binding.setCategory(category);
        binding.getRoot().setOnClickListener(v ->
                ProductBrowsingListActivity.startActivity(v.getContext(),
                        category.getCategoryTag(),
                        category.getName(),
                        SearchType.CATEGORY));
        binding.executePendingBindings();
    }
}
