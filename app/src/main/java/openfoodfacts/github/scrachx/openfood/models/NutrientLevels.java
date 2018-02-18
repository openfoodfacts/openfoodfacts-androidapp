package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NutrientLevels implements Serializable {
    private static final long serialVersionUID = 1L;
    private NutrimentLevel salt;
    private NutrimentLevel fat;
    private NutrimentLevel sugars;
    @JsonProperty("saturated-fat")
    private NutrimentLevel saturatedFat;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     *
     * @return
     * The salt
     */
    public NutrimentLevel getSalt() {
        return salt;
    }

    /**
     *
     * @param salt
     * The salt
     */
    public void setSalt(NutrimentLevel salt) {
        this.salt = salt;
    }

    /**
     *
     * @return
     * The fat
     */
    public NutrimentLevel getFat() {
        return fat;
    }

    /**
     *
     * @param fat
     * The fat
     */
    public void setFat(NutrimentLevel fat) {
        this.fat = fat;
    }

    /**
     *
     * @return
     * The sugars
     */
    public NutrimentLevel getSugars() {
        return sugars;
    }

    /**
     *
     * @param sugars
     * The sugars
     */
    public void setSugars(NutrimentLevel sugars) {
        this.sugars = sugars;
    }

    /**
     *
     * @return
     * The saturatedFat
     */
    public NutrimentLevel getSaturatedFat() {
        return saturatedFat;
    }

    /**
     *
     * @param saturatedFat
     * The saturated-fat
     */
    public void setSaturatedFat(NutrimentLevel saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
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