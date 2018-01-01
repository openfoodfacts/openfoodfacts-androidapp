package openfoodfacts.github.scrachx.openfood.models;

import android.graphics.Bitmap;

/**
 * RatingItem class is being used when binding the items to the recycler view,
 * at Your Personal Ratings menu option.
 */
public class RatingItem {
    private short stars;
    private String comment;
    private String barcode;
    private String productName;
    private Bitmap imageUrl;

    /**
     * Constructor
     * @param stars a short number indicates the number of stars given to the product by the user to this particular rating
     * @param comment a string containing the user's comment on the product related to this particular rating
     * @param barcode a string, indicating the barcode of the product that this particular rating refers to
     * @param productName a string containing the literal name of the product that this particular rating refers to
     * @param imageUrl a bitmap version of the product's image that this particular rating refers to
     */
    public RatingItem(short stars, String comment, String barcode, String productName, Bitmap imageUrl) {
        this.stars = stars;
        this.comment = comment;
        this.barcode = barcode;
        this.productName = productName;
        this.imageUrl = imageUrl;
    }

    public short getStars() { return stars; }

    public void setStars(short stars) { this.stars = stars; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public String getBarcode() { return barcode; }

    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getProductName() { return productName; }

    public void setProductName(String productName) { this.productName = productName; }

    public Bitmap getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(Bitmap imageUrl) { this.imageUrl = imageUrl; }
}
