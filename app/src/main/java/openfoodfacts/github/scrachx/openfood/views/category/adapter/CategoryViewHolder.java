package org.openfoodfacts.scanner.views.category.adapter;

import android.support.v7.widget.RecyclerView;

import org.openfoodfacts.scanner.databinding.CategoryRecyclerItemBinding;
import org.openfoodfacts.scanner.models.CategoryName;
import org.openfoodfacts.scanner.utils.SearchType;
import org.openfoodfacts.scanner.views.ProductBrowsingListActivity;

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
        binding.getRoot().setOnClickListener(v -> ProductBrowsingListActivity.startActivity(v.getContext(), category.getName(), SearchType.CATEGORY));
        binding.executePendingBindings();
    }
}
