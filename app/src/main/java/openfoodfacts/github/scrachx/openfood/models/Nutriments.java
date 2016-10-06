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
        "sodium",
        "sugars",
        "sugars_value",
        "carbohydrates_unit",
        "fat_unit",
        "proteins_unit",
        "nutrition-score-fr_100g",
        "fat",
        "carbohydrates_value",
        "proteins_serving",
        "sodium_serving",
        "fiber_value",
        "proteins",
        "salt",
        "salt_unit",
        "nutrition-score-fr",
        "sugars_unit",
        "fat_serving",
        "fat_value",
        "sodium_unit",
        "sugars_100g",
        "sodium_100g",
        "saturated-fat_serving",
        "saturated-fat_unit",
        "fiber_unit",
        "energy_value",
        "energy",
        "energy_unit",
        "sugars_serving",
        "carbohydrates_100g",
        "nutrition-score-uk",
        "saturated-fat_value",
        "proteins_100g",
        "fiber_serving",
        "carbohydrates_serving",
        "sodium_value",
        "salt_value",
        "energy_serving",
        "fat_100g",
        "saturated-fat_100g",
        "nutrition-score-uk_100g",
        "fiber",
        "salt_100g",
        "salt_serving",
        "fiber_100g",
        "carbohydrates",
        "energy_100g",
        "proteins_value",
        "saturated-fat"
})
public class Nutriments implements Serializable {

    private String sodium;
    private String sugars;
    @JsonProperty("sugars_value")
    private String sugarsValue;
    @JsonProperty("carbohydrates_unit")
    private String carbohydratesUnit;
    @JsonProperty("fat_unit")
    private String fatUnit;
    @JsonProperty("proteins_unit")
    private String proteinsUnit;
    @JsonProperty("nutrition-score-fr_100g")
    private String nutritionScoreFr100g;
    private String fat;
    @JsonProperty("carbohydrates_value")
    private String carbohydratesValue;
    @JsonProperty("proteins_serving")
    private String proteinsServing;
    @JsonProperty("sodium_serving")
    private String sodiumServing;
    @JsonProperty("fiber_value")
    private String fiberValue;
    private String proteins;
    private String salt;
    @JsonProperty("salt_unit")
    private String saltUnit;
    @JsonProperty("nutrition-score-fr")
    private String nutritionScoreFr;
    @JsonProperty("sugars_unit")
    private String sugarsUnit;
    @JsonProperty("fat_serving")
    private String fatServing;
    @JsonProperty("fat_value")
    private String fatValue;
    @JsonProperty("sodium_unit")
    private String sodiumUnit;
    @JsonProperty("sugars_100g")
    private String sugars100g;
    @JsonProperty("sodium_100g")
    private String sodium100g;
    @JsonProperty("saturated-fat_serving")
    private String saturatedFatServing;
    @JsonProperty("saturated-fat_unit")
    private String saturatedFatUnit;
    @JsonProperty("fiber_unit")
    private String fiberUnit;
    @JsonProperty("energy_value")
    private String energyValue;
    private String energy;
    @JsonProperty("energy_unit")
    private String energyUnit;
    @JsonProperty("sugars_serving")
    private String sugarsServing;
    @JsonProperty("carbohydrates_100g")
    private String carbohydrates100g;
    @JsonProperty("nutrition-score-uk")
    private String nutritionScoreUk;
    @JsonProperty("saturated-fat_value")
    private String saturatedFatValue;
    @JsonProperty("proteins_100g")
    private String proteins100g;
    @JsonProperty("fiber_serving")
    private String fiberServing;
    @JsonProperty("carbohydrates_serving")
    private String carbohydratesServing;
    @JsonProperty("sodium_value")
    private String sodiumValue;
    @JsonProperty("salt_value")
    private String saltValue;
    @JsonProperty("energy_serving")
    private String energyServing;
    @JsonProperty("fat_100g")
    private String fat100g;
    @JsonProperty("saturated-fat_100g")
    private String saturatedFat100g;
    @JsonProperty("nutrition-score-uk_100g")
    private String nutritionScoreUk100g;
    private String fiber;
    @JsonProperty("salt_100g")
    private String salt100g;
    @JsonProperty("salt_serving")
    private String saltServing;
    @JsonProperty("fiber_100g")
    private String fiber100g;
    private String carbohydrates;
    @JsonProperty("energy_100g")
    private String energy100g;
    @JsonProperty("proteins_value")
    private String proteinsValue;
    @JsonProperty("saturated-fat")
    private String saturatedFat;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * @return The sodium
     */
    public String getSodium() {
        return sodium;
    }

    /**
     * @param sodium The sodium
     */
    public void setSodium(String sodium) {
        this.sodium = sodium;
    }

    public Nutriments withSodium(String sodium) {
        this.sodium = sodium;
        return this;
    }

    /**
     * @return The sugars
     */
    public String getSugars() {
        return sugars;
    }

    /**
     * @param sugars The sugars
     */
    public void setSugars(String sugars) {
        this.sugars = sugars;
    }

    public Nutriments withSugars(String sugars) {
        this.sugars = sugars;
        return this;
    }

    /**
     * @return The sugarsValue
     */
    public String getSugarsValue() {
        return sugarsValue;
    }

    /**
     * @param sugarsValue The sugars_value
     */
    public void setSugarsValue(String sugarsValue) {
        this.sugarsValue = sugarsValue;
    }

    public Nutriments withSugarsValue(String sugarsValue) {
        this.sugarsValue = sugarsValue;
        return this;
    }

    /**
     * @return The carbohydratesUnit
     */
    public String getCarbohydratesUnit() {
        return carbohydratesUnit;
    }

    /**
     * @param carbohydratesUnit The carbohydrates_unit
     */
    public void setCarbohydratesUnit(String carbohydratesUnit) {
        this.carbohydratesUnit = carbohydratesUnit;
    }

    public Nutriments withCarbohydratesUnit(String carbohydratesUnit) {
        this.carbohydratesUnit = carbohydratesUnit;
        return this;
    }

    /**
     * @return The fatUnit
     */
    public String getFatUnit() {
        return fatUnit;
    }

    /**
     * @param fatUnit The fat_unit
     */
    public void setFatUnit(String fatUnit) {
        this.fatUnit = fatUnit;
    }

    public Nutriments withFatUnit(String fatUnit) {
        this.fatUnit = fatUnit;
        return this;
    }

    /**
     * @return The proteinsUnit
     */
    public String getProteinsUnit() {
        return proteinsUnit;
    }

    /**
     * @param proteinsUnit The proteins_unit
     */
    public void setProteinsUnit(String proteinsUnit) {
        this.proteinsUnit = proteinsUnit;
    }

    public Nutriments withProteinsUnit(String proteinsUnit) {
        this.proteinsUnit = proteinsUnit;
        return this;
    }

    /**
     * @return The nutritionScoreFr100g
     */
    public String getNutritionScoreFr100g() {
        return nutritionScoreFr100g;
    }

    /**
     * @param nutritionScoreFr100g The nutrition-score-fr_100g
     */
    public void setNutritionScoreFr100g(String nutritionScoreFr100g) {
        this.nutritionScoreFr100g = nutritionScoreFr100g;
    }

    public Nutriments withNutritionScoreFr100g(String nutritionScoreFr100g) {
        this.nutritionScoreFr100g = nutritionScoreFr100g;
        return this;
    }

    /**
     * @return The fat
     */
    public String getFat() {
        return fat;
    }

    /**
     * @param fat The fat
     */
    public void setFat(String fat) {
        this.fat = fat;
    }

    public Nutriments withFat(String fat) {
        this.fat = fat;
        return this;
    }

    /**
     * @return The carbohydratesValue
     */
    public String getCarbohydratesValue() {
        return carbohydratesValue;
    }

    /**
     * @param carbohydratesValue The carbohydrates_value
     */
    public void setCarbohydratesValue(String carbohydratesValue) {
        this.carbohydratesValue = carbohydratesValue;
    }

    public Nutriments withCarbohydratesValue(String carbohydratesValue) {
        this.carbohydratesValue = carbohydratesValue;
        return this;
    }

    /**
     * @return The proteinsServing
     */
    public String getProteinsServing() {
        return proteinsServing;
    }

    /**
     * @param proteinsServing The proteins_serving
     */
    public void setProteinsServing(String proteinsServing) {
        this.proteinsServing = proteinsServing;
    }

    public Nutriments withProteinsServing(String proteinsServing) {
        this.proteinsServing = proteinsServing;
        return this;
    }

    /**
     * @return The sodiumServing
     */
    public String getSodiumServing() {
        return sodiumServing;
    }

    /**
     * @param sodiumServing The sodium_serving
     */
    public void setSodiumServing(String sodiumServing) {
        this.sodiumServing = sodiumServing;
    }

    public Nutriments withSodiumServing(String sodiumServing) {
        this.sodiumServing = sodiumServing;
        return this;
    }

    /**
     * @return The fiberValue
     */
    public String getFiberValue() {
        return fiberValue;
    }

    /**
     * @param fiberValue The fiber_value
     */
    public void setFiberValue(String fiberValue) {
        this.fiberValue = fiberValue;
    }

    public Nutriments withFiberValue(String fiberValue) {
        this.fiberValue = fiberValue;
        return this;
    }

    /**
     * @return The proteins
     */
    public String getProteins() {
        return proteins;
    }

    /**
     * @param proteins The proteins
     */
    public void setProteins(String proteins) {
        this.proteins = proteins;
    }

    public Nutriments withProteins(String proteins) {
        this.proteins = proteins;
        return this;
    }

    /**
     * @return The salt
     */
    public String getSalt() {
        return salt;
    }

    /**
     * @param salt The salt
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Nutriments withSalt(String salt) {
        this.salt = salt;
        return this;
    }

    /**
     * @return The saltUnit
     */
    public String getSaltUnit() {
        return saltUnit;
    }

    /**
     * @param saltUnit The salt_unit
     */
    public void setSaltUnit(String saltUnit) {
        this.saltUnit = saltUnit;
    }

    public Nutriments withSaltUnit(String saltUnit) {
        this.saltUnit = saltUnit;
        return this;
    }

    /**
     * @return The nutritionScoreFr
     */
    public String getNutritionScoreFr() {
        return nutritionScoreFr;
    }

    /**
     * @param nutritionScoreFr The nutrition-score-fr
     */
    public void setNutritionScoreFr(String nutritionScoreFr) {
        this.nutritionScoreFr = nutritionScoreFr;
    }

    public Nutriments withNutritionScoreFr(String nutritionScoreFr) {
        this.nutritionScoreFr = nutritionScoreFr;
        return this;
    }

    /**
     * @return The sugarsUnit
     */
    public String getSugarsUnit() {
        return sugarsUnit;
    }

    /**
     * @param sugarsUnit The sugars_unit
     */
    public void setSugarsUnit(String sugarsUnit) {
        this.sugarsUnit = sugarsUnit;
    }

    public Nutriments withSugarsUnit(String sugarsUnit) {
        this.sugarsUnit = sugarsUnit;
        return this;
    }

    /**
     * @return The fatServing
     */
    public String getFatServing() {
        return fatServing;
    }

    /**
     * @param fatServing The fat_serving
     */
    public void setFatServing(String fatServing) {
        this.fatServing = fatServing;
    }

    public Nutriments withFatServing(String fatServing) {
        this.fatServing = fatServing;
        return this;
    }

    /**
     * @return The fatValue
     */
    public String getFatValue() {
        return fatValue;
    }

    /**
     * @param fatValue The fat_value
     */
    public void setFatValue(String fatValue) {
        this.fatValue = fatValue;
    }

    public Nutriments withFatValue(String fatValue) {
        this.fatValue = fatValue;
        return this;
    }

    /**
     * @return The sodiumUnit
     */
    public String getSodiumUnit() {
        return sodiumUnit;
    }

    /**
     * @param sodiumUnit The sodium_unit
     */
    public void setSodiumUnit(String sodiumUnit) {
        this.sodiumUnit = sodiumUnit;
    }

    public Nutriments withSodiumUnit(String sodiumUnit) {
        this.sodiumUnit = sodiumUnit;
        return this;
    }

    /**
     * @return The sugars100g
     */
    public String getSugars100g() {
        return sugars100g;
    }

    /**
     * @param sugars100g The sugars_100g
     */
    public void setSugars100g(String sugars100g) {
        this.sugars100g = sugars100g;
    }

    public Nutriments withSugars100g(String sugars100g) {
        this.sugars100g = sugars100g;
        return this;
    }

    /**
     * @return The sodium100g
     */
    public String getSodium100g() {
        return sodium100g;
    }

    /**
     * @param sodium100g The sodium_100g
     */
    public void setSodium100g(String sodium100g) {
        this.sodium100g = sodium100g;
    }

    public Nutriments withSodium100g(String sodium100g) {
        this.sodium100g = sodium100g;
        return this;
    }

    /**
     * @return The saturatedFatServing
     */
    public String getSaturatedFatServing() {
        return saturatedFatServing;
    }

    /**
     * @param saturatedFatServing The saturated-fat_serving
     */
    public void setSaturatedFatServing(String saturatedFatServing) {
        this.saturatedFatServing = saturatedFatServing;
    }

    public Nutriments withSaturatedFatServing(String saturatedFatServing) {
        this.saturatedFatServing = saturatedFatServing;
        return this;
    }

    /**
     * @return The saturatedFatUnit
     */
    public String getSaturatedFatUnit() {
        return saturatedFatUnit;
    }

    /**
     * @param saturatedFatUnit The saturated-fat_unit
     */
    public void setSaturatedFatUnit(String saturatedFatUnit) {
        this.saturatedFatUnit = saturatedFatUnit;
    }

    public Nutriments withSaturatedFatUnit(String saturatedFatUnit) {
        this.saturatedFatUnit = saturatedFatUnit;
        return this;
    }

    /**
     * @return The fiberUnit
     */
    public String getFiberUnit() {
        return fiberUnit;
    }

    /**
     * @param fiberUnit The fiber_unit
     */
    public void setFiberUnit(String fiberUnit) {
        this.fiberUnit = fiberUnit;
    }

    public Nutriments withFiberUnit(String fiberUnit) {
        this.fiberUnit = fiberUnit;
        return this;
    }

    /**
     * @return The energyValue
     */
    public String getEnergyValue() {
        return energyValue;
    }

    /**
     * @param energyValue The energy_value
     */
    public void setEnergyValue(String energyValue) {
        this.energyValue = energyValue;
    }

    public Nutriments withEnergyValue(String energyValue) {
        this.energyValue = energyValue;
        return this;
    }

    /**
     * @return The energy
     */
    public String getEnergy() {
        return energy;
    }

    /**
     * @param energy The energy
     */
    public void setEnergy(String energy) {
        this.energy = energy;
    }

    public Nutriments withEnergy(String energy) {
        this.energy = energy;
        return this;
    }

    /**
     * @return The energyUnit
     */
    public String getEnergyUnit() {
        return energyUnit;
    }

    /**
     * @param energyUnit The energy_unit
     */
    public void setEnergyUnit(String energyUnit) {
        this.energyUnit = energyUnit;
    }

    public Nutriments withEnergyUnit(String energyUnit) {
        this.energyUnit = energyUnit;
        return this;
    }

    /**
     * @return The sugarsServing
     */
    public String getSugarsServing() {
        return sugarsServing;
    }

    /**
     * @param sugarsServing The sugars_serving
     */
    public void setSugarsServing(String sugarsServing) {
        this.sugarsServing = sugarsServing;
    }

    public Nutriments withSugarsServing(String sugarsServing) {
        this.sugarsServing = sugarsServing;
        return this;
    }

    /**
     * @return The carbohydrates100g
     */
    public String getCarbohydrates100g() {
        return carbohydrates100g;
    }

    /**
     * @param carbohydrates100g The carbohydrates_100g
     */
    public void setCarbohydrates100g(String carbohydrates100g) {
        this.carbohydrates100g = carbohydrates100g;
    }

    public Nutriments withCarbohydrates100g(String carbohydrates100g) {
        this.carbohydrates100g = carbohydrates100g;
        return this;
    }

    /**
     * @return The nutritionScoreUk
     */
    public String getNutritionScoreUk() {
        return nutritionScoreUk;
    }

    /**
     * @param nutritionScoreUk The nutrition-score-uk
     */
    public void setNutritionScoreUk(String nutritionScoreUk) {
        this.nutritionScoreUk = nutritionScoreUk;
    }

    public Nutriments withNutritionScoreUk(String nutritionScoreUk) {
        this.nutritionScoreUk = nutritionScoreUk;
        return this;
    }

    /**
     * @return The saturatedFatValue
     */
    public String getSaturatedFatValue() {
        return saturatedFatValue;
    }

    /**
     * @param saturatedFatValue The saturated-fat_value
     */
    public void setSaturatedFatValue(String saturatedFatValue) {
        this.saturatedFatValue = saturatedFatValue;
    }

    public Nutriments withSaturatedFatValue(String saturatedFatValue) {
        this.saturatedFatValue = saturatedFatValue;
        return this;
    }

    /**
     * @return The proteins100g
     */
    public String getProteins100g() {
        return proteins100g;
    }

    /**
     * @param proteins100g The proteins_100g
     */
    public void setProteins100g(String proteins100g) {
        this.proteins100g = proteins100g;
    }

    public Nutriments withProteins100g(String proteins100g) {
        this.proteins100g = proteins100g;
        return this;
    }

    /**
     * @return The fiberServing
     */
    public String getFiberServing() {
        return fiberServing;
    }

    /**
     * @param fiberServing The fiber_serving
     */
    public void setFiberServing(String fiberServing) {
        this.fiberServing = fiberServing;
    }

    public Nutriments withFiberServing(String fiberServing) {
        this.fiberServing = fiberServing;
        return this;
    }

    /**
     * @return The carbohydratesServing
     */
    public String getCarbohydratesServing() {
        return carbohydratesServing;
    }

    /**
     * @param carbohydratesServing The carbohydrates_serving
     */
    public void setCarbohydratesServing(String carbohydratesServing) {
        this.carbohydratesServing = carbohydratesServing;
    }

    public Nutriments withCarbohydratesServing(String carbohydratesServing) {
        this.carbohydratesServing = carbohydratesServing;
        return this;
    }

    /**
     * @return The sodiumValue
     */
    public String getSodiumValue() {
        return sodiumValue;
    }

    /**
     * @param sodiumValue The sodium_value
     */
    public void setSodiumValue(String sodiumValue) {
        this.sodiumValue = sodiumValue;
    }

    public Nutriments withSodiumValue(String sodiumValue) {
        this.sodiumValue = sodiumValue;
        return this;
    }

    /**
     * @return The saltValue
     */
    public String getSaltValue() {
        return saltValue;
    }

    /**
     * @param saltValue The salt_value
     */
    public void setSaltValue(String saltValue) {
        this.saltValue = saltValue;
    }

    public Nutriments withSaltValue(String saltValue) {
        this.saltValue = saltValue;
        return this;
    }

    /**
     * @return The energyServing
     */
    public String getEnergyServing() {
        return energyServing;
    }

    /**
     * @param energyServing The energy_serving
     */
    public void setEnergyServing(String energyServing) {
        this.energyServing = energyServing;
    }

    public Nutriments withEnergyServing(String energyServing) {
        this.energyServing = energyServing;
        return this;
    }

    /**
     * @return The fat100g
     */
    public String getFat100g() {
        return fat100g;
    }

    /**
     * @param fat100g The fat_100g
     */
    public void setFat100g(String fat100g) {
        this.fat100g = fat100g;
    }

    public Nutriments withFat100g(String fat100g) {
        this.fat100g = fat100g;
        return this;
    }

    /**
     * @return The saturatedFat100g
     */
    public String getSaturatedFat100g() {
        return saturatedFat100g;
    }

    /**
     * @param saturatedFat100g The saturated-fat_100g
     */
    public void setSaturatedFat100g(String saturatedFat100g) {
        this.saturatedFat100g = saturatedFat100g;
    }

    public Nutriments withSaturatedFat100g(String saturatedFat100g) {
        this.saturatedFat100g = saturatedFat100g;
        return this;
    }

    /**
     * @return The nutritionScoreUk100g
     */
    public String getNutritionScoreUk100g() {
        return nutritionScoreUk100g;
    }

    /**
     * @param nutritionScoreUk100g The nutrition-score-uk_100g
     */
    public void setNutritionScoreUk100g(String nutritionScoreUk100g) {
        this.nutritionScoreUk100g = nutritionScoreUk100g;
    }

    public Nutriments withNutritionScoreUk100g(String nutritionScoreUk100g) {
        this.nutritionScoreUk100g = nutritionScoreUk100g;
        return this;
    }

    /**
     * @return The fiber
     */
    public String getFiber() {
        return fiber;
    }

    /**
     * @param fiber The fiber
     */
    public void setFiber(String fiber) {
        this.fiber = fiber;
    }

    public Nutriments withFiber(String fiber) {
        this.fiber = fiber;
        return this;
    }

    /**
     * @return The salt100g
     */
    public String getSalt100g() {
        return salt100g;
    }

    /**
     * @param salt100g The salt_100g
     */
    public void setSalt100g(String salt100g) {
        this.salt100g = salt100g;
    }

    public Nutriments withSalt100g(String salt100g) {
        this.salt100g = salt100g;
        return this;
    }

    /**
     * @return The saltServing
     */
    public String getSaltServing() {
        return saltServing;
    }

    /**
     * @param saltServing The salt_serving
     */
    public void setSaltServing(String saltServing) {
        this.saltServing = saltServing;
    }

    public Nutriments withSaltServing(String saltServing) {
        this.saltServing = saltServing;
        return this;
    }

    /**
     * @return The fiber100g
     */
    public String getFiber100g() {
        return fiber100g;
    }

    /**
     * @param fiber100g The fiber_100g
     */
    public void setFiber100g(String fiber100g) {
        this.fiber100g = fiber100g;
    }

    public Nutriments withFiber100g(String fiber100g) {
        this.fiber100g = fiber100g;
        return this;
    }

    /**
     * @return The carbohydrates
     */
    public String getCarbohydrates() {
        return carbohydrates;
    }

    /**
     * @param carbohydrates The carbohydrates
     */
    public void setCarbohydrates(String carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public Nutriments withCarbohydrates(String carbohydrates) {
        this.carbohydrates = carbohydrates;
        return this;
    }

    /**
     * @return The energy100g
     */
    public String getEnergy100g() {
        return energy100g;
    }

    /**
     * @param energy100g The energy_100g
     */
    public void setEnergy100g(String energy100g) {
        this.energy100g = energy100g;
    }

    public Nutriments withEnergy100g(String energy100g) {
        this.energy100g = energy100g;
        return this;
    }

    /**
     * @return The proteinsValue
     */
    public String getProteinsValue() {
        return proteinsValue;
    }

    /**
     * @param proteinsValue The proteins_value
     */
    public void setProteinsValue(String proteinsValue) {
        this.proteinsValue = proteinsValue;
    }

    public Nutriments withProteinsValue(String proteinsValue) {
        this.proteinsValue = proteinsValue;
        return this;
    }

    /**
     * @return The saturatedFat
     */
    public String getSaturatedFat() {
        return saturatedFat;
    }

    /**
     * @param saturatedFat The saturated-fat
     */
    public void setSaturatedFat(String saturatedFat) {
        this.saturatedFat = saturatedFat;
    }

    public Nutriments withSaturatedFat(String saturatedFat) {
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

    public Nutriments withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

    @Override
    public String toString() {
        return "Nutriments{" +
                "sodium='" + sodium + '\'' +
                ", sugars='" + sugars + '\'' +
                ", sugarsValue='" + sugarsValue + '\'' +
                ", carbohydratesUnit='" + carbohydratesUnit + '\'' +
                ", fatUnit='" + fatUnit + '\'' +
                ", proteinsUnit='" + proteinsUnit + '\'' +
                ", nutritionScoreFr100g='" + nutritionScoreFr100g + '\'' +
                ", fat='" + fat + '\'' +
                ", carbohydratesValue='" + carbohydratesValue + '\'' +
                ", proteinsServing='" + proteinsServing + '\'' +
                ", sodiumServing='" + sodiumServing + '\'' +
                ", fiberValue='" + fiberValue + '\'' +
                ", proteins='" + proteins + '\'' +
                ", salt='" + salt + '\'' +
                ", saltUnit='" + saltUnit + '\'' +
                ", nutritionScoreFr='" + nutritionScoreFr + '\'' +
                ", sugarsUnit='" + sugarsUnit + '\'' +
                ", fatServing='" + fatServing + '\'' +
                ", fatValue='" + fatValue + '\'' +
                ", sodiumUnit='" + sodiumUnit + '\'' +
                ", sugars100g='" + sugars100g + '\'' +
                ", sodium100g='" + sodium100g + '\'' +
                ", saturatedFatServing='" + saturatedFatServing + '\'' +
                ", saturatedFatUnit='" + saturatedFatUnit + '\'' +
                ", fiberUnit='" + fiberUnit + '\'' +
                ", energyValue='" + energyValue + '\'' +
                ", energy='" + energy + '\'' +
                ", energyUnit='" + energyUnit + '\'' +
                ", sugarsServing='" + sugarsServing + '\'' +
                ", carbohydrates100g='" + carbohydrates100g + '\'' +
                ", nutritionScoreUk='" + nutritionScoreUk + '\'' +
                ", saturatedFatValue='" + saturatedFatValue + '\'' +
                ", proteins100g='" + proteins100g + '\'' +
                ", fiberServing='" + fiberServing + '\'' +
                ", carbohydratesServing='" + carbohydratesServing + '\'' +
                ", sodiumValue='" + sodiumValue + '\'' +
                ", saltValue='" + saltValue + '\'' +
                ", energyServing='" + energyServing + '\'' +
                ", fat100g='" + fat100g + '\'' +
                ", saturatedFat100g='" + saturatedFat100g + '\'' +
                ", nutritionScoreUk100g='" + nutritionScoreUk100g + '\'' +
                ", fiber='" + fiber + '\'' +
                ", salt100g='" + salt100g + '\'' +
                ", saltServing='" + saltServing + '\'' +
                ", fiber100g='" + fiber100g + '\'' +
                ", carbohydrates='" + carbohydrates + '\'' +
                ", energy100g='" + energy100g + '\'' +
                ", proteinsValue='" + proteinsValue + '\'' +
                ", saturatedFat='" + saturatedFat + '\'' +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}