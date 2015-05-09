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
}