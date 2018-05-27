package openfoodfacts.github.scrachx.openfood.models;

import android.graphics.Bitmap;

public class HistoryItem {

    private String title;
    private String brands;
    private Bitmap url;
    private String barcode;

    public HistoryItem(){}

    public HistoryItem(String title, String brands, Bitmap url, String barcode) {
        this.title = title;
        this.brands = brands;
        this.url = url;
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public Bitmap getUrl() {
        return url;
    }

    public void setUrl(Bitmap url) {
        this.url = url;
    }

    public String getBrands() {
        return brands;
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }
}
