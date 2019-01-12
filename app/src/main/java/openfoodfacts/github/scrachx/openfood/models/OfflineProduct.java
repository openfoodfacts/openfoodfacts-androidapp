package openfoodfacts.github.scrachx.openfood.models;

import com.opencsv.bean.CsvBindByName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

@Entity(indexes = {
        @Index(value = "barcode", unique = true)
})
public class OfflineProduct {

    @Id
    private Long id;
    @CsvBindByName(column = "product_name")
    private String title;
    @CsvBindByName(column = "brands")
    private String brands;
    @CsvBindByName(column = "code")
    private String barcode;
    @CsvBindByName(column = "quantity")
    private String quantity;
    @CsvBindByName(column = "nutrition_grade_fr")
    private String nutritionGrade;

    public OfflineProduct(String title, String brands, String barcode, String quantity, String nutritionGrade) {
        this.title = title;
        this.brands = brands;
        this.barcode = barcode;
        this.quantity = quantity;
        this.nutritionGrade = nutritionGrade;
    }

    @Generated(hash = 524339296)
    public OfflineProduct(Long id, String title, String brands, String barcode, String quantity,
                          String nutritionGrade) {
        this.id = id;
        this.title = title;
        this.brands = brands;
        this.barcode = barcode;
        this.quantity = quantity;
        this.nutritionGrade = nutritionGrade;
    }

    @Generated(hash = 1425505421)
    public OfflineProduct() {
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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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
