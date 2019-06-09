package openfoodfacts.github.scrachx.openfood.category.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import openfoodfacts.github.scrachx.openfood.category.model.Category;
import openfoodfacts.github.scrachx.openfood.category.network.CategoryResponse;

/**
 * Class used to map tag name with the corresponding categories
 */

public class CategoryMapper {

    @Inject
    public CategoryMapper() {
    }

    /**
     * Returns list of Category objects using the tags
     * @param  tags List of CategoryResponse.Tag object*/

    public List<Category> fromNetwork(List<CategoryResponse.Tag> tags) {
        List<Category> categories = new ArrayList<>(tags.size());
        for (CategoryResponse.Tag tag : tags) {
            categories.add(new Category(tag.getId(),
                    tag.getName(),
                    tag.getUrl(),
                    tag.getProducts()));
        }
        Collections.sort(categories, (first, second) -> first.getName().compareTo(second.getName()));
        return categories;
    }
}