package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by jayanth on 22/2/18.
 */

@Entity(indexes = {
        @Index(value = "id", unique = true)
})
public class ToUploadProduct {

    @Id
    private Long id ;
    private String barcode;
    private String imageFilePath;
    private boolean uploaded = false;
    private String field;


    public ToUploadProduct(String barcode, String imageFilePath, String field) {
        this.barcode = barcode;
        this.imageFilePath = imageFilePath;
        this.field = field;
    }



    @Generated(hash = 499343655)
    public ToUploadProduct(Long id, String barcode, String imageFilePath,
            boolean uploaded, String field) {
        this.id = id;
        this.barcode = barcode;
        this.imageFilePath = imageFilePath;
        this.uploaded = uploaded;
        this.field = field;
    }



    @Generated(hash = 1993491654)
    public ToUploadProduct() {
    }



    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public boolean getUploaded() {
        return this.uploaded;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }


    public ProductImageField getProductField() {
        switch (field) {
            case "front":
                return ProductImageField.FRONT;
            case "ingredients":
                return ProductImageField.INGREDIENTS;
            case "nutrients":
                return ProductImageField.NUTRITION;
            default:
                return ProductImageField.OTHER;
        }
    }


}
