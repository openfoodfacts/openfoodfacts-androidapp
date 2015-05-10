package openfoodfacts.github.scrachx.openfood.models;

import com.orm.SugarRecord;

import java.io.File;

/**
 * Created by scotscriven on 09/05/15.
 */
public class SendProduct extends SugarRecord<SendProduct> {
    String barcode;
    String name;
    String energy;
    String energy_unit;
    String weight;
    String imgupload_front;
    String stores;

    public SendProduct() {

    }

    public SendProduct(String barcode, String name, String energy, String energy_unit, String weight, String imgupload_front, String stores) {
        this.barcode = barcode;
        this.name = name;
        this.energy = energy;
        this.energy_unit = energy_unit;
        this.weight = weight;
        this.imgupload_front = imgupload_front;
        this.stores = stores;
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

    public String getEnergy() {
        return energy;
    }

    public void setEnergy(String energy) {
        this.energy = energy;
    }

    public String getEnergy_unit() {
        return energy_unit;
    }

    public void setEnergy_unit(String energy_unit) {
        this.energy_unit = energy_unit;
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
}