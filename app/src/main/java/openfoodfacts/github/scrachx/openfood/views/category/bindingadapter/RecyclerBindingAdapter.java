package openfoodfacts.github.scrachx.openfood.views.category.bindingadapter;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.views.category.adapter.CategoryListRecyclerAdapter;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class RecyclerBindingAdapter {
    @BindingAdapter({"categories"})
    public static void setStations(RecyclerView recyclerView, List<CategoryName> categoryList) {
        if (recyclerView != null && categoryList != null) {
            recyclerView.setAdapter(new CategoryListRecyclerAdapter(categoryList));
        }
    }
}
