package openfoodfacts.github.scrachx.openfood.models;

import android.util.Base64;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity(indexes = {
        @Index(value = "barcode", unique = true)
})

public class OfflineSavedProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    private String barcode;
    private String productDetails;

    @Generated(hash = 689103893)
    public OfflineSavedProduct(Long id, String barcode, String productDetails) {
        this.id = id;
        this.barcode = barcode;
        this.productDetails = productDetails;
    }

    @Generated(hash = 403273060)
    public OfflineSavedProduct() {
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public HashMap<String, String> getProductDetailsMap() {
        if (this.getProductDetails() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(this.productDetails, Base64.DEFAULT));
            try {
                ObjectInputStream in = new ObjectInputStream(bis);
                try {
                    @SuppressWarnings("unchecked")
                    HashMap<String, String> hashMap = (HashMap<String, String>) in.readObject();
                    return hashMap;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setProductDetailsMap(Map<String, String> detailsMap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(detailsMap);
            out.flush();
            this.productDetails = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProductDetails() {
        return this.productDetails;
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}