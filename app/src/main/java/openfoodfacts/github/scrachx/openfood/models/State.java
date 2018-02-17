package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "status_verbose",
        "status",
        "product",
        "code"
})
public class State implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonProperty("status_verbose")
    private String statusVerbose;
    private long status;
    private Product product;
    private String code;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     *
     * @return
     * The statusVerbose
     */
    public String getStatusVerbose() {
        return statusVerbose;
    }

    /**
     *
     * @param statusVerbose
     * The status_verbose
     */
    public void setStatusVerbose(String statusVerbose) {
        this.statusVerbose = statusVerbose;
    }

    public State withStatusVerbose(String statusVerbose) {
        this.statusVerbose = statusVerbose;
        return this;
    }

    /**
     *
     * @return
     * The status
     */
    public long getStatus() {
        return status;
    }

    /**
     *
     * @param status
     * The status
     */
    public void setStatus(long status) {
        this.status = status;
    }

    public State withStatus(long status) {
        this.status = status;
        return this;
    }

    /**
     *
     * @return
     * The product
     */
    public Product getProduct() {
        return product;
    }

    /**
     *
     * @param product
     * The product
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    public State withProduct(Product product) {
        this.product = product;
        return this;
    }

    /**
     *
     * @return
     * The code
     */
    public String getCode() {
        return code;
    }

    /**
     *
     * @param code
     * The code
     */
    public void setCode(String code) {
        this.code = code;
    }

    public State withCode(String code) {
        this.code = code;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public State withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return "State{" +
                "statusVerbose='" + statusVerbose + '\'' +
                ", status=" + status +
                ", product=" + product +
                ", code='" + code + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
