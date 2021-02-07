package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.util.Date;

@Entity(indexes = {
    @Index(value = "barcode", unique = true)
})
public class HistoryProduct {
    @Id
    private Long id;
    private String title;
    private String brands;
    private String url;
    private Date lastSeen;
    private String barcode;
    private String quantity;
    private String nutritionGrade;
    private String ecoscore;
    private String novaGroup;

    public HistoryProduct(String title,
                          String brands,
                          String url,
                          String barcode,
                          String quantity,
                          String nutritionGrade,
                          String ecoscore,
                          String novaGroup) {
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.barcode = barcode;
        this.quantity = quantity;
        this.nutritionGrade = nutritionGrade;
        this.ecoscore = ecoscore;
        this.novaGroup = novaGroup;
        this.lastSeen = new Date();
    }

    @Generated(hash = 1035374663)
    public HistoryProduct(Long id, String title, String brands, String url, Date lastSeen, String barcode, String quantity,
                          String nutritionGrade, String ecoscore, String novaGroup) {
        this.id = id;
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.lastSeen = lastSeen;
        this.barcode = barcode;
        this.quantity = quantity;
        this.nutritionGrade = nutritionGrade;
        this.ecoscore = ecoscore;
        this.novaGroup = novaGroup;
    }

    @Generated(hash = 1674709907)
    public HistoryProduct() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrands() {
        return this.brands;
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getLastSeen() {
        return this.lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getBarcode() {
        return this.barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getQuantity() {
        return this.quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getNutritionGrade() {
        return this.nutritionGrade;
    }

    public void setNutritionGrade(String nutritionGrade) {
        this.nutritionGrade = nutritionGrade;
    }

    public String getEcoscore() {
        return this.ecoscore;
    }

    public void setEcoscore(String ecoscore) {
        this.ecoscore = ecoscore;
    }

    public String getNovaGroup() {
        return this.novaGroup;
    }

    public void setNovaGroup(String novaGroup) {
        this.novaGroup = novaGroup;
    }
}
