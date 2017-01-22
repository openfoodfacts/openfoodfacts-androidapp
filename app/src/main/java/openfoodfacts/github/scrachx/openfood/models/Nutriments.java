package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

/**
 * JSON representation of the product nutriments entry
 * @see <a href="http://en.wiki.openfoodfacts.org/API#JSON_interface">JSON Structure</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Nutriments implements Serializable {

    private static final String DEFAULT_UNIT = "g";

    @JsonProperty("serum-proteins_100g")
    private String serumProteins100g;
    private String casein;
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
    private String sodium;
    @JsonProperty("carbohydrates_100g")
    private String carbohydrates100g;
    @JsonProperty("taurine_unit")
    private String taurineUnit;
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
    private String nucleotides;
    @JsonProperty("sodium_value")
    private String sodiumValue;
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
    private String chlore;
    @JsonProperty("carbohydrates_value")
    private String carbohydratesValue;
    @JsonProperty("nutrition-score-fr")
    private String nutritionScoreFr;
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
    private String maltodextrins;
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
    private String potassium;
    @JsonProperty("proteins_value")
    private String proteinsValue;
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
    private Map<String, Object> additionalProperties = new HashMap<>();

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
     * @return The serumProteins100g
     */
    public String getSerumProteins100g() {
        return serumProteins100g;
    }

    /**
     * @param serumProteins100g The serum-proteins_100g
     */

    public void setSerumProteins100g(String serumProteins100g) {
        this.serumProteins100g = serumProteins100g;
    }

    /**
     * @return The casein
     */

    public String getCasein() {
        return casein;
    }

    /**
     * @param casein The casein
     */

    public void setCasein(String casein) {
        this.casein = casein;
    }

    /**
     * @return The magnesium
     */

    public String getMagnesium() {
        return magnesium;
    }

    /**
     * @param magnesium The magnesium
     */

    public void setMagnesium(String magnesium) {
        this.magnesium = magnesium;
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

    /**
     * @return The magnesiumValue
     */

    public String getMagnesiumValue() {
        return magnesiumValue;
    }

    /**
     * @param magnesiumValue The magnesium_value
     */

    public void setMagnesiumValue(String magnesiumValue) {
        this.magnesiumValue = magnesiumValue;
    }

    /**
     * @return The maltodextrinsValue
     */

    public String getMaltodextrinsValue() {
        return maltodextrinsValue;
    }

    /**
     * @param maltodextrinsValue The maltodextrins_value
     */

    public void setMaltodextrinsValue(String maltodextrinsValue) {
        this.maltodextrinsValue = maltodextrinsValue;
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

    /**
     * @return The maltodextrinsLabel
     */

    public String getMaltodextrinsLabel() {
        return maltodextrinsLabel;
    }

    /**
     * @return The salt_unit
     */

    public String getSaltUnit() {
        return isEmpty(saltUnit) ? DEFAULT_UNIT : saltUnit;
    }

    /**
     * @param saltUnit The salt_unit
     */

    public void setSaltUnit(String saltUnit) {
        this.saltUnit = saltUnit;
    }

    /**
     * @param maltodextrinsLabel The maltodextrins_label
     */

    public void setMaltodextrinsLabel(String maltodextrinsLabel) {
        this.maltodextrinsLabel = maltodextrinsLabel;
    }

    /**
     * @return The saturatedFatUnit
     */

    public String getSaturatedFatUnit() {
        return isEmpty(saturatedFatUnit) ? DEFAULT_UNIT : saturatedFatUnit;
    }

    /**
     * @param saturatedFatUnit The saturated-fat_unit
     */

    public void setSaturatedFatUnit(String saturatedFatUnit) {
        this.saturatedFatUnit = saturatedFatUnit;
    }

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

    /**
     * @return The taurineUnit
     */

    public String getTaurineUnit() {
        return taurineUnit;
    }

    /**
     * @param taurineUnit The taurine_unit
     */

    public void setTaurineUnit(String taurineUnit) {
        this.taurineUnit = taurineUnit;
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

    /**
     * @return The linoleicAcidUnit
     */

    public String getLinoleicAcidUnit() {
        return linoleicAcidUnit;
    }

    /**
     * @param linoleicAcidUnit The linoleic-acid_unit
     */

    public void setLinoleicAcidUnit(String linoleicAcidUnit) {
        this.linoleicAcidUnit = linoleicAcidUnit;
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

    /**
     * @return The potassiumValue
     */

    public String getPotassiumValue() {
        return potassiumValue;
    }

    /**
     * @param potassiumValue The potassium_value
     */

    public void setPotassiumValue(String potassiumValue) {
        this.potassiumValue = potassiumValue;
    }

    /**
     * @return The calciumServing
     */

    public String getCalciumServing() {
        return calciumServing;
    }

    /**
     * @param calciumServing The calcium_serving
     */

    public void setCalciumServing(String calciumServing) {
        this.calciumServing = calciumServing;
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

    /**
     * @return The serumProteinsUnit
     */

    public String getSerumProteinsUnit() {
        return serumProteinsUnit;
    }

    /**
     * @param serumProteinsUnit The serum-proteins_unit
     */

    public void setSerumProteinsUnit(String serumProteinsUnit) {
        this.serumProteinsUnit = serumProteinsUnit;
    }

    /**
     * @return The maltodextrinsServing
     */

    public String getMaltodextrinsServing() {
        return maltodextrinsServing;
    }

    /**
     * @param maltodextrinsServing The maltodextrins_serving
     */

    public void setMaltodextrinsServing(String maltodextrinsServing) {
        this.maltodextrinsServing = maltodextrinsServing;
    }

    /**
     * @return The caseinUnit
     */

    public String getCaseinUnit() {
        return caseinUnit;
    }

    /**
     * @param caseinUnit The casein_unit
     */

    public void setCaseinUnit(String caseinUnit) {
        this.caseinUnit = caseinUnit;
    }

    /**
     * @return The nucleotidesUnit
     */

    public String getNucleotidesUnit() {
        return nucleotidesUnit;
    }

    /**
     * @param nucleotidesUnit The nucleotides_unit
     */

    public void setNucleotidesUnit(String nucleotidesUnit) {
        this.nucleotidesUnit = nucleotidesUnit;
    }

    /**
     * @return The calciumValue
     */

    public String getCalciumValue() {
        return calciumValue;
    }

    /**
     * @param calciumValue The calcium_value
     */

    public void setCalciumValue(String calciumValue) {
        this.calciumValue = calciumValue;
    }

    /**
     * @return The sugarsUnit
     */

    public String getSugarsUnit() {
        return isEmpty(sugarsUnit) ? DEFAULT_UNIT : sugarsUnit;
    }

    /**
     * @param sugarsUnit The sugars_unit
     */

    public void setSugarsUnit(String sugarsUnit) {
        this.sugarsUnit = sugarsUnit;
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

    /**
     * @return The lactose
     */

    public String getLactose() {
        return lactose;
    }

    /**
     * @param lactose The lactose
     */

    public void setLactose(String lactose) {
        this.lactose = lactose;
    }

    /**
     * @return The alphaLinolenicAcid100g
     */

    public String getAlphaLinolenicAcid100g() {
        return alphaLinolenicAcid100g;
    }

    /**
     * @param alphaLinolenicAcid100g The alpha-linolenic-acid_100g
     */

    public void setAlphaLinolenicAcid100g(String alphaLinolenicAcid100g) {
        this.alphaLinolenicAcid100g = alphaLinolenicAcid100g;
    }

    /**
     * @return The potassiumUnit
     */

    public String getPotassiumUnit() {
        return potassiumUnit;
    }

    /**
     * @param potassiumUnit The potassium_unit
     */

    public void setPotassiumUnit(String potassiumUnit) {
        this.potassiumUnit = potassiumUnit;
    }

    /**
     * @return The nucleotidesServing
     */

    public String getNucleotidesServing() {
        return nucleotidesServing;
    }

    /**
     * @param nucleotidesServing The nucleotides_serving
     */

    public void setNucleotidesServing(String nucleotidesServing) {
        this.nucleotidesServing = nucleotidesServing;
    }

    /**
     * @return The fatUnit
     */

    public String getFatUnit() {
        return isEmpty(fatUnit) ? DEFAULT_UNIT : fatUnit;
    }

    /**
     * @param fatUnit The fat_unit
     */

    public void setFatUnit(String fatUnit) {
        this.fatUnit = fatUnit;
    }

    /**
     * @return The linoleicAcidValue
     */

    public String getLinoleicAcidValue() {
        return linoleicAcidValue;
    }

    /**
     * @param linoleicAcidValue The linoleic-acid_value
     */

    public void setLinoleicAcidValue(String linoleicAcidValue) {
        this.linoleicAcidValue = linoleicAcidValue;
    }

    /**
     * @return The arachidonicAcid
     */

    public String getArachidonicAcid() {
        return arachidonicAcid;
    }

    /**
     * @param arachidonicAcid The arachidonic-acid
     */

    public void setArachidonicAcid(String arachidonicAcid) {
        this.arachidonicAcid = arachidonicAcid;
    }

    /**
     * @return The potassium100g
     */

    public String getPotassium100g() {
        return potassium100g;
    }

    /**
     * @param potassium100g The potassium_100g
     */

    public void setPotassium100g(String potassium100g) {
        this.potassium100g = potassium100g;
    }

    /**
     * @return The linoleicAcidLabel
     */

    public String getLinoleicAcidLabel() {
        return linoleicAcidLabel;
    }

    /**
     * @param linoleicAcidLabel The linoleic-acid_label
     */

    public void setLinoleicAcidLabel(String linoleicAcidLabel) {
        this.linoleicAcidLabel = linoleicAcidLabel;
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

    /**
     * @return The chloreLabel
     */

    public String getChloreLabel() {
        return chloreLabel;
    }

    /**
     * @param chloreLabel The chlore_label
     */

    public void setChloreLabel(String chloreLabel) {
        this.chloreLabel = chloreLabel;
    }

    /**
     * @return The lactoseLabel
     */

    public String getLactoseLabel() {
        return lactoseLabel;
    }

    /**
     * @param lactoseLabel The lactose_label
     */

    public void setLactoseLabel(String lactoseLabel) {
        this.lactoseLabel = lactoseLabel;
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

    /**
     * @return The lactoseServing
     */

    public String getLactoseServing() {
        return lactoseServing;
    }

    /**
     * @param lactoseServing The lactose_serving
     */

    public void setLactoseServing(String lactoseServing) {
        this.lactoseServing = lactoseServing;
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

    /**
     * @return The caseinValue
     */

    public String getCaseinValue() {
        return caseinValue;
    }

    /**
     * @param caseinValue The casein_value
     */

    public void setCaseinValue(String caseinValue) {
        this.caseinValue = caseinValue;
    }

    /**
     * @return The serumProteinsLabel
     */

    public String getSerumProteinsLabel() {
        return serumProteinsLabel;
    }

    /**
     * @param serumProteinsLabel The serum-proteins_label
     */

    public void setSerumProteinsLabel(String serumProteinsLabel) {
        this.serumProteinsLabel = serumProteinsLabel;
    }

    /**
     * @return The serumProteins
     */

    public String getSerumProteins() {
        return serumProteins;
    }

    /**
     * @param serumProteins The serum-proteins
     */

    public void setSerumProteins(String serumProteins) {
        this.serumProteins = serumProteins;
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

    /**
     * @return The docosahexaenoicAcidValue
     */

    public String getDocosahexaenoicAcidValue() {
        return docosahexaenoicAcidValue;
    }

    /**
     * @param docosahexaenoicAcidValue The docosahexaenoic-acid_value
     */

    public void setDocosahexaenoicAcidValue(String docosahexaenoicAcidValue) {
        this.docosahexaenoicAcidValue = docosahexaenoicAcidValue;
    }

    /**
     * @return The nucleotidesValue
     */

    public String getNucleotidesValue() {
        return nucleotidesValue;
    }

    /**
     * @param nucleotidesValue The nucleotides_value
     */

    public void setNucleotidesValue(String nucleotidesValue) {
        this.nucleotidesValue = nucleotidesValue;
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

    /**
     * @return The arachidonicAcidUnit
     */

    public String getArachidonicAcidUnit() {
        return arachidonicAcidUnit;
    }

    /**
     * @param arachidonicAcidUnit The arachidonic-acid_unit
     */

    public void setArachidonicAcidUnit(String arachidonicAcidUnit) {
        this.arachidonicAcidUnit = arachidonicAcidUnit;
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

    /**
     * @return The nucleotides
     */

    public String getNucleotides() {
        return nucleotides;
    }

    /**
     * @param nucleotides The nucleotides
     */

    public void setNucleotides(String nucleotides) {
        this.nucleotides = nucleotides;
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

    /**
     * @return The calcium
     */

    public String getCalcium() {
        return calcium;
    }

    /**
     * @param calcium The calcium
     */

    public void setCalcium(String calcium) {
        this.calcium = calcium;
    }

    /**
     * @return The chloreServing
     */

    public String getChloreServing() {
        return chloreServing;
    }

    /**
     * @param chloreServing The chlore_serving
     */

    public void setChloreServing(String chloreServing) {
        this.chloreServing = chloreServing;
    }

    /**
     * @return The arachidonicAcid100g
     */

    public String getArachidonicAcid100g() {
        return arachidonicAcid100g;
    }

    /**
     * @param arachidonicAcid100g The arachidonic-acid_100g
     */

    public void setArachidonicAcid100g(String arachidonicAcid100g) {
        this.arachidonicAcid100g = arachidonicAcid100g;
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

    /**
     * @return The magnesium100g
     */

    public String getMagnesium100g() {
        return magnesium100g;
    }

    /**
     * @param magnesium100g The magnesium_100g
     */

    public void setMagnesium100g(String magnesium100g) {
        this.magnesium100g = magnesium100g;
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

    /**
     * @return The docosahexaenoicAcid100g
     */

    public String getDocosahexaenoicAcid100g() {
        return docosahexaenoicAcid100g;
    }

    /**
     * @param docosahexaenoicAcid100g The docosahexaenoic-acid_100g
     */

    public void setDocosahexaenoicAcid100g(String docosahexaenoicAcid100g) {
        this.docosahexaenoicAcid100g = docosahexaenoicAcid100g;
    }

    /**
     * @return The chlore
     */

    public String getChlore() {
        return chlore;
    }

    /**
     * @param chlore The chlore
     */

    public void setChlore(String chlore) {
        this.chlore = chlore;
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

    /**
     * @return The taurineValue
     */

    public String getTaurineValue() {
        return taurineValue;
    }

    /**
     * @param taurineValue The taurine_value
     */

    public void setTaurineValue(String taurineValue) {
        this.taurineValue = taurineValue;
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

    /**
     * @return The alphaLinolenicAcidUnit
     */

    public String getAlphaLinolenicAcidUnit() {
        return alphaLinolenicAcidUnit;
    }

    /**
     * @param alphaLinolenicAcidUnit The alpha-linolenic-acid_unit
     */

    public void setAlphaLinolenicAcidUnit(String alphaLinolenicAcidUnit) {
        this.alphaLinolenicAcidUnit = alphaLinolenicAcidUnit;
    }

    /**
     * @return The taurineLabel
     */

    public String getTaurineLabel() {
        return taurineLabel;
    }

    /**
     * @param taurineLabel The taurine_label
     */

    public void setTaurineLabel(String taurineLabel) {
        this.taurineLabel = taurineLabel;
    }

    /**
     * @return The chloreValue
     */

    public String getChloreValue() {
        return chloreValue;
    }

    /**
     * @param chloreValue The chlore_value
     */

    public void setChloreValue(String chloreValue) {
        this.chloreValue = chloreValue;
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

    /**
     * @return The magnesiumLabel
     */

    public String getMagnesiumLabel() {
        return magnesiumLabel;
    }

    /**
     * @param magnesiumLabel The magnesium_label
     */

    public void setMagnesiumLabel(String magnesiumLabel) {
        this.magnesiumLabel = magnesiumLabel;
    }

    /**
     * @return The magnesiumServing
     */

    public String getMagnesiumServing() {
        return magnesiumServing;
    }

    /**
     * @param magnesiumServing The magnesium_serving
     */

    public void setMagnesiumServing(String magnesiumServing) {
        this.magnesiumServing = magnesiumServing;
    }

    /**
     * @return The alphaLinolenicAcid
     */

    public String getAlphaLinolenicAcid() {
        return alphaLinolenicAcid;
    }

    /**
     * @param alphaLinolenicAcid The alpha-linolenic-acid
     */

    public void setAlphaLinolenicAcid(String alphaLinolenicAcid) {
        this.alphaLinolenicAcid = alphaLinolenicAcid;
    }

    /**
     * @return The lactoseUnit
     */

    public String getLactoseUnit() {
        return lactoseUnit;
    }

    /**
     * @param lactoseUnit The lactose_unit
     */

    public void setLactoseUnit(String lactoseUnit) {
        this.lactoseUnit = lactoseUnit;
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

    /**
     * @return The docosahexaenoicAcidUnit
     */

    public String getDocosahexaenoicAcidUnit() {
        return docosahexaenoicAcidUnit;
    }

    /**
     * @param docosahexaenoicAcidUnit The docosahexaenoic-acid_unit
     */

    public void setDocosahexaenoicAcidUnit(String docosahexaenoicAcidUnit) {
        this.docosahexaenoicAcidUnit = docosahexaenoicAcidUnit;
    }

    /**
     * @return The lactose100g
     */

    public String getLactose100g() {
        return lactose100g;
    }

    /**
     * @param lactose100g The lactose_100g
     */

    public void setLactose100g(String lactose100g) {
        this.lactose100g = lactose100g;
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

    /**
     * @return The alphaLinolenicAcidValue
     */

    public String getAlphaLinolenicAcidValue() {
        return alphaLinolenicAcidValue;
    }

    /**
     * @param alphaLinolenicAcidValue The alpha-linolenic-acid_value
     */

    public void setAlphaLinolenicAcidValue(String alphaLinolenicAcidValue) {
        this.alphaLinolenicAcidValue = alphaLinolenicAcidValue;
    }

    /**
     * @return The serumProteinsValue
     */

    public String getSerumProteinsValue() {
        return serumProteinsValue;
    }

    /**
     * @param serumProteinsValue The serum-proteins_value
     */

    public void setSerumProteinsValue(String serumProteinsValue) {
        this.serumProteinsValue = serumProteinsValue;
    }

    /**
     * @return The potassiumServing
     */

    public String getPotassiumServing() {
        return potassiumServing;
    }

    /**
     * @param potassiumServing The potassium_serving
     */

    public void setPotassiumServing(String potassiumServing) {
        this.potassiumServing = potassiumServing;
    }

    /**
     * @return The maltodextrins
     */

    public String getMaltodextrins() {
        return maltodextrins;
    }

    /**
     * @param maltodextrins The maltodextrins
     */

    public void setMaltodextrins(String maltodextrins) {
        this.maltodextrins = maltodextrins;
    }

    /**
     * @return The taurine
     */

    public String getTaurine() {
        return taurine;
    }

    /**
     * @param taurine The taurine
     */

    public void setTaurine(String taurine) {
        this.taurine = taurine;
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

    /**
     * @return The potassiumLabel
     */

    public String getPotassiumLabel() {
        return potassiumLabel;
    }

    /**
     * @param potassiumLabel The potassium_label
     */

    public void setPotassiumLabel(String potassiumLabel) {
        this.potassiumLabel = potassiumLabel;
    }

    /**
     * @return The linoleicAcidServing
     */

    public String getLinoleicAcidServing() {
        return linoleicAcidServing;
    }

    /**
     * @param linoleicAcidServing The linoleic-acid_serving
     */

    public void setLinoleicAcidServing(String linoleicAcidServing) {
        this.linoleicAcidServing = linoleicAcidServing;
    }

    /**
     * @return The carbohydratesUnit
     */
//
    public String getCarbohydratesUnit() {
        return carbohydratesUnit;
    }

    /**
     * @param carbohydratesUnit The carbohydrates_unit
     */
    public void setCarbohydratesUnit(String carbohydratesUnit) {
        this.carbohydratesUnit = carbohydratesUnit;
    }

    /**
     * @return The calciumUnit
     */

    public String getCalciumUnit() {
        return calciumUnit;
    }

    /**
     * @param calciumUnit The calcium_unit
     */

    public void setCalciumUnit(String calciumUnit) {
        this.calciumUnit = calciumUnit;
    }

    /**
     * @return The calciumLabel
     */

    public String getCalciumLabel() {
        return calciumLabel;
    }

    /**
     * @param calciumLabel The calcium_label
     */

    public void setCalciumLabel(String calciumLabel) {
        this.calciumLabel = calciumLabel;
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

    /**
     * @return The docosahexaenoicAcidLabel
     */

    public String getDocosahexaenoicAcidLabel() {
        return docosahexaenoicAcidLabel;
    }

    /**
     * @param docosahexaenoicAcidLabel The docosahexaenoic-acid_label
     */

    public void setDocosahexaenoicAcidLabel(String docosahexaenoicAcidLabel) {
        this.docosahexaenoicAcidLabel = docosahexaenoicAcidLabel;
    }

    /**
     * @return The chlore100g
     */

    public String getChlore100g() {
        return chlore100g;
    }

    /**
     * @param chlore100g The chlore_100g
     */

    public void setChlore100g(String chlore100g) {
        this.chlore100g = chlore100g;
    }

    /**
     * @return The calcium100g
     */

    public String getCalcium100g() {
        return calcium100g;
    }

    /**
     * @param calcium100g The calcium_100g
     */

    public void setCalcium100g(String calcium100g) {
        this.calcium100g = calcium100g;
    }

    /**
     * @return The nucleotidesLabel
     */

    public String getNucleotidesLabel() {
        return nucleotidesLabel;
    }

    /**
     * @param nucleotidesLabel The nucleotides_label
     */

    public void setNucleotidesLabel(String nucleotidesLabel) {
        this.nucleotidesLabel = nucleotidesLabel;
    }

    /**
     * @return The arachidonicAcidLabel
     */

    public String getArachidonicAcidLabel() {
        return arachidonicAcidLabel;
    }

    /**
     * @param arachidonicAcidLabel The arachidonic-acid_label
     */

    public void setArachidonicAcidLabel(String arachidonicAcidLabel) {
        this.arachidonicAcidLabel = arachidonicAcidLabel;
    }

    /**
     * @return The nucleotides100g
     */

    public String getNucleotides100g() {
        return nucleotides100g;
    }

    /**
     * @param nucleotides100g The nucleotides_100g
     */

    public void setNucleotides100g(String nucleotides100g) {
        this.nucleotides100g = nucleotides100g;
    }

    /**
     * @return The casein100g
     */

    public String getCasein100g() {
        return casein100g;
    }

    /**
     * @param casein100g The casein_100g
     */

    public void setCasein100g(String casein100g) {
        this.casein100g = casein100g;
    }

    /**
     * @return The caseinLabel
     */

    public String getCaseinLabel() {
        return caseinLabel;
    }

    /**
     * @param caseinLabel The casein_label
     */

    public void setCaseinLabel(String caseinLabel) {
        this.caseinLabel = caseinLabel;
    }

    /**
     * @return The arachidonicAcidServing
     */

    public String getArachidonicAcidServing() {
        return arachidonicAcidServing;
    }

    /**
     * @param arachidonicAcidServing The arachidonic-acid_serving
     */

    public void setArachidonicAcidServing(String arachidonicAcidServing) {
        this.arachidonicAcidServing = arachidonicAcidServing;
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

    /**
     * @return The chloreUnit
     */

    public String getChloreUnit() {
        return chloreUnit;
    }

    /**
     * @param chloreUnit The chlore_unit
     */

    public void setChloreUnit(String chloreUnit) {
        this.chloreUnit = chloreUnit;
    }

    /**
     * @return The maltodextrinsUnit
     */

    public String getMaltodextrinsUnit() {
        return maltodextrinsUnit;
    }

    /**
     * @param maltodextrinsUnit The maltodextrins_unit
     */

    public void setMaltodextrinsUnit(String maltodextrinsUnit) {
        this.maltodextrinsUnit = maltodextrinsUnit;
    }

    /**
     * @return The serumProteinsServing
     */

    public String getSerumProteinsServing() {
        return serumProteinsServing;
    }

    /**
     * @param serumProteinsServing The serum-proteins_serving
     */

    public void setSerumProteinsServing(String serumProteinsServing) {
        this.serumProteinsServing = serumProteinsServing;
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

    /**
     * @return The linoleicAcid
     */

    public String getLinoleicAcid() {
        return linoleicAcid;
    }

    /**
     * @param linoleicAcid The linoleic-acid
     */

    public void setLinoleicAcid(String linoleicAcid) {
        this.linoleicAcid = linoleicAcid;
    }

    /**
     * @return The alphaLinolenicAcidLabel
     */

    public String getAlphaLinolenicAcidLabel() {
        return alphaLinolenicAcidLabel;
    }

    /**
     * @param alphaLinolenicAcidLabel The alpha-linolenic-acid_label
     */

    public void setAlphaLinolenicAcidLabel(String alphaLinolenicAcidLabel) {
        this.alphaLinolenicAcidLabel = alphaLinolenicAcidLabel;
    }

    /**
     * @return The arachidonicAcidValue
     */

    public String getArachidonicAcidValue() {
        return arachidonicAcidValue;
    }

    /**
     * @param arachidonicAcidValue The arachidonic-acid_value
     */

    public void setArachidonicAcidValue(String arachidonicAcidValue) {
        this.arachidonicAcidValue = arachidonicAcidValue;
    }

    /**
     * @return The taurine100g
     */

    public String getTaurine100g() {
        return taurine100g;
    }

    /**
     * @param taurine100g The taurine_100g
     */

    public void setTaurine100g(String taurine100g) {
        this.taurine100g = taurine100g;
    }

    /**
     * @return The maltodextrins100g
     */

    public String getMaltodextrins100g() {
        return maltodextrins100g;
    }

    /**
     * @param maltodextrins100g The maltodextrins_100g
     */

    public void setMaltodextrins100g(String maltodextrins100g) {
        this.maltodextrins100g = maltodextrins100g;
    }

    /**
     * @return The lactoseValue
     */

    public String getLactoseValue() {
        return lactoseValue;
    }

    /**
     * @param lactoseValue The lactose_value
     */

    public void setLactoseValue(String lactoseValue) {
        this.lactoseValue = lactoseValue;
    }

    /**
     * @return The taurineServing
     */

    public String getTaurineServing() {
        return taurineServing;
    }

    /**
     * @param taurineServing The taurine_serving
     */

    public void setTaurineServing(String taurineServing) {
        this.taurineServing = taurineServing;
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

    /**
     * @return The linoleicAcid100g
     */

    public String getLinoleicAcid100g() {
        return linoleicAcid100g;
    }

    /**
     * @param linoleicAcid100g The linoleic-acid_100g
     */

    public void setLinoleicAcid100g(String linoleicAcid100g) {
        this.linoleicAcid100g = linoleicAcid100g;
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

    /**
     * @return The alphaLinolenicAcidServing
     */

    public String getAlphaLinolenicAcidServing() {
        return alphaLinolenicAcidServing;
    }

    /**
     * @param alphaLinolenicAcidServing The alpha-linolenic-acid_serving
     */

    public void setAlphaLinolenicAcidServing(String alphaLinolenicAcidServing) {
        this.alphaLinolenicAcidServing = alphaLinolenicAcidServing;
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

    /**
     * @return The caseinServing
     */

    public String getCaseinServing() {
        return caseinServing;
    }

    /**
     * @param caseinServing The casein_serving
     */

    public void setCaseinServing(String caseinServing) {
        this.caseinServing = caseinServing;
    }

    /**
     * @return The docosahexaenoicAcid
     */

    public String getDocosahexaenoicAcid() {
        return docosahexaenoicAcid;
    }

    /**
     * @param docosahexaenoicAcid The docosahexaenoic-acid
     */

    public void setDocosahexaenoicAcid(String docosahexaenoicAcid) {
        this.docosahexaenoicAcid = docosahexaenoicAcid;
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

    /**
     * @return The magnesiumUnit
     */

    public String getMagnesiumUnit() {
        return magnesiumUnit;
    }

    /**
     * @param magnesiumUnit The magnesium_unit
     */

    public void setMagnesiumUnit(String magnesiumUnit) {
        this.magnesiumUnit = magnesiumUnit;
    }

    /**
     * @return The potassium
     */

    public String getPotassium() {
        return potassium;
    }

    /**
     * @param potassium The potassium
     */

    public void setPotassium(String potassium) {
        this.potassium = potassium;
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

    /**
     * @return The docosahexaenoicAcidServing
     */

    public String getDocosahexaenoicAcidServing() {
        return docosahexaenoicAcidServing;
    }

    /**
     * @param docosahexaenoicAcidServing The docosahexaenoic-acid_serving
     */

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