package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "page_size",
        "count",
        "skip",
        "page",
        "products"
})
public class Search implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonProperty("page_size")
    private String pageSize;
    private String count;
    private Integer skip;
    private Integer page;
    private List<Product> products = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     *
     * @return
     * The pageSize
     */
    public String getPageSize() {
        return pageSize;
    }

    /**
     *
     * @param pageSize
     * The page_size
     */
    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    /**
     *
     * @return
     * The count
     */
    public String getCount() {
        return count;
    }

    /**
     *
     * @param count
     * The count
     */
    public void setCount(String count) {
        this.count = count;
    }

    /**
     *
     * @return
     * The skip
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     *
     * @param skip
     * The skip
     */
    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    /**
     *
     * @return
     * The page
     */
    public Integer getPage() {
        return page;
    }

    /**
     *
     * @param page
     * The page
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     *
     * @return
     * The products
     */
    public List<Product> getProducts() {
        return products;
    }

    /**
     *
     * @param products
     * The products
     */
    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
