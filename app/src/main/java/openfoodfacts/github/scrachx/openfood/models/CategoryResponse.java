package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Lobster on 04.03.18.
 */

public class CategoryResponse {

    private String code;

    private Map<String, String> names;
    private String wikiDataCode;
    private Boolean isWikiDataIdPresent = false;

    public CategoryResponse(String code, Map<String, String> names, String wikiDataCode) {
        this.code = code;
        this.names = names;
        this.wikiDataCode = wikiDataCode;
        isWikiDataIdPresent = true;
    }

    public CategoryResponse(String code, Map<String, String> names) {
        this.code = code;
        this.names = names;
        isWikiDataIdPresent = false;
    }

    public Category map() {
        Category category;
        if (isWikiDataIdPresent) {
            category = new Category(code, new ArrayList<>(), wikiDataCode);
            for (Map.Entry<String, String> name : names.entrySet()) {
                category.getNames().add(new CategoryName(category.getTag(), name.getKey(), name.getValue(), wikiDataCode));
            }

        } else {
            category = new Category(code, new ArrayList<>());
            for (Map.Entry<String, String> name : names.entrySet()) {
                category.getNames().add(new CategoryName(category.getTag(), name.getKey(), name.getValue()));
            }
        }


        return category;
    }

}
