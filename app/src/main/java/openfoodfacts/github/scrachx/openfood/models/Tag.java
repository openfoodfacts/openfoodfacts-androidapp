package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

// Model class for Packager code 'tags' --> "https://world.openfoodfacts.org/packager-codes.json"

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "products",
        "url",
        "id"
})
@Entity(indexes = {
        @Index(value = "id", unique = true)
})
public class Tag {

    @JsonProperty("name")
    private String name;
    @JsonProperty("products")
    private Integer products;
    @JsonProperty("url")
    private String url;
    @JsonProperty("id")
    private String id;

    @Generated(hash = 1453823337)
    public Tag(String name, Integer products, String url, String id) {
        this.name = name;
        this.products = products;
        this.url = url;
        this.id = id;
    }

    @Generated(hash = 1605720318)
    public Tag() {
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("products")
    public Integer getProducts() {
        return products;
    }

    @JsonProperty("products")
    public void setProducts(Integer products) {
        this.products = products;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }
}