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
        "serum-proteins_100g",
        "casein",
        "magnesium",
        "fiber_unit",
        "magnesium_value",
        "maltodextrins_value",
        "proteins_unit",
        "maltodextrins_label",
        "saturated-fat_unit",
        "sodium",
        "carbohydrates_100g",
        "taurine_unit",
        "carbohydrates",
        "linoleic-acid_unit",
        "energy_100g",
        "potassium_value",
        "calcium_serving",
        "sodium_100g",
        "serum-proteins_unit",
        "maltodextrins_serving",
        "casein_unit",
        "nucleotides_unit",
        "calcium_value",
        "sugars_unit",
        "sodium_unit",
        "salt_unit",
        "lactose",
        "alpha-linolenic-acid_100g",
        "potassium_unit",
        "nucleotides_serving",
        "fat_unit",
        "linoleic-acid_value",
        "arachidonic-acid",
        "potassium_100g",
        "linoleic-acid_label",
        "energy_value",
        "energy_unit",
        "chlore_label",
        "lactose_label",
        "proteins",
        "lactose_serving",
        "sugars_value",
        "casein_value",
        "serum-proteins_label",
        "serum-proteins",
        "salt",
        "docosahexaenoic-acid_value",
        "nucleotides_value",
        "proteins_100g",
        "fiber_serving",
        "arachidonic-acid_unit",
        "nutrition-score-uk",
        "nucleotides",
        "sodium_value",
        "calcium",
        "chlore_serving",
        "arachidonic-acid_100g",
        "saturated-fat",
        "magnesium_100g",
        "fat_value",
        "docosahexaenoic-acid_100g",
        "chlore",
        "carbohydrates_value",
        "nutrition-score-fr",
        "sugars",
        "taurine_value",
        "fiber_100g",
        "alpha-linolenic-acid_unit",
        "taurine_label",
        "chlore_value",
        "fiber",
        "sugars_serving",
        "magnesium_label",
        "magnesium_serving",
        "alpha-linolenic-acid",
        "lactose_unit",
        "saturated-fat_serving",
        "docosahexaenoic-acid_unit",
        "lactose_100g",
        "sugars_100g",
        "saturated-fat_100g",
        "alpha-linolenic-acid_value",
        "serum-proteins_value",
        "potassium_serving",
        "maltodextrins",
        "taurine",
        "salt_100g",
        "potassium_label",
        "linoleic-acid_serving",
        "carbohydrates_unit",
        "calcium_unit",
        "calcium_label",
        "saturated-fat_value",
        "docosahexaenoic-acid_label",
        "chlore_100g",
        "calcium_100g",
        "nucleotides_label",
        "arachidonic-acid_label",
        "nucleotides_100g",
        "casein_100g",
        "casein_label",
        "arachidonic-acid_serving",
        "fiber_value",
        "chlore_unit",
        "maltodextrins_unit",
        "serum-proteins_serving",
        "proteins_serving",
        "linoleic-acid",
        "alpha-linolenic-acid_label",
        "arachidonic-acid_value",
        "taurine_100g",
        "maltodextrins_100g",
        "lactose_value",
        "taurine_serving",
        "nutrition-score-uk_100g",
        "fat",
        "linoleic-acid_100g",
        "fat_serving",
        "fat_100g",
        "nutrition-score-fr_100g",
        "alpha-linolenic-acid_serving",
        "salt_serving",
        "carbohydrates_serving",
        "casein_serving",
        "docosahexaenoic-acid",
        "energy_serving",
        "magnesium_unit",
        "potassium",
        "proteins_value",
        "energy",
        "sodium_serving",
        "docosahexaenoic-acid_serving"
})
public class Nutriments implements Serializable {

    @JsonProperty("serum-proteins_100g")
    private String serumProteins100g;
    @JsonProperty("casein")
    private String casein;
    @JsonProperty("magnesium")
    private String magnesium;
    @JsonProperty("fiber_unit")
    private String fiberUnit;
    @JsonProperty("magnesium_value")
    private String magnesiumValue;
    @JsonProperty("maltodextrins_value")
    private String maltodextrinsValue;
    @JsonProperty("proteins_unit")
    private String proteinsUnit;
    @JsonProperty("maltodextrins_label")
    private String maltodextrinsLabel;
    @JsonProperty("saturated-fat_unit")
    private String saturatedFatUnit;
    @JsonProperty("sodium")
    private String sodium;
    @JsonProperty("carbohydrates_100g")
    private String carbohydrates100g;
    @JsonProperty("taurine_unit")
    private String taurineUnit;
    @JsonProperty("carbohydrates")
    private String carbohydrates;
    @JsonProperty("linoleic-acid_unit")
    private String linoleicAcidUnit;
    @JsonProperty("energy_100g")
    private String energy100g;
    @JsonProperty("potassium_value")
    private String potassiumValue;
    @JsonProperty("calcium_serving")
    private String calciumServing;
    @JsonProperty("sodium_100g")
    private String sodium100g;
    @JsonProperty("serum-proteins_unit")
    private String serumProteinsUnit;
    @JsonProperty("maltodextrins_serving")
    private String maltodextrinsServing;
    @JsonProperty("casein_unit")
    private String caseinUnit;
    @JsonProperty("nucleotides_unit")
    private String nucleotidesUnit;
    @JsonProperty("calcium_value")
    private String calciumValue;
    @JsonProperty("sugars_unit")
    private String sugarsUnit;
    @JsonProperty("sodium_unit")
    private String sodiumUnit;
    @JsonProperty("salt_unit")
    private String saltUnit;
    @JsonProperty("lactose")
    private String lactose;
    @JsonProperty("alpha-linolenic-acid_100g")
    private String alphaLinolenicAcid100g;
    @JsonProperty("potassium_unit")
    private String potassiumUnit;
    @JsonProperty("nucleotides_serving")
    private String nucleotidesServing;
    @JsonProperty("fat_unit")
    private String fatUnit;
    @JsonProperty("linoleic-acid_value")
    private String linoleicAcidValue;
    @JsonProperty("arachidonic-acid")
    private String arachidonicAcid;
    @JsonProperty("potassium_100g")
    private String potassium100g;
    @JsonProperty("linoleic-acid_label")
    private String linoleicAcidLabel;
    @JsonProperty("energy_value")
    private String energyValue;
    @JsonProperty("energy_unit")
    private String energyUnit;
    @JsonProperty("chlore_label")
    private String chloreLabel;
    @JsonProperty("lactose_label")
    private String lactoseLabel;
    @JsonProperty("proteins")
    private String proteins;
    @JsonProperty("lactose_serving")
    private String lactoseServing;
    @JsonProperty("sugars_value")
    private String sugarsValue;
    @JsonProperty("casein_value")
    private String caseinValue;
    @JsonProperty("serum-proteins_label")
    private String serumProteinsLabel;
    @JsonProperty("serum-proteins")
    private String serumProteins;
    @JsonProperty("salt")
    private String salt;
    @JsonProperty("docosahexaenoic-acid_value")
    private String docosahexaenoicAcidValue;
    @JsonProperty("nucleotides_value")
    private String nucleotidesValue;
    @JsonProperty("proteins_100g")
    private String proteins100g;
    @JsonProperty("fiber_serving")
    private String fiberServing;
    @JsonProperty("arachidonic-acid_unit")
    private String arachidonicAcidUnit;
    @JsonProperty("nutrition-score-uk")
    private String nutritionScoreUk;
    @JsonProperty("nucleotides")
    private String nucleotides;
    @JsonProperty("sodium_value")
    private String sodiumValue;
    @JsonProperty("calcium")
    private String calcium;
    @JsonProperty("chlore_serving")
    private String chloreServing;
    @JsonProperty("arachidonic-acid_100g")
    private String arachidonicAcid100g;
    @JsonProperty("saturated-fat")
    private String saturatedFat;
    @JsonProperty("magnesium_100g")
    private String magnesium100g;
    @JsonProperty("fat_value")
    private String fatValue;
    @JsonProperty("docosahexaenoic-acid_100g")
    private String docosahexaenoicAcid100g;
    @JsonProperty("chlore")
    private String chlore;
    @JsonProperty("carbohydrates_value")
    private String carbohydratesValue;
    @JsonProperty("nutrition-score-fr")
    private String nutritionScoreFr;
    @JsonProperty("sugars")
    private String sugars;
    @JsonProperty("taurine_value")
    private String taurineValue;
    @JsonProperty("fiber_100g")
    private String fiber100g;
    @JsonProperty("alpha-linolenic-acid_unit")
    private String alphaLinolenicAcidUnit;
    @JsonProperty("taurine_label")
    private String taurineLabel;
    @JsonProperty("chlore_value")
    private String chloreValue;
    @JsonProperty("fiber")
    private String fiber;
    @JsonProperty("sugars_serving")
    private String sugarsServing;
    @JsonProperty("magnesium_label")
    private String magnesiumLabel;
    @JsonProperty("magnesium_serving")
    private String magnesiumServing;
    @JsonProperty("alpha-linolenic-acid")
    private String alphaLinolenicAcid;
    @JsonProperty("lactose_unit")
    private String lactoseUnit;
    @JsonProperty("saturated-fat_serving")
    private String saturatedFatServing;
    @JsonProperty("docosahexaenoic-acid_unit")
    private String docosahexaenoicAcidUnit;
    @JsonProperty("lactose_100g")
    private String lactose100g;
    @JsonProperty("sugars_100g")
    private String sugars100g;
    @JsonProperty("saturated-fat_100g")
    private String saturatedFat100g;
    @JsonProperty("alpha-linolenic-acid_value")
    private String alphaLinolenicAcidValue;
    @JsonProperty("serum-proteins_value")
    private String serumProteinsValue;
    @JsonProperty("potassium_serving")
    private String potassiumServing;
    @JsonProperty("maltodextrins")
    private String maltodextrins;
    @JsonProperty("taurine")
    private String taurine;
    @JsonProperty("salt_100g")
    private String salt100g;
    @JsonProperty("potassium_label")
    private String potassiumLabel;
    @JsonProperty("linoleic-acid_serving")
    private String linoleicAcidServing;
    @JsonProperty("carbohydrates_unit")
    private String carbohydratesUnit;
    @JsonProperty("calcium_unit")
    private String calciumUnit;
    @JsonProperty("calcium_label")
    private String calciumLabel;
    @JsonProperty("saturated-fat_value")
    private String saturatedFatValue;
    @JsonProperty("docosahexaenoic-acid_label")
    private String docosahexaenoicAcidLabel;
    @JsonProperty("chlore_100g")
    private String chlore100g;
    @JsonProperty("calcium_100g")
    private String calcium100g;
    @JsonProperty("nucleotides_label")
    private String nucleotidesLabel;
    @JsonProperty("arachidonic-acid_label")
    private String arachidonicAcidLabel;
    @JsonProperty("nucleotides_100g")
    private String nucleotides100g;
    @JsonProperty("casein_100g")
    private String casein100g;
    @JsonProperty("casein_label")
    private String caseinLabel;
    @JsonProperty("arachidonic-acid_serving")
    private String arachidonicAcidServing;
    @JsonProperty("fiber_value")
    private String fiberValue;
    @JsonProperty("chlore_unit")
    private String chloreUnit;
    @JsonProperty("maltodextrins_unit")
    private String maltodextrinsUnit;
    @JsonProperty("serum-proteins_serving")
    private String serumProteinsServing;
    @JsonProperty("proteins_serving")
    private String proteinsServing;
    @JsonProperty("linoleic-acid")
    private String linoleicAcid;
    @JsonProperty("alpha-linolenic-acid_label")
    private String alphaLinolenicAcidLabel;
    @JsonProperty("arachidonic-acid_value")
    private String arachidonicAcidValue;
    @JsonProperty("taurine_100g")
    private String taurine100g;
    @JsonProperty("maltodextrins_100g")
    private String maltodextrins100g;
    @JsonProperty("lactose_value")
    private String lactoseValue;
    @JsonProperty("taurine_serving")
    private String taurineServing;
    @JsonProperty("nutrition-score-uk_100g")
    private String nutritionScoreUk100g;
    @JsonProperty("fat")
    private String fat;
    @JsonProperty("linoleic-acid_100g")
    private String linoleicAcid100g;
    @JsonProperty("fat_serving")
    private String fatServing;
    @JsonProperty("fat_100g")
    private String fat100g;
    @JsonProperty("nutrition-score-fr_100g")
    private String nutritionScoreFr100g;
    @JsonProperty("alpha-linolenic-acid_serving")
    private String alphaLinolenicAcidServing;
    @JsonProperty("salt_serving")
    private String saltServing;
    @JsonProperty("carbohydrates_serving")
    private String carbohydratesServing;
    @JsonProperty("casein_serving")
    private String caseinServing;
    @JsonProperty("docosahexaenoic-acid")
    private String docosahexaenoicAcid;
    @JsonProperty("energy_serving")
    private String energyServing;
    @JsonProperty("magnesium_unit")
    private String magnesiumUnit;
    @JsonProperty("potassium")
    private String potassium;
    @JsonProperty("proteins_value")
    private String proteinsValue;
    @JsonProperty("energy")
    private String energy;
    @JsonProperty("sodium_serving")
    private String sodiumServing;
    @JsonProperty("docosahexaenoic-acid_serving")
    private String docosahexaenoicAcidServing;
    @JsonProperty("carbon-footprint_unit")
    private String carbonFootprintUnit;
    @JsonProperty("carbon-footprint_100g")
    private String carbonFootprint100g;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getCarbonFootprint100g() {
        return carbonFootprint100g;
    }

    public void setCarbonFootprint100g(String carbonFootprint100g) {
        this.carbonFootprint100g = carbonFootprint100g;
    }

    public String getCarbonFootprintUnit() {
        return carbonFootprintUnit;
    }

    public void setCarbonFootprintUnit(String carbonFootprintUnit) {
        this.carbonFootprintUnit = carbonFootprintUnit;
    }

    /**
     *
     * @return
     * The serumProteins100g
     */
    @JsonProperty("serum-proteins_100g")
    public String getSerumProteins100g() {
        return serumProteins100g;
    }

    /**
     *
     * @param serumProteins100g
     * The serum-proteins_100g
     */
    @JsonProperty("serum-proteins_100g")
    public void setSerumProteins100g(String serumProteins100g) {
        this.serumProteins100g = serumProteins100g;
    }

    /**
     *
     * @return
     * The casein
     */
    @JsonProperty("casein")
    public String getCasein() {
        return casein;
    }

    /**
     *
     * @param casein
     * The casein
     */
    @JsonProperty("casein")
    public void setCasein(String casein) {
        this.casein = casein;
    }

    /**
     *
     * @return
     * The magnesium
     */
    @JsonProperty("magnesium")
    public String getMagnesium() {
        return magnesium;
    }

    /**
     *
     * @param magnesium
     * The magnesium
     */
    @JsonProperty("magnesium")
    public void setMagnesium(String magnesium) {
        this.magnesium = magnesium;
    }

    /**
     *
     * @return
     * The fiberUnit
     */
    @JsonProperty("fiber_unit")
    public String getFiberUnit() {
        return fiberUnit;
    }

    /**
     *
     * @param fiberUnit
     * The fiber_unit
     */
    @JsonProperty("fiber_unit")
    public void setFiberUnit(String fiberUnit) {
        this.fiberUnit = fiberUnit;
    }

    /**
     *
     * @return
     * The magnesiumValue
     */
    @JsonProperty("magnesium_value")
    public String getMagnesiumValue() {
        return magnesiumValue;
    }

    /**
     *
     * @param magnesiumValue
     * The magnesium_value
     */
    @JsonProperty("magnesium_value")
    public void setMagnesiumValue(String magnesiumValue) {
        this.magnesiumValue = magnesiumValue;
    }

    /**
     *
     * @return
     * The maltodextrinsValue
     */
    @JsonProperty("maltodextrins_value")
    public String getMaltodextrinsValue() {
        return maltodextrinsValue;
    }

    /**
     *
     * @param maltodextrinsValue
     * The maltodextrins_value
     */
    @JsonProperty("maltodextrins_value")
    public void setMaltodextrinsValue(String maltodextrinsValue) {
        this.maltodextrinsValue = maltodextrinsValue;
    }

    /**
     *
     * @return
     * The proteinsUnit
     */
    @JsonProperty("proteins_unit")
    public String getProteinsUnit() {
        return proteinsUnit;
    }

    /**
     *
     * @param proteinsUnit
     * The proteins_unit
     */
    @JsonProperty("proteins_unit")
    public void setProteinsUnit(String proteinsUnit) {
        this.proteinsUnit = proteinsUnit;
    }

    /**
     *
     * @return
     * The maltodextrinsLabel
     */
    @JsonProperty("maltodextrins_label")
    public String getMaltodextrinsLabel() {
        return maltodextrinsLabel;
    }

    /**
     *
     * @return
     * The salt_unit
     */
    @JsonProperty("salt_unit")
    public String getSaltUnit() {
        return saltUnit;
    }

    /**
     *
     * @param saltUnit
     * The salt_unit
     */
    @JsonProperty("salt_unit")
    public void setSaltUnit(String saltUnit) {
        this.saltUnit = saltUnit;
    }

    /**
     *
     * @param maltodextrinsLabel
     * The maltodextrins_label
     */
    @JsonProperty("maltodextrins_label")
    public void setMaltodextrinsLabel(String maltodextrinsLabel) {
        this.maltodextrinsLabel = maltodextrinsLabel;
    }

    /**
     *
     * @return
     * The saturatedFatUnit
     */
    @JsonProperty("saturated-fat_unit")
    public String getSaturatedFatUnit() {
        return saturatedFatUnit;
    }

    /**
     *
     * @param saturatedFatUnit
     * The saturated-fat_unit
     */
    @JsonProperty("saturated-fat_unit")
    public void setSaturatedFatUnit(String saturatedFatUnit) {
        this.saturatedFatUnit = saturatedFatUnit;
    }

    /**
     *
     * @return
     * The sodium
     */
    @JsonProperty("sodium")
    public String getSodium() {
        return sodium;
    }

    /**
     *
     * @param sodium
     * The sodium
     */
    @JsonProperty("sodium")
    public void setSodium(String sodium) {
        this.sodium = sodium;
    }

    /**
     *
     * @return
     * The carbohydrates100g
     */
    @JsonProperty("carbohydrates_100g")
    public String getCarbohydrates100g() {
        return carbohydrates100g;
    }

    /**
     *
     * @param carbohydrates100g
     * The carbohydrates_100g
     */
    @JsonProperty("carbohydrates_100g")
    public void setCarbohydrates100g(String carbohydrates100g) {
        this.carbohydrates100g = carbohydrates100g;
    }

    /**
     *
     * @return
     * The taurineUnit
     */
    @JsonProperty("taurine_unit")
    public String getTaurineUnit() {
        return taurineUnit;
    }

    /**
     *
     * @param taurineUnit
     * The taurine_unit
     */
    @JsonProperty("taurine_unit")
    public void setTaurineUnit(String taurineUnit) {
        this.taurineUnit = taurineUnit;
    }

    /**
     *
     * @return
     * The carbohydrates
     */
    @JsonProperty("carbohydrates")
    public String getCarbohydrates() {
        return carbohydrates;
    }

    /**
     *
     * @param carbohydrates
     * The carbohydrates
     */
    @JsonProperty("carbohydrates")
    public void setCarbohydrates(String carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    /**
     *
     * @return
     * The linoleicAcidUnit
     */
    @JsonProperty("linoleic-acid_unit")
    public String getLinoleicAcidUnit() {
        return linoleicAcidUnit;
    }

    /**
     *
     * @param linoleicAcidUnit
     * The linoleic-acid_unit
     */
    @JsonProperty("linoleic-acid_unit")
    public void setLinoleicAcidUnit(String linoleicAcidUnit) {
        this.linoleicAcidUnit = linoleicAcidUnit;
    }

    /**
     *
     * @return
     * The energy100g
     */
    @JsonProperty("energy_100g")
    public String getEnergy100g() {
        return energy100g;
    }

    /**
     *
     * @param energy100g
     * The energy_100g
     */
    @JsonProperty("energy_100g")
    public void setEnergy100g(String energy100g) {
        this.energy100g = energy100g;
    }

    /**
     *
     * @return
     * The potassiumValue
     */
    @JsonProperty("potassium_value")
    public String getPotassiumValue() {
        return potassiumValue;
    }

    /**
     *
     * @param potassiumValue
     * The potassium_value
     */
    @JsonProperty("potassium_value")
    public void setPotassiumValue(String potassiumValue) {
        this.potassiumValue = potassiumValue;
    }

    /**
     *
     * @return
     * The calciumServing
     */
    @JsonProperty("calcium_serving")
    public String getCalciumServing() {
        return calciumServing;
    }

    /**
     *
     * @param calciumServing
     * The calcium_serving
     */
    @JsonProperty("calcium_serving")
    public void setCalciumServing(String calciumServing) {
        this.calciumServing = calciumServing;
    }

    /**
     *
     * @return
     * The sodium100g
     */
    @JsonProperty("sodium_100g")
    public String getSodium100g() {
        return sodium100g;
    }

    /**
     *
     * @param sodium100g
     * The sodium_100g
     */
    @JsonProperty("sodium_100g")
    public void setSodium100g(String sodium100g) {
        this.sodium100g = sodium100g;
    }

    /**
     *
     * @return
     * The serumProteinsUnit
     */
    @JsonProperty("serum-proteins_unit")
    public String getSerumProteinsUnit() {
        return serumProteinsUnit;
    }

    /**
     *
     * @param serumProteinsUnit
     * The serum-proteins_unit
     */
    @JsonProperty("serum-proteins_unit")
    public void setSerumProteinsUnit(String serumProteinsUnit) {
        this.serumProteinsUnit = serumProteinsUnit;
    }

    /**
     *
     * @return
     * The maltodextrinsServing
     */
    @JsonProperty("maltodextrins_serving")
    public String getMaltodextrinsServing() {
        return maltodextrinsServing;
    }

    /**
     *
     * @param maltodextrinsServing
     * The maltodextrins_serving
     */
    @JsonProperty("maltodextrins_serving")
    public void setMaltodextrinsServing(String maltodextrinsServing) {
        this.maltodextrinsServing = maltodextrinsServing;
    }

    /**
     *
     * @return
     * The caseinUnit
     */
    @JsonProperty("casein_unit")
    public String getCaseinUnit() {
        return caseinUnit;
    }

    /**
     *
     * @param caseinUnit
     * The casein_unit
     */
    @JsonProperty("casein_unit")
    public void setCaseinUnit(String caseinUnit) {
        this.caseinUnit = caseinUnit;
    }

    /**
     *
     * @return
     * The nucleotidesUnit
     */
    @JsonProperty("nucleotides_unit")
    public String getNucleotidesUnit() {
        return nucleotidesUnit;
    }

    /**
     *
     * @param nucleotidesUnit
     * The nucleotides_unit
     */
    @JsonProperty("nucleotides_unit")
    public void setNucleotidesUnit(String nucleotidesUnit) {
        this.nucleotidesUnit = nucleotidesUnit;
    }

    /**
     *
     * @return
     * The calciumValue
     */
    @JsonProperty("calcium_value")
    public String getCalciumValue() {
        return calciumValue;
    }

    /**
     *
     * @param calciumValue
     * The calcium_value
     */
    @JsonProperty("calcium_value")
    public void setCalciumValue(String calciumValue) {
        this.calciumValue = calciumValue;
    }

    /**
     *
     * @return
     * The sugarsUnit
     */
    @JsonProperty("sugars_unit")
    public String getSugarsUnit() {
        return sugarsUnit;
    }

    /**
     *
     * @param sugarsUnit
     * The sugars_unit
     */
    @JsonProperty("sugars_unit")
    public void setSugarsUnit(String sugarsUnit) {
        this.sugarsUnit = sugarsUnit;
    }

    /**
     *
     * @return
     * The sodiumUnit
     */
    @JsonProperty("sodium_unit")
    public String getSodiumUnit() {
        return sodiumUnit;
    }

    /**
     *
     * @param sodiumUnit
     * The sodium_unit
     */
    @JsonProperty("sodium_unit")
    public void setSodiumUnit(String sodiumUnit) {
        this.sodiumUnit = sodiumUnit;
    }

    /**
     *
     * @return
     * The lactose
     */
    @JsonProperty("lactose")
    public String getLactose() {
        return lactose;
    }

    /**
     *
     * @param lactose
     * The lactose
     */
    @JsonProperty("lactose")
    public void setLactose(String lactose) {
        this.lactose = lactose;
    }

    /**
     *
     * @return
     * The alphaLinolenicAcid100g
     */
    @JsonProperty("alpha-linolenic-acid_100g")
    public String getAlphaLinolenicAcid100g() {
        return alphaLinolenicAcid100g;
    }

    /**
     *
     * @param alphaLinolenicAcid100g
     * The alpha-linolenic-acid_100g
     */
    @JsonProperty("alpha-linolenic-acid_100g")
    public void setAlphaLinolenicAcid100g(String alphaLinolenicAcid100g) {
        this.alphaLinolenicAcid100g = alphaLinolenicAcid100g;
    }

    /**
     *
     * @return
     * The potassiumUnit
     */
    @JsonProperty("potassium_unit")
    public String getPotassiumUnit() {
        return potassiumUnit;
    }

    /**
     *
     * @param potassiumUnit
     * The potassium_unit
     */
    @JsonProperty("potassium_unit")
    public void setPotassiumUnit(String potassiumUnit) {
        this.potassiumUnit = potassiumUnit;
    }

    /**
     *
     * @return
     * The nucleotidesServing
     */
    @JsonProperty("nucleotides_serving")
    public String getNucleotidesServing() {
        return nucleotidesServing;
    }

    /**
     *
     * @param nucleotidesServing
     * The nucleotides_serving
     */
    @JsonProperty("nucleotides_serving")
    public void setNucleotidesServing(String nucleotidesServing) {
        this.nucleotidesServing = nucleotidesServing;
    }

    /**
     *
     * @return
     * The fatUnit
     */
    @JsonProperty("fat_unit")
    public String getFatUnit() {
        return fatUnit;
    }

    /**
     *
     * @param fatUnit
     * The fat_unit
     */
    @JsonProperty("fat_unit")
    public void setFatUnit(String fatUnit) {
        this.fatUnit = fatUnit;
    }

    /**
     *
     * @return
     * The linoleicAcidValue
     */
    @JsonProperty("linoleic-acid_value")
    public String getLinoleicAcidValue() {
        return linoleicAcidValue;
    }

    /**
     *
     * @param linoleicAcidValue
     * The linoleic-acid_value
     */
    @JsonProperty("linoleic-acid_value")
    public void setLinoleicAcidValue(String linoleicAcidValue) {
        this.linoleicAcidValue = linoleicAcidValue;
    }

    /**
     *
     * @return
     * The arachidonicAcid
     */
    @JsonProperty("arachidonic-acid")
    public String getArachidonicAcid() {
        return arachidonicAcid;
    }

    /**
     *
     * @param arachidonicAcid
     * The arachidonic-acid
     */
    @JsonProperty("arachidonic-acid")
    public void setArachidonicAcid(String arachidonicAcid) {
        this.arachidonicAcid = arachidonicAcid;
    }

    /**
     *
     * @return
     * The potassium100g
     */
    @JsonProperty("potassium_100g")
    public String getPotassium100g() {
        return potassium100g;
    }

    /**
     *
     * @param potassium100g
     * The potassium_100g
     */
    @JsonProperty("potassium_100g")
    public void setPotassium100g(String potassium100g) {
        this.potassium100g = potassium100g;
    }

    /**
     *
     * @return
     * The linoleicAcidLabel
     */
    @JsonProperty("linoleic-acid_label")
    public String getLinoleicAcidLabel() {
        return linoleicAcidLabel;
    }

    /**
     *
     * @param linoleicAcidLabel
     * The linoleic-acid_label
     */
    @JsonProperty("linoleic-acid_label")
    public void setLinoleicAcidLabel(String linoleicAcidLabel) {
        this.linoleicAcidLabel = linoleicAcidLabel;
    }

    /**
     *
     * @return
     * The energyValue
     */
    @JsonProperty("energy_value")
    public String getEnergyValue() {
        return energyValue;
    }

    /**
     *
     * @param energyValue
     * The energy_value
     */
    @JsonProperty("energy_value")
    public void setEnergyValue(String energyValue) {
        this.energyValue = energyValue;
    }

    /**
     *
     * @return
     * The energyUnit
     */
    @JsonProperty("energy_unit")
    public String getEnergyUnit() {
        return energyUnit;
    }

    /**
     *
     * @param energyUnit
     * The energy_unit
     */
    @JsonProperty("energy_unit")
    public void setEnergyUnit(String energyUnit) {
        this.energyUnit = energyUnit;
    }

    /**
     *
     * @return
     * The chloreLabel
     */
    @JsonProperty("chlore_label")
    public String getChloreLabel() {
        return chloreLabel;
    }

    /**
     *
     * @param chloreLabel
     * The chlore_label
     */
    @JsonProperty("chlore_label")
    public void setChloreLabel(String chloreLabel) {
        this.chloreLabel = chloreLabel;
    }

    /**
     *
     * @return
     * The lactoseLabel
     */
    @JsonProperty("lactose_label")
    public String getLactoseLabel() {
        return lactoseLabel;
    }

    /**
     *
     * @param lactoseLabel
     * The lactose_label
     */
    @JsonProperty("lactose_label")
    public void setLactoseLabel(String lactoseLabel) {
        this.lactoseLabel = lactoseLabel;
    }

    /**
     *
     * @return
     * The proteins
     */
    @JsonProperty("proteins")
    public String getProteins() {
        return proteins;
    }

    /**
     *
     * @param proteins
     * The proteins
     */
    @JsonProperty("proteins")
    public void setProteins(String proteins) {
        this.proteins = proteins;
    }

    /**
     *
     * @return
     * The lactoseServing
     */
    @JsonProperty("lactose_serving")
    public String getLactoseServing() {
        return lactoseServing;
    }

    /**
     *
     * @param lactoseServing
     * The lactose_serving
     */
    @JsonProperty("lactose_serving")
    public void setLactoseServing(String lactoseServing) {
        this.lactoseServing = lactoseServing;
    }

    /**
     *
     * @return
     * The sugarsValue
     */
    @JsonProperty("sugars_value")
    public String getSugarsValue() {
        return sugarsValue;
    }

    /**
     *
     * @param sugarsValue
     * The sugars_value
     */
    @JsonProperty("sugars_value")
    public void setSugarsValue(String sugarsValue) {
        this.sugarsValue = sugarsValue;
    }

    /**
     *
     * @return
     * The caseinValue
     */
    @JsonProperty("casein_value")
    public String getCaseinValue() {
        return caseinValue;
    }

    /**
     *
     * @param caseinValue
     * The casein_value
     */
    @JsonProperty("casein_value")
    public void setCaseinValue(String caseinValue) {
        this.caseinValue = caseinValue;
    }

    /**
     *
     * @return
     * The serumProteinsLabel
     */
    @JsonProperty("serum-proteins_label")
    public String getSerumProteinsLabel() {
        return serumProteinsLabel;
    }

    /**
     *
     * @param serumProteinsLabel
     * The serum-proteins_label
     */
    @JsonProperty("serum-proteins_label")
    public void setSerumProteinsLabel(String serumProteinsLabel) {
        this.serumProteinsLabel = serumProteinsLabel;
    }

    /**
     *
     * @return
     * The serumProteins
     */
    @JsonProperty("serum-proteins")
    public String getSerumProteins() {
        return serumProteins;
    }

    /**
     *
     * @param serumProteins
     * The serum-proteins
     */
    @JsonProperty("serum-proteins")
    public void setSerumProteins(String serumProteins) {
        this.serumProteins = serumProteins;
    }

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

    /**
     *
     * @return
     * The docosahexaenoicAcidValue
     */
    @JsonProperty("docosahexaenoic-acid_value")
    public String getDocosahexaenoicAcidValue() {
        return docosahexaenoicAcidValue;
    }

    /**
     *
     * @param docosahexaenoicAcidValue
     * The docosahexaenoic-acid_value
     */
    @JsonProperty("docosahexaenoic-acid_value")
    public void setDocosahexaenoicAcidValue(String docosahexaenoicAcidValue) {
        this.docosahexaenoicAcidValue = docosahexaenoicAcidValue;
    }

    /**
     *
     * @return
     * The nucleotidesValue
     */
    @JsonProperty("nucleotides_value")
    public String getNucleotidesValue() {
        return nucleotidesValue;
    }

    /**
     *
     * @param nucleotidesValue
     * The nucleotides_value
     */
    @JsonProperty("nucleotides_value")
    public void setNucleotidesValue(String nucleotidesValue) {
        this.nucleotidesValue = nucleotidesValue;
    }

    /**
     *
     * @return
     * The proteins100g
     */
    @JsonProperty("proteins_100g")
    public String getProteins100g() {
        return proteins100g;
    }

    /**
     *
     * @param proteins100g
     * The proteins_100g
     */
    @JsonProperty("proteins_100g")
    public void setProteins100g(String proteins100g) {
        this.proteins100g = proteins100g;
    }

    /**
     *
     * @return
     * The fiberServing
     */
    @JsonProperty("fiber_serving")
    public String getFiberServing() {
        return fiberServing;
    }

    /**
     *
     * @param fiberServing
     * The fiber_serving
     */
    @JsonProperty("fiber_serving")
    public void setFiberServing(String fiberServing) {
        this.fiberServing = fiberServing;
    }

    /**
     *
     * @return
     * The arachidonicAcidUnit
     */
    @JsonProperty("arachidonic-acid_unit")
    public String getArachidonicAcidUnit() {
        return arachidonicAcidUnit;
    }

    /**
     *
     * @param arachidonicAcidUnit
     * The arachidonic-acid_unit
     */
    @JsonProperty("arachidonic-acid_unit")
    public void setArachidonicAcidUnit(String arachidonicAcidUnit) {
        this.arachidonicAcidUnit = arachidonicAcidUnit;
    }

    /**
     *
     * @return
     * The nutritionScoreUk
     */
    @JsonProperty("nutrition-score-uk")
    public String getNutritionScoreUk() {
        return nutritionScoreUk;
    }

    /**
     *
     * @param nutritionScoreUk
     * The nutrition-score-uk
     */
    @JsonProperty("nutrition-score-uk")
    public void setNutritionScoreUk(String nutritionScoreUk) {
        this.nutritionScoreUk = nutritionScoreUk;
    }

    /**
     *
     * @return
     * The nucleotides
     */
    @JsonProperty("nucleotides")
    public String getNucleotides() {
        return nucleotides;
    }

    /**
     *
     * @param nucleotides
     * The nucleotides
     */
    @JsonProperty("nucleotides")
    public void setNucleotides(String nucleotides) {
        this.nucleotides = nucleotides;
    }

    /**
     *
     * @return
     * The sodiumValue
     */
    @JsonProperty("sodium_value")
    public String getSodiumValue() {
        return sodiumValue;
    }

    /**
     *
     * @param sodiumValue
     * The sodium_value
     */
    @JsonProperty("sodium_value")
    public void setSodiumValue(String sodiumValue) {
        this.sodiumValue = sodiumValue;
    }

    /**
     *
     * @return
     * The calcium
     */
    @JsonProperty("calcium")
    public String getCalcium() {
        return calcium;
    }

    /**
     *
     * @param calcium
     * The calcium
     */
    @JsonProperty("calcium")
    public void setCalcium(String calcium) {
        this.calcium = calcium;
    }

    /**
     *
     * @return
     * The chloreServing
     */
    @JsonProperty("chlore_serving")
    public String getChloreServing() {
        return chloreServing;
    }

    /**
     *
     * @param chloreServing
     * The chlore_serving
     */
    @JsonProperty("chlore_serving")
    public void setChloreServing(String chloreServing) {
        this.chloreServing = chloreServing;
    }

    /**
     *
     * @return
     * The arachidonicAcid100g
     */
    @JsonProperty("arachidonic-acid_100g")
    public String getArachidonicAcid100g() {
        return arachidonicAcid100g;
    }

    /**
     *
     * @param arachidonicAcid100g
     * The arachidonic-acid_100g
     */
    @JsonProperty("arachidonic-acid_100g")
    public void setArachidonicAcid100g(String arachidonicAcid100g) {
        this.arachidonicAcid100g = arachidonicAcid100g;
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

    /**
     *
     * @return
     * The magnesium100g
     */
    @JsonProperty("magnesium_100g")
    public String getMagnesium100g() {
        return magnesium100g;
    }

    /**
     *
     * @param magnesium100g
     * The magnesium_100g
     */
    @JsonProperty("magnesium_100g")
    public void setMagnesium100g(String magnesium100g) {
        this.magnesium100g = magnesium100g;
    }

    /**
     *
     * @return
     * The fatValue
     */
    @JsonProperty("fat_value")
    public String getFatValue() {
        return fatValue;
    }

    /**
     *
     * @param fatValue
     * The fat_value
     */
    @JsonProperty("fat_value")
    public void setFatValue(String fatValue) {
        this.fatValue = fatValue;
    }

    /**
     *
     * @return
     * The docosahexaenoicAcid100g
     */
    @JsonProperty("docosahexaenoic-acid_100g")
    public String getDocosahexaenoicAcid100g() {
        return docosahexaenoicAcid100g;
    }

    /**
     *
     * @param docosahexaenoicAcid100g
     * The docosahexaenoic-acid_100g
     */
    @JsonProperty("docosahexaenoic-acid_100g")
    public void setDocosahexaenoicAcid100g(String docosahexaenoicAcid100g) {
        this.docosahexaenoicAcid100g = docosahexaenoicAcid100g;
    }

    /**
     *
     * @return
     * The chlore
     */
    @JsonProperty("chlore")
    public String getChlore() {
        return chlore;
    }

    /**
     *
     * @param chlore
     * The chlore
     */
    @JsonProperty("chlore")
    public void setChlore(String chlore) {
        this.chlore = chlore;
    }

    /**
     *
     * @return
     * The carbohydratesValue
     */
    @JsonProperty("carbohydrates_value")
    public String getCarbohydratesValue() {
        return carbohydratesValue;
    }

    /**
     *
     * @param carbohydratesValue
     * The carbohydrates_value
     */
    @JsonProperty("carbohydrates_value")
    public void setCarbohydratesValue(String carbohydratesValue) {
        this.carbohydratesValue = carbohydratesValue;
    }

    /**
     *
     * @return
     * The nutritionScoreFr
     */
    @JsonProperty("nutrition-score-fr")
    public String getNutritionScoreFr() {
        return nutritionScoreFr;
    }

    /**
     *
     * @param nutritionScoreFr
     * The nutrition-score-fr
     */
    @JsonProperty("nutrition-score-fr")
    public void setNutritionScoreFr(String nutritionScoreFr) {
        this.nutritionScoreFr = nutritionScoreFr;
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

    /**
     *
     * @return
     * The taurineValue
     */
    @JsonProperty("taurine_value")
    public String getTaurineValue() {
        return taurineValue;
    }

    /**
     *
     * @param taurineValue
     * The taurine_value
     */
    @JsonProperty("taurine_value")
    public void setTaurineValue(String taurineValue) {
        this.taurineValue = taurineValue;
    }

    /**
     *
     * @return
     * The fiber100g
     */
    @JsonProperty("fiber_100g")
    public String getFiber100g() {
        return fiber100g;
    }

    /**
     *
     * @param fiber100g
     * The fiber_100g
     */
    @JsonProperty("fiber_100g")
    public void setFiber100g(String fiber100g) {
        this.fiber100g = fiber100g;
    }

    /**
     *
     * @return
     * The alphaLinolenicAcidUnit
     */
    @JsonProperty("alpha-linolenic-acid_unit")
    public String getAlphaLinolenicAcidUnit() {
        return alphaLinolenicAcidUnit;
    }

    /**
     *
     * @param alphaLinolenicAcidUnit
     * The alpha-linolenic-acid_unit
     */
    @JsonProperty("alpha-linolenic-acid_unit")
    public void setAlphaLinolenicAcidUnit(String alphaLinolenicAcidUnit) {
        this.alphaLinolenicAcidUnit = alphaLinolenicAcidUnit;
    }

    /**
     *
     * @return
     * The taurineLabel
     */
    @JsonProperty("taurine_label")
    public String getTaurineLabel() {
        return taurineLabel;
    }

    /**
     *
     * @param taurineLabel
     * The taurine_label
     */
    @JsonProperty("taurine_label")
    public void setTaurineLabel(String taurineLabel) {
        this.taurineLabel = taurineLabel;
    }

    /**
     *
     * @return
     * The chloreValue
     */
    @JsonProperty("chlore_value")
    public String getChloreValue() {
        return chloreValue;
    }

    /**
     *
     * @param chloreValue
     * The chlore_value
     */
    @JsonProperty("chlore_value")
    public void setChloreValue(String chloreValue) {
        this.chloreValue = chloreValue;
    }

    /**
     *
     * @return
     * The fiber
     */
    @JsonProperty("fiber")
    public String getFiber() {
        return fiber;
    }

    /**
     *
     * @param fiber
     * The fiber
     */
    @JsonProperty("fiber")
    public void setFiber(String fiber) {
        this.fiber = fiber;
    }

    /**
     *
     * @return
     * The sugarsServing
     */
    @JsonProperty("sugars_serving")
    public String getSugarsServing() {
        return sugarsServing;
    }

    /**
     *
     * @param sugarsServing
     * The sugars_serving
     */
    @JsonProperty("sugars_serving")
    public void setSugarsServing(String sugarsServing) {
        this.sugarsServing = sugarsServing;
    }

    /**
     *
     * @return
     * The magnesiumLabel
     */
    @JsonProperty("magnesium_label")
    public String getMagnesiumLabel() {
        return magnesiumLabel;
    }

    /**
     *
     * @param magnesiumLabel
     * The magnesium_label
     */
    @JsonProperty("magnesium_label")
    public void setMagnesiumLabel(String magnesiumLabel) {
        this.magnesiumLabel = magnesiumLabel;
    }

    /**
     *
     * @return
     * The magnesiumServing
     */
    @JsonProperty("magnesium_serving")
    public String getMagnesiumServing() {
        return magnesiumServing;
    }

    /**
     *
     * @param magnesiumServing
     * The magnesium_serving
     */
    @JsonProperty("magnesium_serving")
    public void setMagnesiumServing(String magnesiumServing) {
        this.magnesiumServing = magnesiumServing;
    }

    /**
     *
     * @return
     * The alphaLinolenicAcid
     */
    @JsonProperty("alpha-linolenic-acid")
    public String getAlphaLinolenicAcid() {
        return alphaLinolenicAcid;
    }

    /**
     *
     * @param alphaLinolenicAcid
     * The alpha-linolenic-acid
     */
    @JsonProperty("alpha-linolenic-acid")
    public void setAlphaLinolenicAcid(String alphaLinolenicAcid) {
        this.alphaLinolenicAcid = alphaLinolenicAcid;
    }

    /**
     *
     * @return
     * The lactoseUnit
     */
    @JsonProperty("lactose_unit")
    public String getLactoseUnit() {
        return lactoseUnit;
    }

    /**
     *
     * @param lactoseUnit
     * The lactose_unit
     */
    @JsonProperty("lactose_unit")
    public void setLactoseUnit(String lactoseUnit) {
        this.lactoseUnit = lactoseUnit;
    }

    /**
     *
     * @return
     * The saturatedFatServing
     */
    @JsonProperty("saturated-fat_serving")
    public String getSaturatedFatServing() {
        return saturatedFatServing;
    }

    /**
     *
     * @param saturatedFatServing
     * The saturated-fat_serving
     */
    @JsonProperty("saturated-fat_serving")
    public void setSaturatedFatServing(String saturatedFatServing) {
        this.saturatedFatServing = saturatedFatServing;
    }

    /**
     *
     * @return
     * The docosahexaenoicAcidUnit
     */
    @JsonProperty("docosahexaenoic-acid_unit")
    public String getDocosahexaenoicAcidUnit() {
        return docosahexaenoicAcidUnit;
    }

    /**
     *
     * @param docosahexaenoicAcidUnit
     * The docosahexaenoic-acid_unit
     */
    @JsonProperty("docosahexaenoic-acid_unit")
    public void setDocosahexaenoicAcidUnit(String docosahexaenoicAcidUnit) {
        this.docosahexaenoicAcidUnit = docosahexaenoicAcidUnit;
    }

    /**
     *
     * @return
     * The lactose100g
     */
    @JsonProperty("lactose_100g")
    public String getLactose100g() {
        return lactose100g;
    }

    /**
     *
     * @param lactose100g
     * The lactose_100g
     */
    @JsonProperty("lactose_100g")
    public void setLactose100g(String lactose100g) {
        this.lactose100g = lactose100g;
    }

    /**
     *
     * @return
     * The sugars100g
     */
    @JsonProperty("sugars_100g")
    public String getSugars100g() {
        return sugars100g;
    }

    /**
     *
     * @param sugars100g
     * The sugars_100g
     */
    @JsonProperty("sugars_100g")
    public void setSugars100g(String sugars100g) {
        this.sugars100g = sugars100g;
    }

    /**
     *
     * @return
     * The saturatedFat100g
     */
    @JsonProperty("saturated-fat_100g")
    public String getSaturatedFat100g() {
        return saturatedFat100g;
    }

    /**
     *
     * @param saturatedFat100g
     * The saturated-fat_100g
     */
    @JsonProperty("saturated-fat_100g")
    public void setSaturatedFat100g(String saturatedFat100g) {
        this.saturatedFat100g = saturatedFat100g;
    }

    /**
     *
     * @return
     * The alphaLinolenicAcidValue
     */
    @JsonProperty("alpha-linolenic-acid_value")
    public String getAlphaLinolenicAcidValue() {
        return alphaLinolenicAcidValue;
    }

    /**
     *
     * @param alphaLinolenicAcidValue
     * The alpha-linolenic-acid_value
     */
    @JsonProperty("alpha-linolenic-acid_value")
    public void setAlphaLinolenicAcidValue(String alphaLinolenicAcidValue) {
        this.alphaLinolenicAcidValue = alphaLinolenicAcidValue;
    }

    /**
     *
     * @return
     * The serumProteinsValue
     */
    @JsonProperty("serum-proteins_value")
    public String getSerumProteinsValue() {
        return serumProteinsValue;
    }

    /**
     *
     * @param serumProteinsValue
     * The serum-proteins_value
     */
    @JsonProperty("serum-proteins_value")
    public void setSerumProteinsValue(String serumProteinsValue) {
        this.serumProteinsValue = serumProteinsValue;
    }

    /**
     *
     * @return
     * The potassiumServing
     */
    @JsonProperty("potassium_serving")
    public String getPotassiumServing() {
        return potassiumServing;
    }

    /**
     *
     * @param potassiumServing
     * The potassium_serving
     */
    @JsonProperty("potassium_serving")
    public void setPotassiumServing(String potassiumServing) {
        this.potassiumServing = potassiumServing;
    }

    /**
     *
     * @return
     * The maltodextrins
     */
    @JsonProperty("maltodextrins")
    public String getMaltodextrins() {
        return maltodextrins;
    }

    /**
     *
     * @param maltodextrins
     * The maltodextrins
     */
    @JsonProperty("maltodextrins")
    public void setMaltodextrins(String maltodextrins) {
        this.maltodextrins = maltodextrins;
    }

    /**
     *
     * @return
     * The taurine
     */
    @JsonProperty("taurine")
    public String getTaurine() {
        return taurine;
    }

    /**
     *
     * @param taurine
     * The taurine
     */
    @JsonProperty("taurine")
    public void setTaurine(String taurine) {
        this.taurine = taurine;
    }

    /**
     *
     * @return
     * The salt100g
     */
    @JsonProperty("salt_100g")
    public String getSalt100g() {
        return salt100g;
    }

    /**
     *
     * @param salt100g
     * The salt_100g
     */
    @JsonProperty("salt_100g")
    public void setSalt100g(String salt100g) {
        this.salt100g = salt100g;
    }

    /**
     *
     * @return
     * The potassiumLabel
     */
    @JsonProperty("potassium_label")
    public String getPotassiumLabel() {
        return potassiumLabel;
    }

    /**
     *
     * @param potassiumLabel
     * The potassium_label
     */
    @JsonProperty("potassium_label")
    public void setPotassiumLabel(String potassiumLabel) {
        this.potassiumLabel = potassiumLabel;
    }

    /**
     *
     * @return
     * The linoleicAcidServing
     */
    @JsonProperty("linoleic-acid_serving")
    public String getLinoleicAcidServing() {
        return linoleicAcidServing;
    }

    /**
     *
     * @param linoleicAcidServing
     * The linoleic-acid_serving
     */
    @JsonProperty("linoleic-acid_serving")
    public void setLinoleicAcidServing(String linoleicAcidServing) {
        this.linoleicAcidServing = linoleicAcidServing;
    }

    /**
     *
     * @return
     * The carbohydratesUnit
     */
    @JsonProperty("carbohydrates_unit")
    public String getCarbohydratesUnit() {
        return carbohydratesUnit;
    }

    /**
     *
     * @param carbohydratesUnit
     * The carbohydrates_unit
     */
    @JsonProperty("carbohydrates_unit")
    public void setCarbohydratesUnit(String carbohydratesUnit) {
        this.carbohydratesUnit = carbohydratesUnit;
    }

    /**
     *
     * @return
     * The calciumUnit
     */
    @JsonProperty("calcium_unit")
    public String getCalciumUnit() {
        return calciumUnit;
    }

    /**
     *
     * @param calciumUnit
     * The calcium_unit
     */
    @JsonProperty("calcium_unit")
    public void setCalciumUnit(String calciumUnit) {
        this.calciumUnit = calciumUnit;
    }

    /**
     *
     * @return
     * The calciumLabel
     */
    @JsonProperty("calcium_label")
    public String getCalciumLabel() {
        return calciumLabel;
    }

    /**
     *
     * @param calciumLabel
     * The calcium_label
     */
    @JsonProperty("calcium_label")
    public void setCalciumLabel(String calciumLabel) {
        this.calciumLabel = calciumLabel;
    }

    /**
     *
     * @return
     * The saturatedFatValue
     */
    @JsonProperty("saturated-fat_value")
    public String getSaturatedFatValue() {
        return saturatedFatValue;
    }

    /**
     *
     * @param saturatedFatValue
     * The saturated-fat_value
     */
    @JsonProperty("saturated-fat_value")
    public void setSaturatedFatValue(String saturatedFatValue) {
        this.saturatedFatValue = saturatedFatValue;
    }

    /**
     *
     * @return
     * The docosahexaenoicAcidLabel
     */
    @JsonProperty("docosahexaenoic-acid_label")
    public String getDocosahexaenoicAcidLabel() {
        return docosahexaenoicAcidLabel;
    }

    /**
     *
     * @param docosahexaenoicAcidLabel
     * The docosahexaenoic-acid_label
     */
    @JsonProperty("docosahexaenoic-acid_label")
    public void setDocosahexaenoicAcidLabel(String docosahexaenoicAcidLabel) {
        this.docosahexaenoicAcidLabel = docosahexaenoicAcidLabel;
    }

    /**
     *
     * @return
     * The chlore100g
     */
    @JsonProperty("chlore_100g")
    public String getChlore100g() {
        return chlore100g;
    }

    /**
     *
     * @param chlore100g
     * The chlore_100g
     */
    @JsonProperty("chlore_100g")
    public void setChlore100g(String chlore100g) {
        this.chlore100g = chlore100g;
    }

    /**
     *
     * @return
     * The calcium100g
     */
    @JsonProperty("calcium_100g")
    public String getCalcium100g() {
        return calcium100g;
    }

    /**
     *
     * @param calcium100g
     * The calcium_100g
     */
    @JsonProperty("calcium_100g")
    public void setCalcium100g(String calcium100g) {
        this.calcium100g = calcium100g;
    }

    /**
     *
     * @return
     * The nucleotidesLabel
     */
    @JsonProperty("nucleotides_label")
    public String getNucleotidesLabel() {
        return nucleotidesLabel;
    }

    /**
     *
     * @param nucleotidesLabel
     * The nucleotides_label
     */
    @JsonProperty("nucleotides_label")
    public void setNucleotidesLabel(String nucleotidesLabel) {
        this.nucleotidesLabel = nucleotidesLabel;
    }

    /**
     *
     * @return
     * The arachidonicAcidLabel
     */
    @JsonProperty("arachidonic-acid_label")
    public String getArachidonicAcidLabel() {
        return arachidonicAcidLabel;
    }

    /**
     *
     * @param arachidonicAcidLabel
     * The arachidonic-acid_label
     */
    @JsonProperty("arachidonic-acid_label")
    public void setArachidonicAcidLabel(String arachidonicAcidLabel) {
        this.arachidonicAcidLabel = arachidonicAcidLabel;
    }

    /**
     *
     * @return
     * The nucleotides100g
     */
    @JsonProperty("nucleotides_100g")
    public String getNucleotides100g() {
        return nucleotides100g;
    }

    /**
     *
     * @param nucleotides100g
     * The nucleotides_100g
     */
    @JsonProperty("nucleotides_100g")
    public void setNucleotides100g(String nucleotides100g) {
        this.nucleotides100g = nucleotides100g;
    }

    /**
     *
     * @return
     * The casein100g
     */
    @JsonProperty("casein_100g")
    public String getCasein100g() {
        return casein100g;
    }

    /**
     *
     * @param casein100g
     * The casein_100g
     */
    @JsonProperty("casein_100g")
    public void setCasein100g(String casein100g) {
        this.casein100g = casein100g;
    }

    /**
     *
     * @return
     * The caseinLabel
     */
    @JsonProperty("casein_label")
    public String getCaseinLabel() {
        return caseinLabel;
    }

    /**
     *
     * @param caseinLabel
     * The casein_label
     */
    @JsonProperty("casein_label")
    public void setCaseinLabel(String caseinLabel) {
        this.caseinLabel = caseinLabel;
    }

    /**
     *
     * @return
     * The arachidonicAcidServing
     */
    @JsonProperty("arachidonic-acid_serving")
    public String getArachidonicAcidServing() {
        return arachidonicAcidServing;
    }

    /**
     *
     * @param arachidonicAcidServing
     * The arachidonic-acid_serving
     */
    @JsonProperty("arachidonic-acid_serving")
    public void setArachidonicAcidServing(String arachidonicAcidServing) {
        this.arachidonicAcidServing = arachidonicAcidServing;
    }

    /**
     *
     * @return
     * The fiberValue
     */
    @JsonProperty("fiber_value")
    public String getFiberValue() {
        return fiberValue;
    }

    /**
     *
     * @param fiberValue
     * The fiber_value
     */
    @JsonProperty("fiber_value")
    public void setFiberValue(String fiberValue) {
        this.fiberValue = fiberValue;
    }

    /**
     *
     * @return
     * The chloreUnit
     */
    @JsonProperty("chlore_unit")
    public String getChloreUnit() {
        return chloreUnit;
    }

    /**
     *
     * @param chloreUnit
     * The chlore_unit
     */
    @JsonProperty("chlore_unit")
    public void setChloreUnit(String chloreUnit) {
        this.chloreUnit = chloreUnit;
    }

    /**
     *
     * @return
     * The maltodextrinsUnit
     */
    @JsonProperty("maltodextrins_unit")
    public String getMaltodextrinsUnit() {
        return maltodextrinsUnit;
    }

    /**
     *
     * @param maltodextrinsUnit
     * The maltodextrins_unit
     */
    @JsonProperty("maltodextrins_unit")
    public void setMaltodextrinsUnit(String maltodextrinsUnit) {
        this.maltodextrinsUnit = maltodextrinsUnit;
    }

    /**
     *
     * @return
     * The serumProteinsServing
     */
    @JsonProperty("serum-proteins_serving")
    public String getSerumProteinsServing() {
        return serumProteinsServing;
    }

    /**
     *
     * @param serumProteinsServing
     * The serum-proteins_serving
     */
    @JsonProperty("serum-proteins_serving")
    public void setSerumProteinsServing(String serumProteinsServing) {
        this.serumProteinsServing = serumProteinsServing;
    }

    /**
     *
     * @return
     * The proteinsServing
     */
    @JsonProperty("proteins_serving")
    public String getProteinsServing() {
        return proteinsServing;
    }

    /**
     *
     * @param proteinsServing
     * The proteins_serving
     */
    @JsonProperty("proteins_serving")
    public void setProteinsServing(String proteinsServing) {
        this.proteinsServing = proteinsServing;
    }

    /**
     *
     * @return
     * The linoleicAcid
     */
    @JsonProperty("linoleic-acid")
    public String getLinoleicAcid() {
        return linoleicAcid;
    }

    /**
     *
     * @param linoleicAcid
     * The linoleic-acid
     */
    @JsonProperty("linoleic-acid")
    public void setLinoleicAcid(String linoleicAcid) {
        this.linoleicAcid = linoleicAcid;
    }

    /**
     *
     * @return
     * The alphaLinolenicAcidLabel
     */
    @JsonProperty("alpha-linolenic-acid_label")
    public String getAlphaLinolenicAcidLabel() {
        return alphaLinolenicAcidLabel;
    }

    /**
     *
     * @param alphaLinolenicAcidLabel
     * The alpha-linolenic-acid_label
     */
    @JsonProperty("alpha-linolenic-acid_label")
    public void setAlphaLinolenicAcidLabel(String alphaLinolenicAcidLabel) {
        this.alphaLinolenicAcidLabel = alphaLinolenicAcidLabel;
    }

    /**
     *
     * @return
     * The arachidonicAcidValue
     */
    @JsonProperty("arachidonic-acid_value")
    public String getArachidonicAcidValue() {
        return arachidonicAcidValue;
    }

    /**
     *
     * @param arachidonicAcidValue
     * The arachidonic-acid_value
     */
    @JsonProperty("arachidonic-acid_value")
    public void setArachidonicAcidValue(String arachidonicAcidValue) {
        this.arachidonicAcidValue = arachidonicAcidValue;
    }

    /**
     *
     * @return
     * The taurine100g
     */
    @JsonProperty("taurine_100g")
    public String getTaurine100g() {
        return taurine100g;
    }

    /**
     *
     * @param taurine100g
     * The taurine_100g
     */
    @JsonProperty("taurine_100g")
    public void setTaurine100g(String taurine100g) {
        this.taurine100g = taurine100g;
    }

    /**
     *
     * @return
     * The maltodextrins100g
     */
    @JsonProperty("maltodextrins_100g")
    public String getMaltodextrins100g() {
        return maltodextrins100g;
    }

    /**
     *
     * @param maltodextrins100g
     * The maltodextrins_100g
     */
    @JsonProperty("maltodextrins_100g")
    public void setMaltodextrins100g(String maltodextrins100g) {
        this.maltodextrins100g = maltodextrins100g;
    }

    /**
     *
     * @return
     * The lactoseValue
     */
    @JsonProperty("lactose_value")
    public String getLactoseValue() {
        return lactoseValue;
    }

    /**
     *
     * @param lactoseValue
     * The lactose_value
     */
    @JsonProperty("lactose_value")
    public void setLactoseValue(String lactoseValue) {
        this.lactoseValue = lactoseValue;
    }

    /**
     *
     * @return
     * The taurineServing
     */
    @JsonProperty("taurine_serving")
    public String getTaurineServing() {
        return taurineServing;
    }

    /**
     *
     * @param taurineServing
     * The taurine_serving
     */
    @JsonProperty("taurine_serving")
    public void setTaurineServing(String taurineServing) {
        this.taurineServing = taurineServing;
    }

    /**
     *
     * @return
     * The nutritionScoreUk100g
     */
    @JsonProperty("nutrition-score-uk_100g")
    public String getNutritionScoreUk100g() {
        return nutritionScoreUk100g;
    }

    /**
     *
     * @param nutritionScoreUk100g
     * The nutrition-score-uk_100g
     */
    @JsonProperty("nutrition-score-uk_100g")
    public void setNutritionScoreUk100g(String nutritionScoreUk100g) {
        this.nutritionScoreUk100g = nutritionScoreUk100g;
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

    /**
     *
     * @return
     * The linoleicAcid100g
     */
    @JsonProperty("linoleic-acid_100g")
    public String getLinoleicAcid100g() {
        return linoleicAcid100g;
    }

    /**
     *
     * @param linoleicAcid100g
     * The linoleic-acid_100g
     */
    @JsonProperty("linoleic-acid_100g")
    public void setLinoleicAcid100g(String linoleicAcid100g) {
        this.linoleicAcid100g = linoleicAcid100g;
    }

    /**
     *
     * @return
     * The fatServing
     */
    @JsonProperty("fat_serving")
    public String getFatServing() {
        return fatServing;
    }

    /**
     *
     * @param fatServing
     * The fat_serving
     */
    @JsonProperty("fat_serving")
    public void setFatServing(String fatServing) {
        this.fatServing = fatServing;
    }

    /**
     *
     * @return
     * The fat100g
     */
    @JsonProperty("fat_100g")
    public String getFat100g() {
        return fat100g;
    }

    /**
     *
     * @param fat100g
     * The fat_100g
     */
    @JsonProperty("fat_100g")
    public void setFat100g(String fat100g) {
        this.fat100g = fat100g;
    }

    /**
     *
     * @return
     * The nutritionScoreFr100g
     */
    @JsonProperty("nutrition-score-fr_100g")
    public String getNutritionScoreFr100g() {
        return nutritionScoreFr100g;
    }

    /**
     *
     * @param nutritionScoreFr100g
     * The nutrition-score-fr_100g
     */
    @JsonProperty("nutrition-score-fr_100g")
    public void setNutritionScoreFr100g(String nutritionScoreFr100g) {
        this.nutritionScoreFr100g = nutritionScoreFr100g;
    }

    /**
     *
     * @return
     * The alphaLinolenicAcidServing
     */
    @JsonProperty("alpha-linolenic-acid_serving")
    public String getAlphaLinolenicAcidServing() {
        return alphaLinolenicAcidServing;
    }

    /**
     *
     * @param alphaLinolenicAcidServing
     * The alpha-linolenic-acid_serving
     */
    @JsonProperty("alpha-linolenic-acid_serving")
    public void setAlphaLinolenicAcidServing(String alphaLinolenicAcidServing) {
        this.alphaLinolenicAcidServing = alphaLinolenicAcidServing;
    }

    /**
     *
     * @return
     * The saltServing
     */
    @JsonProperty("salt_serving")
    public String getSaltServing() {
        return saltServing;
    }

    /**
     *
     * @param saltServing
     * The salt_serving
     */
    @JsonProperty("salt_serving")
    public void setSaltServing(String saltServing) {
        this.saltServing = saltServing;
    }

    /**
     *
     * @return
     * The carbohydratesServing
     */
    @JsonProperty("carbohydrates_serving")
    public String getCarbohydratesServing() {
        return carbohydratesServing;
    }

    /**
     *
     * @param carbohydratesServing
     * The carbohydrates_serving
     */
    @JsonProperty("carbohydrates_serving")
    public void setCarbohydratesServing(String carbohydratesServing) {
        this.carbohydratesServing = carbohydratesServing;
    }

    /**
     *
     * @return
     * The caseinServing
     */
    @JsonProperty("casein_serving")
    public String getCaseinServing() {
        return caseinServing;
    }

    /**
     *
     * @param caseinServing
     * The casein_serving
     */
    @JsonProperty("casein_serving")
    public void setCaseinServing(String caseinServing) {
        this.caseinServing = caseinServing;
    }

    /**
     *
     * @return
     * The docosahexaenoicAcid
     */
    @JsonProperty("docosahexaenoic-acid")
    public String getDocosahexaenoicAcid() {
        return docosahexaenoicAcid;
    }

    /**
     *
     * @param docosahexaenoicAcid
     * The docosahexaenoic-acid
     */
    @JsonProperty("docosahexaenoic-acid")
    public void setDocosahexaenoicAcid(String docosahexaenoicAcid) {
        this.docosahexaenoicAcid = docosahexaenoicAcid;
    }

    /**
     *
     * @return
     * The energyServing
     */
    @JsonProperty("energy_serving")
    public String getEnergyServing() {
        return energyServing;
    }

    /**
     *
     * @param energyServing
     * The energy_serving
     */
    @JsonProperty("energy_serving")
    public void setEnergyServing(String energyServing) {
        this.energyServing = energyServing;
    }

    /**
     *
     * @return
     * The magnesiumUnit
     */
    @JsonProperty("magnesium_unit")
    public String getMagnesiumUnit() {
        return magnesiumUnit;
    }

    /**
     *
     * @param magnesiumUnit
     * The magnesium_unit
     */
    @JsonProperty("magnesium_unit")
    public void setMagnesiumUnit(String magnesiumUnit) {
        this.magnesiumUnit = magnesiumUnit;
    }

    /**
     *
     * @return
     * The potassium
     */
    @JsonProperty("potassium")
    public String getPotassium() {
        return potassium;
    }

    /**
     *
     * @param potassium
     * The potassium
     */
    @JsonProperty("potassium")
    public void setPotassium(String potassium) {
        this.potassium = potassium;
    }

    /**
     *
     * @return
     * The proteinsValue
     */
    @JsonProperty("proteins_value")
    public String getProteinsValue() {
        return proteinsValue;
    }

    /**
     *
     * @param proteinsValue
     * The proteins_value
     */
    @JsonProperty("proteins_value")
    public void setProteinsValue(String proteinsValue) {
        this.proteinsValue = proteinsValue;
    }

    /**
     *
     * @return
     * The energy
     */
    @JsonProperty("energy")
    public String getEnergy() {
        return energy;
    }

    /**
     *
     * @param energy
     * The energy
     */
    @JsonProperty("energy")
    public void setEnergy(String energy) {
        this.energy = energy;
    }

    /**
     *
     * @return
     * The sodiumServing
     */
    @JsonProperty("sodium_serving")
    public String getSodiumServing() {
        return sodiumServing;
    }

    /**
     *
     * @param sodiumServing
     * The sodium_serving
     */
    @JsonProperty("sodium_serving")
    public void setSodiumServing(String sodiumServing) {
        this.sodiumServing = sodiumServing;
    }

    /**
     *
     * @return
     * The docosahexaenoicAcidServing
     */
    @JsonProperty("docosahexaenoic-acid_serving")
    public String getDocosahexaenoicAcidServing() {
        return docosahexaenoicAcidServing;
    }

    /**
     *
     * @param docosahexaenoicAcidServing
     * The docosahexaenoic-acid_serving
     */
    @JsonProperty("docosahexaenoic-acid_serving")
    public void setDocosahexaenoicAcidServing(String docosahexaenoicAcidServing) {
        this.docosahexaenoicAcidServing = docosahexaenoicAcidServing;
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