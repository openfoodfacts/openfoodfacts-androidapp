package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.util.Date;

@Entity(indexes = {
<<<<<<< HEAD
    @Index(value = "barcode", unique = true)
})
public class HistoryProduct {
=======
        @Index(value = "barcode", unique = true)
})
public class HistoryProduct {

>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    @Id
    private Long id;
    private String title;
    private String brands;
    private String url;
    private Date lastSeen;
    private String barcode;
    private String quantity;
    private String nutritionGrade;
<<<<<<< HEAD
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
=======

    public HistoryProduct(String title, String brands, String url, String barcode, String quantity, String nutritionGrade) {
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.barcode = barcode;
<<<<<<< HEAD
        this.quantity = quantity;
        this.nutritionGrade = nutritionGrade;
        this.ecoscore = ecoscore;
        this.novaGroup = novaGroup;
        this.lastSeen = new Date();
    }

    @Generated(hash = 1035374663)
    public HistoryProduct(Long id, String title, String brands, String url, Date lastSeen, String barcode, String quantity,
                          String nutritionGrade, String ecoscore, String novaGroup) {
=======
        this.lastSeen = new Date();
        this.quantity = quantity;
        this.nutritionGrade = nutritionGrade;
    }

    @Generated(hash = 1473607560)
    public HistoryProduct(Long id, String title, String brands, String url, Date lastSeen, String barcode, String quantity,
            String nutritionGrade) {
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
        this.id = id;
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.lastSeen = lastSeen;
        this.barcode = barcode;
        this.quantity = quantity;
        this.nutritionGrade = nutritionGrade;
<<<<<<< HEAD
        this.ecoscore = ecoscore;
        this.novaGroup = novaGroup;
=======
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    @Generated(hash = 1674709907)
    public HistoryProduct() {
    }

<<<<<<< HEAD
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
=======
    public String getTitle() {
        return title;
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrands() {
<<<<<<< HEAD
        return this.brands;
=======
        return brands;
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }

    public String getUrl() {
<<<<<<< HEAD
        return this.url;
=======
        return url;
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    public void setUrl(String url) {
        this.url = url;
    }

<<<<<<< HEAD
    public Date getLastSeen() {
        return this.lastSeen;
=======
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Date getLastSeen() {
        return lastSeen;
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

<<<<<<< HEAD
    public String getBarcode() {
        return this.barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getQuantity() {
        return this.quantity;
=======
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuantity() {
        return quantity;
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getNutritionGrade() {
<<<<<<< HEAD
        return this.nutritionGrade;
=======
        return nutritionGrade;
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    public void setNutritionGrade(String nutritionGrade) {
        this.nutritionGrade = nutritionGrade;
    }
<<<<<<< HEAD

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
=======
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
}
