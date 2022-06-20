package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.util.Date;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistoryProduct that = (HistoryProduct) o;

        if (!Objects.equals(id, that.id)) {
            return false;
        }
        if (!Objects.equals(title, that.title)) {
            return false;
        }
        if (!Objects.equals(brands, that.brands)) {
            return false;
        }
        if (!Objects.equals(url, that.url)) {
            return false;
        }
        if (!Objects.equals(lastSeen, that.lastSeen)) {
            return false;
        }
        if (!Objects.equals(barcode, that.barcode)) {
            return false;
        }
        if (!Objects.equals(quantity, that.quantity)) {
            return false;
        }
        if (!Objects.equals(nutritionGrade, that.nutritionGrade)) {
            return false;
        }
        if (!Objects.equals(ecoscore, that.ecoscore)) {
            return false;
        }
        return Objects.equals(novaGroup, that.novaGroup);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (brands != null ? brands.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (lastSeen != null ? lastSeen.hashCode() : 0);
        result = 31 * result + (barcode != null ? barcode.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (nutritionGrade != null ? nutritionGrade.hashCode() : 0);
        result = 31 * result + (ecoscore != null ? ecoscore.hashCode() : 0);
        result = 31 * result + (novaGroup != null ? novaGroup.hashCode() : 0);
        return result;
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
