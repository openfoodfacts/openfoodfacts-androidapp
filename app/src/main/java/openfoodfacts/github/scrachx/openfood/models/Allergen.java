package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "url",
        "name",
        "products",
        "id"
})
@Entity(indexes = {
        @Index(value = "name", unique = true)
})
public class Allergen {

    private String enable;
    private String url;
    private String name;
    private Integer products;
    @JsonProperty("id")
    @Id
    private String idAllergen;

    public Allergen() {
        this.enable = "false";
    }

    @Generated(hash = 512140330)
    public Allergen(String enable, String url, String name, Integer products,
                    String idAllergen) {
        this.enable = enable;
        this.url = url;
        this.name = name;
        this.products = products;
        this.idAllergen = idAllergen;
    }

    public String isEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getIdAllergen() {
        return idAllergen;
    }

    public void setIdAllergen(String idAllergen) {
        this.idAllergen = idAllergen;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The products
     */
    public Integer getProducts() {
        return products;
    }

    /**
     *
     * @param products
     * The products
     */
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

    public String getEnable() {
        return this.enable;
    }
    
}
