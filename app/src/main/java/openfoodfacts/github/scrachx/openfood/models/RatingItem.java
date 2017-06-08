package openfoodfacts.github.scrachx.openfood.models;

import android.graphics.Bitmap;

public class RatingItem {
    private short stars;
    private String comment;
    private String barcode;
    private String productName;
    private Bitmap imageUrl;

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
