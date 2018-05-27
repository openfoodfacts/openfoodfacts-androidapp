package org.openfoodfacts.scanner.views.category.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import org.openfoodfacts.scanner.databinding.CategoryRecyclerItemBinding;
import org.openfoodfacts.scanner.models.CategoryName;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class CategoryListRecyclerAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
    private final List<CategoryName> categories;

    public CategoryListRecyclerAdapter(List<CategoryName> categories) {
        this.categories = categories;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CategoryRecyclerItemBinding binding = CategoryRecyclerItemBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
