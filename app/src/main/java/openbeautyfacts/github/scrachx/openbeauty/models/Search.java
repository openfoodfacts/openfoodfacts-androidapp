package openbeautyfacts.github.scrachx.openfood.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "page_size",
        "count",
        "skip",
        "page",
        "products"
})
public class Search implements Serializable {

    @JsonProperty("page_size")
    private String pageSize;
    @JsonProperty("count")
    private String count;
    @JsonProperty("skip")
    private Integer skip;
    @JsonProperty("page")
    private Integer page;
    @JsonProperty("products")
    private List<Product> products = new ArrayList<Product>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The pageSize
     */
    @JsonProperty("page_size")
    public String getPageSize() {
        return pageSize;
    }

    /**
     *
     * @param pageSize
     * The page_size
     */
    @JsonProperty("page_size")
    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

    /**
     *
     * @return
     * The count
     */
    @JsonProperty("count")
    public String getCount() {
        return count;
    }

    /**
     *
     * @param count
     * The count
     */
    @JsonProperty("count")
    public void setCount(String count) {
        this.count = count;
    }

    /**
     *
     * @return
     * The skip
     */
    @JsonProperty("skip")
    public Integer getSkip() {
        return skip;
    }

    /**
     *
     * @param skip
     * The skip
     */
    @JsonProperty("skip")
    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    /**
     *
     * @return
     * The page
     */
    @JsonProperty("page")
    public Integer getPage() {
        return page;
    }

    /**
     *
     * @param page
     * The page
     */
    @JsonProperty("page")
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     *
     * @return
     * The products
     */
    @JsonProperty("products")
    public List<Product> getProducts() {
        return products;
    }

    /**
     *
     * @param products
     * The products
     */
    @JsonProperty("products")
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
