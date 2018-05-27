package org.openfoodfacts.scanner.models;

import android.graphics.Bitmap;

public class SaveItem {

    private String title;
    private int icon;
    private String url;
    private String barcode;
    private String weight;
    private String brand;

    public SaveItem(){}

    public SaveItem(String title, int icon, String url, String barcode , String weight ,String brand) {
        this.title = title;
        this.icon = icon;
        this.url = url;
        this.barcode = barcode;
        this.brand = brand;
        this.weight =weight;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setWeight(String weight){
        this.weight = weight;
    }

    public String getWeight(){
        return this.weight;
    }

    public void setBrand(String brand){
        this.brand = brand;
    }

    public String getBrand(){
        return this.brand;
    }
}
