package openfoodfacts.github.scrachx.openfood.models;

import com.orm.SugarRecord;

public class SendProduct extends SugarRecord {

    String barcode;
    String name;
    String weight;
    String weight_unit;
    String imgupload_front;
    String imgupload_ingredients;
    String imgupload_nutrition;
    String stores;

    public SendProduct() {
        this.barcode = "";
        this.name = "";
        this.weight = "";
        this.weight_unit = "";
        this.imgupload_front = "";
        this.imgupload_ingredients = "";
        this.imgupload_nutrition = "";
        this.stores = "";
    }

    public SendProduct(String barcode, String name, String weight, String weight_unit, String imgupload_front, String imgupload_ingredients, String imgupload_nutrition, String stores) {
        this.barcode = barcode;
        this.name = name;
        this.weight = weight;
        this.weight_unit = weight_unit;
        this.imgupload_front = imgupload_front;
        this.imgupload_ingredients = imgupload_ingredients;
        this.imgupload_nutrition = imgupload_nutrition;
        this.stores = stores;
    }

    public String getWeight_unit() {
        return weight_unit;
    }

    public void setWeight_unit(String weight_unit) {
        this.weight_unit = weight_unit;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImgupload_front() {
        return imgupload_front;
    }

    public void setImgupload_front(String imgupload_front) {
        this.imgupload_front = imgupload_front;
    }

    public String getStores() {
        return stores;
    }

    public void setStores(String stores) {
        this.stores = stores;
    }

    public String getImgupload_ingredients() {
        return imgupload_ingredients;
    }

    public void setImgupload_ingredients(String imgupload_ingredients) {
        this.imgupload_ingredients = imgupload_ingredients;
    }

    public String getImgupload_nutrition() {
        return imgupload_nutrition;
    }

    public void setImgupload_nutrition(String imgupload_nutrition) {
        this.imgupload_nutrition = imgupload_nutrition;
    }
}