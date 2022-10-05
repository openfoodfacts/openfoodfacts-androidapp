package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.Serializable;

import openfoodfacts.github.scrachx.openfood.models.entities.TaxonomyEntity;

@Entity(indexes = {@Index(value = "barcode", unique = true)})
public class InvalidBarcode implements Serializable, TaxonomyEntity {
    private static final long serialVersionUID = 1L;
    @Id
    private String barcode;

    @Generated(hash = 1699276588)
    public InvalidBarcode(String barcode) {
        this.barcode = barcode;
    }

    @Generated(hash = 1875151970)
    public InvalidBarcode() {
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
