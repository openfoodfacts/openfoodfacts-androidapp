package openfoodfacts.github.scrachx.openfood.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "salt",
        "fat",
        "sugars",
        "saturated-fat"
})
public class NutrientLevels implements Serializable {

    @JsonProperty("salt")
    private String salt;
    @JsonProperty("fat")
    private String fat;
    @JsonProperty("sugars")
    private String sugars;
    @JsonProperty("saturated-fat")
    private String saturatedFat;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The salt
     */
    @JsonProperty("salt")
    public String getSalt() {
        return salt;
    }

    /**
     *
     * @param salt
     * The salt
     */
    @JsonProperty("salt")
    public void setSalt(String salt) {
        this.salt = salt;
    }

    public NutrientLevels withSalt(String salt) {
        this.salt = salt;
        return this;
    }

    /**
     *
     * @return
     * The fat
     */
    @JsonProperty("fat")
    public String getFat() {
        return fat;
    }

    /**
     *
     * @param fat
     * The fat
     */
    @JsonProperty("fat")
    public void setFat(String fat) {
        this.fat = fat;
    }

    public NutrientLevels withFat(String fat) {
        this.fat = fat;
        return this;
    }

    /**
     *
     * @return
     * The sugars
     */
    @JsonProperty("sugars")
    public String getSugars() {
        return sugars;
    }

    /**
     *
     * @param sugars
     * The sugars
     */
    @JsonProperty("sugars")
    public void setSugars(String sugars) {
        this.sugars = sugars;
    }

    public NutrientLevels withSugars(String sugars) {
        this.sugars = sugars;
        return this;
    }

    /**
     *
     * @return
     * The saturatedFat
     */
    @JsonProperty("saturated-fat")
    public String getSaturatedFat() {
        return saturatedFat;
    }

    /**
     *
     * @param saturatedFat
     * The saturated-fat
     */
    @JsonProperty("saturated-fat")
    public void setSaturatedFat(String saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public NutrientLevels withSaturatedFat(String saturatedFat) {
        this.saturatedFat = saturatedFat;
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

    public NutrientLevels withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return "NutrientLevels{" +
                "salt='" + salt + '\'' +
                ", fat='" + fat + '\'' +
                ", sugars='" + sugars + '\'' +
                ", saturatedFat='" + saturatedFat + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}