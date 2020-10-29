package openfoodfacts.github.scrachx.openfood.features.categories.bindingadapter;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.features.categories.adapter.CategoryListRecyclerAdapter;
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class RecyclerBindingAdapter {
    private RecyclerBindingAdapter() {
    }

    @BindingAdapter({"categories"})
    public static void setStations(RecyclerView recyclerView, List<CategoryName> categoryList) {
        if (recyclerView != null && categoryList != null) {
            recyclerView.setAdapter(new CategoryListRecyclerAdapter(categoryList));
        }
    }
}
