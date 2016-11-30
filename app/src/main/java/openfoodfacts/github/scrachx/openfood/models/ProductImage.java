package openfoodfacts.github.scrachx.openfood.models;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ProductImage {

    private final RequestBody code;

    private final RequestBody field;

    private final RequestBody imguploadFront;

    private final RequestBody imguploadIngredients;

    private final RequestBody imguploadNutrition;

    public ProductImage(String code, ProductImageField field, File image) {
        this.code = RequestBody.create(MediaType.parse("text/plain"), code);
        this.field = RequestBody.create(MediaType.parse("text/plain"), field.toString());

        switch (field) {
            case FRONT:
                this.imguploadFront = RequestBody.create(MediaType.parse("image/*"), image);
                this.imguploadIngredients = null;
                this.imguploadNutrition = null;
                break;
            case INGREDIENTS:
                this.imguploadIngredients = RequestBody.create(MediaType.parse("image/*"), image);
                this.imguploadFront = null;
                this.imguploadNutrition = null;
                break;
            case NUTRITION:
                this.imguploadNutrition = RequestBody.create(MediaType.parse("image/*"), image);
                this.imguploadFront = null;
                this.imguploadIngredients = null;
                break;
            default:
                this.imguploadNutrition = null;
                this.imguploadFront = null;
                this.imguploadIngredients = null;
                break;
        }
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
}
