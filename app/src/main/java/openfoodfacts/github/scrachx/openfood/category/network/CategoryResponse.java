package openfoodfacts.github.scrachx.openfood.category.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */

public class CategoryResponse {
    private final int count;
    private final List<Tag> tags;

    public CategoryResponse(int count, List<Tag> tags) {
        this.count = count;
        this.tags = tags;
    }

    public CategoryResponse() {
        this(0, Collections.emptyList());
    }

    public int getCount() {
        return count;
    }

    public List<Tag> getTags() {
        return tags;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tag{
        private final String id;
        private final String name;
        private final String url;
        private final int products;

        public Tag(String id, String name, String url, int products) {
            this.id = id;
            this.name = name;
            this.url = url;
            this.products = products;
        }

        public Tag() {
            this("", "", "",0 );
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
}
