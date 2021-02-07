package openfoodfacts.github.scrachx.openfood.models;

public class SaveItem {

    private String title;
    private int fieldsCompleted;
    private String url;
    private String barcode;
    private String weight;
    private String brand;

    public SaveItem() {
    }

    public SaveItem(String title, int fieldsCompleted, String url, String barcode, String weight, String brand) {
        this.title = title;
        this.fieldsCompleted = fieldsCompleted;
        this.url = url;
        this.barcode = barcode;
        this.brand = brand;
        this.weight = weight;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getFieldsCompleted() {
        return this.fieldsCompleted;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWeight() {
        return this.weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getBrand() {
        return this.brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
