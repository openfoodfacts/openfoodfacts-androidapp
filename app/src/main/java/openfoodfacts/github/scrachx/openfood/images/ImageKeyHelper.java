package openfoodfacts.github.scrachx.openfood.images;

import android.os.Bundle;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import org.apache.commons.lang3.StringUtils;

public class ImageKeyHelper {
    public static final String IMAGE_URL = "imageurl";
    public static final String IMAGE_FILE = "imagefile";
    public static final String IMAGE_TYPE = "imageType";
    public static final String PRODUCT_BARCODE = "code";
    public static final String PRODUCT = "product";
    public static final String LANGUAGE = "language";
    public static final String IMAGE_STRING_ID = "id";
    public static final String IMG_ID = "imgid";
    static final String IMAGE_EDIT_SIZE = "400";
    public static final String IMAGE_EDIT_SIZE_FILE = "." + IMAGE_EDIT_SIZE;

    private ImageKeyHelper() {
    }

    public static String getImageStringKey(ProductImageField field, Product product) {
        return getImageStringKey(field, product.getLang());
    }

    public static String getImageStringKey(ProductImageField field, String language) {
        return field.toString() + '_' + language;
    }

    public static String getLanguageCodeFromUrl(ProductImageField field, String url) {
        if (StringUtils.isBlank(url) || field == null) {
            return null;
        }
        return StringUtils.substringBefore(StringUtils.substringAfterLast(url, field.toString() + "_"), ".");
    }

    public static Bundle createImageBundle(ProductImageField imageType, Product product, String language, String imageUrl) {
        Bundle bundle = new Bundle();
        bundle.putString(ImageKeyHelper.IMAGE_URL, imageUrl);
        if (product != null) {
            bundle.putSerializable(ImageKeyHelper.PRODUCT, product);
            bundle.putSerializable(ImageKeyHelper.IMAGE_TYPE, imageType);
            bundle.putString(ImageKeyHelper.LANGUAGE, language);
        }
        return bundle;
    }

    public static int getResourceIdForEditAction(ProductImageField field) {
        switch (field) {
            case FRONT:
                return R.string.edit_front_image;
            case NUTRITION:
                return R.string.edit_nutrition_image;
            case INGREDIENTS:
                return R.string.edit_ingredients_image;
            default:
                return R.string.edit_other_image;
        }
    }

    public static int getResourceId(ProductImageField field) {
        switch (field) {
            case FRONT:
                return R.string.front_short_picture;
            case NUTRITION:
                return R.string.nutrition_facts;
            case INGREDIENTS:
                return R.string.ingredients;
            default:
                return R.string.other_picture;
        }
    }

    public static String getImageUrl(String barcode, String imageName, String size) {
        String baseUrlString = "https://static.openfoodfacts.org/images/products/";
        String barcodePattern = barcode;
        if (barcodePattern.length() > 8) {
            barcodePattern = new StringBuilder(barcode)
                .insert(3, "/")
                .insert(7, "/")
                .insert(11, "/")
                .toString();
        }

        return baseUrlString + barcodePattern + "/" + imageName + size + ".jpg";
    }
}
