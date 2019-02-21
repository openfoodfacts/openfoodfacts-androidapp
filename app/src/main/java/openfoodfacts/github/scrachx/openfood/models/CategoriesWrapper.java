package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.network.deserializers.CategoriesWrapperDeserializer;

/**
 * Created by Lobster on 04.03.18.
 */

@JsonDeserialize(using = CategoriesWrapperDeserializer.class)
public class CategoriesWrapper {

    private List<CategoryResponse> categories;

    public List<Category> map() {
        List<Category> entityCategories = new ArrayList<>();
        for (CategoryResponse category: categories) {
            entityCategories.add(category.map());
        }

        return entityCategories;
    }

    public List<CategoryResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }
}
