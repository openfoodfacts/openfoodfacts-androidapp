package openfoodfacts.github.scrachx.openfood.models;

import java.io.File;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ProductImage {

    private final RequestBody code;

    private final RequestBody field;

    private final RequestBody imguploadFront;

    private final RequestBody imguploadIngredients;

    private final RequestBody imguploadNutrition;

    private final RequestBody imguploadOther;

    private String filePath;

    private String barcode;

    private ProductImageField imageField;

    public ProductImage(String code, ProductImageField field, File image) {
        this.code = RequestBody.create(MediaType.parse("text/plain"), code);
        this.field = RequestBody.create(MediaType.parse("text/plain"), field.toString() + '_' + Locale.getDefault().getLanguage());

        switch (field) {
            case FRONT:
                this.imguploadFront = RequestBody.create(MediaType.parse("image/*"), image);
                this.imguploadIngredients = null;
                this.imguploadNutrition = null;
                this.imguploadOther = null;
                break;
            case INGREDIENTS:
                this.imguploadIngredients = RequestBody.create(MediaType.parse("image/*"), image);
                this.imguploadFront = null;
                this.imguploadNutrition = null;
                this.imguploadOther = null;
                break;
            case NUTRITION:
                this.imguploadNutrition = RequestBody.create(MediaType.parse("image/*"), image);
                this.imguploadFront = null;
                this.imguploadIngredients = null;
                this.imguploadOther = null;
                break;
            case OTHER:
                this.imguploadOther = RequestBody.create(MediaType.parse("image/*"), image);
                this.imguploadNutrition = null;
                this.imguploadFront = null;
                this.imguploadIngredients = null;
                break;
            default:
                this.imguploadNutrition = null;
                this.imguploadFront = null;
                this.imguploadIngredients = null;
                this.imguploadOther = null;
                break;
        }

        barcode = code;
        imageField = field;
    }

    public RequestBody getCode() {
        return code;
    }

    public RequestBody getField() {
        return field;
    }

    public RequestBody getImguploadFront() {
        return imguploadFront;
    }

    public RequestBody getImguploadIngredients() {
        return imguploadIngredients;
    }

    public RequestBody getImguploadNutrition() {
        return imguploadNutrition;
    }

    public RequestBody getImguploadOther() {
        return imguploadOther;
    }

    public void setFilePath(String path) {
        filePath = path;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getFilePath() {
        return filePath;
    }

    public ProductImageField getImageField() {
        return imageField;
    }

}
