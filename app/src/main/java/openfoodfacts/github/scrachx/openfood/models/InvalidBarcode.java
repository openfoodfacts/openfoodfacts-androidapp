package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
<<<<<<< HEAD
import org.greenrobot.greendao.annotation.Generated;
=======
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.Serializable;
<<<<<<< HEAD

@Entity(indexes = {@Index(value = "barcode", unique = true)})
public class InvalidBarcode implements Serializable {
    private static final long serialVersionUID = 1L;
=======
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
    @Index(value = "barcode", unique = true)
})

public class InvalidBarcode implements Serializable {

    private static final long serialVersionUID = 1L;

>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
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
