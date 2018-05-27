package openfoodfacts.github.scrachx.openfood.models;

import android.graphics.Bitmap;

public class SaveItem {

    private String title;
    private int icon;
    private Bitmap url;
    private String barcode;

    public SaveItem(){}

    public SaveItem(String title, int icon, Bitmap url, String barcode) {
        this.title = title;
        this.icon = icon;
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

    public int getIcon(){
        return this.icon;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setIcon(int icon){
        this.icon = icon;
    }

    public Bitmap getUrl() {
        return url;
    }

    public void setUrl(Bitmap url) {
        this.url = url;
    }
}
