package openfoodfacts.github.scrachx.openfood.images;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

import java.io.File;

public class ProductImage {

    private final RequestBody code;

    private final RequestBody field;

    private final RequestBody imguploadFront;

    private final RequestBody imguploadIngredients;

    private final RequestBody imguploadNutrition;

    private final RequestBody imguploadOther;

    private String filePath;

    private String barcode;
    private String language;

    private ProductImageField imageField;

    public ProductImage(String code, ProductImageField field, File image) {
        this(code,field,image, LocaleHelper.getLanguage(OFFApplication.getInstance()));

    }
    public ProductImage(String code, ProductImageField field, File image,String language) {
        this.code = RequestBody.create(MediaType.parse(OpenFoodAPIClient.TEXT_PLAIN), code);
        this.language=language;
        this.field = RequestBody.create(MediaType.parse(OpenFoodAPIClient.TEXT_PLAIN), field.toString() + '_' + language);

        switch (field) {
            case FRONT:
                this.imguploadFront = createImageRequest(image);
                this.imguploadIngredients = null;
                this.imguploadNutrition = null;
                this.imguploadOther = null;
                break;
            case INGREDIENTS:
                this.imguploadIngredients = createImageRequest(image);
                this.imguploadFront = null;
                this.imguploadNutrition = null;
                this.imguploadOther = null;
                break;
            case NUTRITION:
                this.imguploadNutrition = createImageRequest(image);
                this.imguploadFront = null;
                this.imguploadIngredients = null;
                this.imguploadOther = null;
                break;
            case OTHER:
                this.imguploadOther = createImageRequest(image);
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

    public String getLanguage() {
        return language;
    }

    public static RequestBody createImageRequest(File image) {
        return RequestBody.create(MediaType.parse("image/*"), image);
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
