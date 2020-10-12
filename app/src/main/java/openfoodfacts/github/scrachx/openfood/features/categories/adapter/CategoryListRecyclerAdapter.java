package openfoodfacts.github.scrachx.openfood.features.categories.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.databinding.CategoryRecyclerItemBinding;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;

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
