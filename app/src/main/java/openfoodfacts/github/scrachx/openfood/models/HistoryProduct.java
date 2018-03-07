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

    public HistoryProduct(String title, String brands, String url, String barcode, String quantity, String nutritionGrade) {
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.barcode = barcode;
        this.lastSeen = new Date();
        this.quantity = quantity;
        this.nutritionGrade = nutritionGrade;
    }

    @Generated(hash = 1473607560)
    public HistoryProduct(Long id, String title, String brands, String url, Date lastSeen, String barcode, String quantity,
            String nutritionGrade) {
        this.id = id;
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.lastSeen = lastSeen;
        this.barcode = barcode;
        this.quantity = quantity;
        this.nutritionGrade = nutritionGrade;
    }

    @Generated(hash = 1674709907)
    public HistoryProduct() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrands() {
        return brands;
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getNutritionGrade() {
        return nutritionGrade;
    }

    public void setNutritionGrade(String nutritionGrade) {
        this.nutritionGrade = nutritionGrade;
    }
}
