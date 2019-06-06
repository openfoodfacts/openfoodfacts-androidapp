package openfoodfacts.github.scrachx.openfood.models;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

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
        this.code = RequestBody.create(MediaType.parse(OpenFoodAPIClient.TEXT_PLAIN), code);
        this.field = RequestBody.create(MediaType.parse(OpenFoodAPIClient.TEXT_PLAIN), field.toString() + '_' + LocaleHelper.getLanguage(OFFApplication.getInstance()));

        switch (field) {
            case FRONT:
                this.imguploadFront = OpenFoodAPIClient.createImageRequest(image);
                this.imguploadIngredients = null;
                this.imguploadNutrition = null;
                this.imguploadOther = null;
                break;
            case INGREDIENTS:
                this.imguploadIngredients = OpenFoodAPIClient.createImageRequest(image);
                this.imguploadFront = null;
                this.imguploadNutrition = null;
                this.imguploadOther = null;
                break;
            case NUTRITION:
                this.imguploadNutrition = OpenFoodAPIClient.createImageRequest(image);
                this.imguploadFront = null;
                this.imguploadIngredients = null;
                this.imguploadOther = null;
                break;
            case OTHER:
                this.imguploadOther = OpenFoodAPIClient.createImageRequest(image);
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
