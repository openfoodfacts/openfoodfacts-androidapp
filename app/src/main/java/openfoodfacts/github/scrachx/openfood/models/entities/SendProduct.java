package openfoodfacts.github.scrachx.openfood.models.entities;

import android.text.TextUtils;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.utils.ImageCompressor;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity(indexes = {@Index(value = "barcode", unique = true)})
public class SendProduct implements Serializable {
    private static final long serialVersionUID = 2L;
    @Id
    private Long id;
    @JsonProperty(ApiFields.Keys.BARCODE)
    private String barcode;
    private String lang;
    @JsonProperty(ApiFields.Keys.PRODUCT_NAME)
    private String name;
    private String brands;
    @JsonIgnore
    private String weight;
    @JsonIgnore
    @Property(nameInDb = "IMGUPLOAD_FRONT")
    private String imgUploadFront;
    @JsonIgnore
    @Property(nameInDb = "IMGUPLOAD_INGREDIENTS")
    private String imgUploadIngredients;
    @JsonIgnore
    @Property(nameInDb = "IMGUPLOAD_NUTRITION")
    private String imgUploadNutrition;
    @JsonIgnore
    @Property(nameInDb = "IMGUPLOAD_PACKAGING")
    private String imgUploadPackaging;
    @JsonProperty(ApiFields.Keys.USER_ID)
    @Transient
    private String userID;
    @JsonIgnore
    @Property(nameInDb = "weight_unit")
    private String weightUnit = "g";
    @Transient
    private String password;

    public SendProduct() {
    }

    @Generated(hash = 1316358111)
    public SendProduct(Long id, String barcode, String lang, String name, String brands, String weight, String imgUploadFront, String imgUploadIngredients,
                       String imgUploadNutrition, String imgUploadPackaging, String weightUnit) {
        this.id = id;
        this.barcode = barcode;
        this.lang = lang;
        this.name = name;
        this.brands = brands;
        this.weight = weight;
        this.imgUploadFront = imgUploadFront;
        this.imgUploadIngredients = imgUploadIngredients;
        this.imgUploadNutrition = imgUploadNutrition;
        this.imgUploadPackaging = imgUploadPackaging;
        this.weightUnit = weightUnit;
    }

    @Keep
    public SendProduct(@NonNull SendProduct sp) {
        this.barcode = sp.getBarcode();
        this.name = sp.getName();
        this.brands = sp.getBrands();
        this.weight = sp.getWeight();
        this.weightUnit = sp.getWeightUnit();
        this.imgUploadFront = sp.getImgUploadFront();
        this.imgUploadIngredients = sp.getImgUploadIngredients();
        this.imgUploadNutrition = sp.getImgUploadNutrition();
        this.imgUploadPackaging = sp.getImgUploadPackaging();
        this.lang = sp.getLang();
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getQuantity() {
        if (weight == null || weight.length() == 0) {
            return null;
        }

        return String.format("%s %s", this.weight, this.weightUnit);
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImgUploadFront() {
        return imgUploadFront;
    }

    public void setImgUploadFront(String imgUploadFront) {
        this.imgUploadFront = imgUploadFront;
    }

    public String getBrands() {
        return brands;
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }

    public String getImgUploadIngredients() {
        return imgUploadIngredients;
    }

    public void setImgUploadIngredients(String imgUploadIngredients) {
        this.imgUploadIngredients = imgUploadIngredients;
    }

    public String getImgUploadNutrition() {
        return imgUploadNutrition;
    }

    public void setImgUploadNutrition(String imgUploadNutrition) {
        this.imgUploadNutrition = imgUploadNutrition;
    }

    public String getImgUploadPackaging() {
        return imgUploadPackaging;
    }

    public void setImgUploadPackaging(String imgUploadPackaging) {
        this.imgUploadPackaging = imgUploadPackaging;
    }

    /**
     * Compress the image according to the {@link ProductImageField}.
     * Add a "_small" prefix in the image name after the compression
     */
    public void compress(@NonNull ProductImageField field) {
        switch (field) {
            case NUTRITION:
                this.imgUploadNutrition = ImageCompressor.compress(this.imgUploadNutrition);
                break;
            case INGREDIENTS:
                this.imgUploadIngredients = ImageCompressor.compress(this.imgUploadIngredients);
                break;
            case PACKAGING:
                this.imgUploadPackaging = ImageCompressor.compress(this.imgUploadPackaging);
                break;
            case FRONT:
                this.imgUploadFront = ImageCompressor.compress(this.imgUploadFront);
                break;
            default:
                //nothing to do
                break;
        }
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SendProduct sp = (SendProduct) o;

        return TextUtils.equals(this.barcode, sp.getBarcode())
            && TextUtils.equals(this.name, sp.getName())
            && TextUtils.equals(this.brands, sp.getBrands())
            && TextUtils.equals(this.weight, sp.getWeight())
            && TextUtils.equals(this.weightUnit, sp.getWeightUnit())
            && TextUtils.equals(this.imgUploadFront, sp.getImgUploadFront())
            && TextUtils.equals(this.imgUploadNutrition, sp.getImgUploadNutrition())
            && TextUtils.equals(this.imgUploadPackaging, sp.getImgUploadPackaging())
            && TextUtils.equals(this.imgUploadIngredients, sp.getImgUploadIngredients());
    }
}