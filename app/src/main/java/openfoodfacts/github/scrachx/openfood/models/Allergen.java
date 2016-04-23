package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.orm.SugarRecord;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "url",
        "name",
        "products",
        "id"
})
public class Allergen extends SugarRecord {

    private String enable;
    @JsonProperty("url")
    private String url;
    @JsonProperty("name")
    private String name;
    @JsonProperty("products")
    private Integer products;
    @JsonProperty("id")
    private String idAllergen;

    public Allergen() {
    }

    public Allergen(String url, String name, Integer products, String idAllergen) {
        this.url = url;
        this.name = name;
        this.products = products;
        this.idAllergen = idAllergen;
        enable = "false";
    }

    public String isEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    @JsonProperty("id")
    public String getIdAllergen() {
        return idAllergen;
    }

    @JsonProperty("id")
    public void setIdAllergen(String idAllergen) {
        this.idAllergen = idAllergen;
    }

    /**
     *
     * @return
     * The url
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The products
     */
    @JsonProperty("products")
    public Integer getProducts() {
        return products;
    }

    /**
     *
     * @param products
     * The products
     */
    @JsonProperty("products")
    public void setProducts(Integer products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "Allergen{" +
                "enable=" + enable +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", products=" + products +
                ", idAllergen='" + idAllergen + '\'' +
                '}';
    }
}
