package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "barcode", unique = true)
})
public class HistoryProduct {

    private String title;
    private String brands;
    private String url;
    private Date lastSeen;
    @Id
    private String barcode;

    // Default constructor is necessary for SugarRecord
    public HistoryProduct() {}

    public HistoryProduct(String title, String brands, String url, String barcode) {
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.barcode = barcode;
        this.lastSeen = new Date();
    }

    @Generated(hash = 1846082873)
    public HistoryProduct(String title, String brands, String url, Date lastSeen,
            String barcode) {
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.lastSeen = lastSeen;
        this.barcode = barcode;
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
}
