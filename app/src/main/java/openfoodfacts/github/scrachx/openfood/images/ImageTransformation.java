package openfoodfacts.github.scrachx.openfood.images;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ImageTransformation {
    public static final int NO_VALUE = -1;
    private static final String LEFT = "x1";
    private static final String RIGHT = "x2";
    private static final String TOP = "y1";
    private static final String BOTTOM = "y2";
    private static final String ANGLE = "angle";
    /**
     * image rotation in degree
     */
    private int rotationInDegree;
    private Rect cropRectangle;
    private String initImageUrl;
    private String initImageId;

    private ImageTransformation() {
    }

    public static void addTransformToMap(ImageTransformation newServerTransformation, HashMap<String, String> imgMap) {
        imgMap.put(ANGLE, Integer.toString(newServerTransformation.getRotationInDegree()));
        final Rect cropRectangle = newServerTransformation.getCropRectangle();
        if (cropRectangle != null) {
            imgMap.put(LEFT, Integer.toString(cropRectangle.left));
            imgMap.put(RIGHT, Integer.toString(cropRectangle.right));
            imgMap.put(TOP, Integer.toString(cropRectangle.top));
            imgMap.put(BOTTOM, Integer.toString(cropRectangle.bottom));
        }
    }

    public String getInitImageId() {
        return initImageId;
    }

    public ImageTransformation(int rotationInDegree, Rect cropRectangle) {
        this.rotationInDegree = rotationInDegree;
        this.cropRectangle = cropRectangle;
    }

    @Override
    public String toString() {
        return "ImageTransformation{" +
            "rotationInDegree=" + rotationInDegree +
            ", cropRectangle=" + cropRectangle +
            ", initImageUrl='" + initImageUrl + '\'' +
            '}';
    }

    @SuppressWarnings("EqualsReplaceableByObjectsCall")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageTransformation that = (ImageTransformation) o;

        if (rotationInDegree != that.rotationInDegree) {
            return false;
        }
        if (cropRectangle != null ? !cropRectangle.equals(that.cropRectangle) : that.cropRectangle != null) {
            return false;
        }
        return initImageUrl != null ? initImageUrl.equals(that.initImageUrl) : that.initImageUrl == null;
    }

    @Override
    public int hashCode() {
        int result = rotationInDegree;
        result = 31 * result + (cropRectangle != null ? cropRectangle.hashCode() : 0);
        result = 31 * result + (initImageUrl != null ? initImageUrl.hashCode() : 0);
        return result;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(initImageUrl);
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public int getRotationInDegree() {
        return rotationInDegree;
    }

    public Rect getCropRectangle() {
        return cropRectangle;
    }

    public String getInitImageUrl() {
        return initImageUrl;
    }

    /**
     * @param product the product
     * @param productImageField the type of the image
     * @param language the language
     * @return the image transformation containing the initial url and the transformation (rotation/crop) for screen
     */
    public static ImageTransformation getScreenTransformation(Product product, final ProductImageField productImageField, String language) {

        ImageTransformation res = getInitialServerTransformation(product, productImageField, language);
        if (res.isEmpty()) {
            return res;
        }

        //if a rotation + crop we have to rotate the crop area.
        //off applies the crop on the rotated image and the android librairy applies the crop before the rotation... so we should
        // transform the crop from off to the android library version.
        if (res.getCropRectangle() != null && res.rotationInDegree != 0) {
            applyRotationOnCropRectangle(product, productImageField, language, res, true);
        }
        return res;
    }

    private static void applyRotationOnCropRectangle(Product product, ProductImageField productImageField, String language, ImageTransformation res, boolean inverse) {
        //if a crop and a rotation is done we should rotate the cropped rectangle
        final String imageKey = ImageKeyHelper.getImageStringKey(productImageField, language);
        final Map<String, ?> imageDetails = product.getImageDetails(imageKey);
        String initImageId = (String) imageDetails.get(ImageKeyHelper.IMG_ID);
        final Map<String, ?> imageDetailsInitImage = product.getImageDetails(initImageId);
        if (imageDetailsInitImage != null) {
            Map<String, Map<String, ?>> sizes = (Map<String, Map<String, ?>>) imageDetailsInitImage.get("sizes");
            try {
                RectF initCrop = toRectF(res.getCropRectangle());
                int height = getDimension(sizes, "h");
                int width = getDimension(sizes, "w");
                if (height != NO_VALUE && width != NO_VALUE) {
                    int rotationToApply = res.rotationInDegree;
                    //we will rotate the whole image to have the top left values
                    RectF wholeImage = new RectF(0, 0, width, height);
                    Matrix m = new Matrix();
                    if (inverse) {
                        m.setRotate(rotationToApply);
                        m.mapRect(wholeImage);
                        //the whole image whith the final width/height
                        wholeImage = new RectF(0, 0, wholeImage.width(), wholeImage.height());
                        m.reset();
                    }
                    //now wholeImage and initCrop are in the same dimension as in the server.
                    //to revert the off crop to the initial image without rotation
                    m.setRotate(inverse ? -rotationToApply : rotationToApply);
                    m.mapRect(initCrop);
                    m.mapRect(wholeImage);
                    m.reset();
                    //we translate the crop rectangle to the origin
                    m.setTranslate(-wholeImage.left, -wholeImage.top);
                    m.mapRect(initCrop);
                    res.cropRectangle = toRect(initCrop);
                }
            } catch (Exception e) {
                Log.e(ImageTransformation.class.getSimpleName(), "can't process image for product " + product.getCode(), e);
            }
        }
    }

    public static ImageTransformation getInitialServerTransformation(Product product, final ProductImageField productImageField, String language) {
        final String imageKey = ImageKeyHelper.getImageStringKey(productImageField, language);
        final Map<String, ?> imageDetails = product.getImageDetails(imageKey);
        ImageTransformation res = new ImageTransformation();
        if (imageDetails == null) {
            return res;
        }
        String initImageId = (String) imageDetails.get(ImageKeyHelper.IMG_ID);
        if (StringUtils.isBlank(initImageId)) {
            return res;
        }
        res.initImageId = initImageId;
        res.initImageUrl = ImageKeyHelper.getImageUrl(product.getCode(), initImageId, ImageKeyHelper.IMAGE_EDIT_SIZE_FILE);
        res.rotationInDegree = getImageRotation(imageDetails);
        RectF initCrop = getImageCropRect(imageDetails);
        if (initCrop != null) {
            res.cropRectangle = toRect(initCrop);
        }
        return res;
    }

    /**
     * @param product the product
     * @param productImageField the type of the image
     * @param language the language
     * @return the image transformation containing the initial url and the transformation (rotation/crop) for screen
     */
    public static ImageTransformation toServerTransformation(ImageTransformation screenTransformation, Product product, final ProductImageField productImageField,
                                                             String language) {
        ImageTransformation res = getInitialServerTransformation(product, productImageField, language);
        if (res.isEmpty()) {
            return res;
        }
        res.rotationInDegree = screenTransformation.rotationInDegree;
        res.cropRectangle = screenTransformation.cropRectangle;
        if (res.getCropRectangle() != null && res.rotationInDegree != 0) {
            applyRotationOnCropRectangle(product, productImageField, language, res, false);
        }
        return res;
    }

    /**
     * @param sizes the map of sizes
     * @param key the key
     * @return NO_VALUE if can't parse the size
     */
    public static int getDimension(Map<String, Map<String, ?>> sizes, String key) {
        final Object value = sizes.get(ImageKeyHelper.IMAGE_EDIT_SIZE).get(key);
        if (value == null) {
            return NO_VALUE;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private static Rect toRect(RectF init) {
        if (init == null) {
            return null;
        }
        return new Rect((int) Math.ceil(init.left), (int) Math.ceil(init.top), (int) Math.ceil(init.right), (int) Math.ceil(init.bottom));
    }

    private static RectF toRectF(Rect init) {
        if (init == null) {
            return null;
        }
        return new RectF((float) init.left, (float) Math.ceil(init.top), (float) Math.ceil(init.right), (float) Math.ceil(init.bottom));
    }

    /**
     * @param imgDetails
     * @return the angle in degree from the map.
     */
    private static int getImageRotation(Map<String, ?> imgDetails) {
        int rotation = 0;
        String rotationAsString = (String) imgDetails.get(ANGLE);
        if (rotationAsString != null) {
            try {
                rotation = Integer.parseInt(rotationAsString);
            } catch (NumberFormatException e) {
                Log.e(ImageKeyHelper.class.getSimpleName(), "can parse rotate info", e);
            }
        }
        return rotation;
    }

    private static RectF getImageCropRect(Map<String, ?> imgDetails) {
        String x1AsString = (String) imgDetails.get(LEFT);
        String x2AsString = (String) imgDetails.get(RIGHT);
        String y1AsString = (String) imgDetails.get(TOP);
        String y2AsString = (String) imgDetails.get(BOTTOM);
        try {
            if (x1AsString != null && x2AsString != null && y1AsString != null && y2AsString != null) {
                float x1 = getAsFloat(x1AsString);
                float x2 = getAsFloat(x2AsString);
                float y1 = getAsFloat(y1AsString);
                float y2 = getAsFloat(y2AsString);
                if (x2 > x1 && y2 > y1) {
                    return new RectF(x1, y1, x2, y2);
                }
            }
        } catch (Exception e) {
            Log.e(ImageKeyHelper.class.getSimpleName(), "can parse crop  info", e);
        }
        return null;
    }

    private static float getAsFloat(String valueAsString) {
        return (float) Double.parseDouble(valueAsString);
    }
}
