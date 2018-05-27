package org.openfoodfacts.scanner.views.category.bindingadapter;

import android.databinding.BindingAdapter;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import org.openfoodfacts.scanner.models.CategoryName;
import org.openfoodfacts.scanner.views.category.adapter.CategoryListRecyclerAdapter;

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