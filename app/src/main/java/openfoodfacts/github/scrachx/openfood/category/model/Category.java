package openfoodfacts.github.scrachx.openfood.category.model;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class Category {
    private final String id;
    private final String name;
    private final String url;
    private final int products;

    public Category(String id, String name, String url, int products) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.products = products;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getProducts() {
        return products;
    }
}
