package openfoodfacts.github.scrachx.openfood.views.category.bindingadapter;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.category.model.Category;
import openfoodfacts.github.scrachx.openfood.views.category.adapter.CategoryListRecyclerAdapter;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class RecyclerBindingAdapter {
    @BindingAdapter({"categories"})
    public static void setStations(RecyclerView recyclerView, List<Category> categoryList) {
        if (recyclerView != null && categoryList != null) {
            recyclerView.setAdapter(new CategoryListRecyclerAdapter(categoryList));
        }
    }
}