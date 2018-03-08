package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Lobster on 04.03.18.
 */

public class CategoryResponse {

    private String code;

    private Map<String, String> names;

    public CategoryResponse(String code, Map<String, String> names) {
        this.code = code;
        this.names = names;
    }

    public Category map() {
        Category category = new Category(code, new ArrayList<>());
        for (Map.Entry<String, String> name : names.entrySet()) {
            category.getNames().add(new CategoryName(category.getTag(), name.getKey(), name.getValue()));
        }

        return category;
    }

}
