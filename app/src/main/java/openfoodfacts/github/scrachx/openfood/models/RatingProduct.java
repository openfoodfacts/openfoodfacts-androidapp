package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {
        @Index(value = "barcode", unique = true)
})

/**
 * The model that is used in order to create the database table or ratings.
 */
public class RatingProduct {
    @Id private Long id;
    private short stars;
    private String comment;
    private String barcode;
    private String productName;
    private String imageUrl;

    public RatingProduct(short stars, String comment, String barcode, String productName, String imageUrl) {
        this.stars = stars;
        this.comment = comment;
        this.barcode = barcode;
        this.productName = productName;
        this.imageUrl = imageUrl;
    }

    @Generated(hash = 2097299515)
    public RatingProduct(Long id, short stars, String comment, String barcode, String productName, String imageUrl) {
        this.id = id;
        this.stars = stars;
        this.comment = comment;
        this.barcode = barcode;
        this.productName = productName;
        this.imageUrl = imageUrl;
    }

    @Generated(hash = 230848273)
    public RatingProduct() {
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public short getStars() { return stars; }

    public void setStars(short stars) { this.stars = stars; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public String getBarcode() { return barcode; }

    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getProductName() { return productName; }

    public void setProductName(String productName) { this.productName = productName; }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
