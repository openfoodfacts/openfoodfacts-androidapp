package openfoodfacts.github.scrachx.openfood.models;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;
import java.util.Date;

public class HistoryProduct extends SugarRecord {

    private String title;
    private String brands;
    private String url;
    private Date lastSeen;
    @Unique
    private String barcode;

    public HistoryProduct() {
        this.title = "";
        this.brands = "";
        this.url = "";
        this.barcode = "";
        this.lastSeen = new Date();
    }

    public HistoryProduct(String title, String brands, String url, String barcode) {
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.barcode = barcode;
        this.lastSeen = new Date();
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
