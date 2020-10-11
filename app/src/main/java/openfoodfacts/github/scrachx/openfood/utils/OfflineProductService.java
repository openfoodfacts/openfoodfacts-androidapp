package openfoodfacts.github.scrachx.openfood.utils;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProductDao;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent;
import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public class OfflineProductService {
    private static final String LOG_TAG = "OfflineProductService";
    private final ProductsAPI apiClient;

    private static OfflineSavedProductDao getOfflineProductDAO() {
        return OFFApplication.getDaoSession().getOfflineSavedProductDao();
    }

    public static OfflineProductService sharedInstance() {
        return Loader.INSTANCE;
    }

    private OfflineProductService() {
        super();
        this.apiClient = CommonApiManager.getInstance().getProductsApi();
    }

    private static class Loader {
        // static synchronized singleton
        static final OfflineProductService INSTANCE = new OfflineProductService();
    }

    public static OfflineSavedProduct getOfflineProductByBarcode(String barcode) {
        return getOfflineProductDAO().queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(barcode)).unique();
    }

    private static List<OfflineSavedProduct> getListOfflineProducts() {
        return getOfflineProductDAO().queryBuilder()
            .where(OfflineSavedProductDao.Properties.Barcode.isNotNull())
            .where(OfflineSavedProductDao.Properties.Barcode.notEq(""))
            .list();
    }

    private static List<OfflineSavedProduct> getListOfflineProductsWithoutDataSynced() {
        return getOfflineProductDAO().queryBuilder()
            .where(OfflineSavedProductDao.Properties.Barcode.isNotNull())
            .where(OfflineSavedProductDao.Properties.Barcode.notEq(""))
            .where(OfflineSavedProductDao.Properties.IsDataUploaded.notEq(true))
            .list();
    }

    /**
     * @return true if there is still products to upload, false otherwise
     */
    public Single<Boolean> uploadAll(boolean includeImages) {
        return Single.fromCallable(() -> {
            final List<OfflineSavedProduct> listSaveProduct = OfflineProductService.getListOfflineProducts();

            for (final OfflineSavedProduct product : listSaveProduct) {
                if (TextUtils.isEmpty(product.getBarcode())) {
                    Log.d(LOG_TAG, "Ignore product because empty barcode: " + product.toString());
                    continue;
                }

                Log.d(LOG_TAG, "Start treating of product " + product.toString());

                try {
                    boolean ok = addProductToServerIfNeeded(product);

                    if (includeImages) {
                        ok = ok && uploadImageIfNeeded(product, ProductImageField.FRONT);
                        ok = ok && uploadImageIfNeeded(product, ProductImageField.INGREDIENTS);
                        ok = ok && uploadImageIfNeeded(product, ProductImageField.NUTRITION);

                        if (ok) {
                            OfflineProductService.getOfflineProductDAO().deleteByKey(product.getId());
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Error getting the product", e);
                }
            }
            if (includeImages) {
                return !OfflineProductService.getListOfflineProducts().isEmpty();
            }
            return !OfflineProductService.getListOfflineProductsWithoutDataSynced().isEmpty();
        });
    }

    /**
     * Performs network call and uploads the product to the server.
     *
     * @param product The offline product to be uploaded to the server.
     */
    private boolean addProductToServerIfNeeded(OfflineSavedProduct product) {
        if (product.getIsDataUploaded()) {
            return true;
        }

        HashMap<String, String> productDetails = product.getProductDetailsMap();
        // Remove the images from the HashMap before uploading the product details
        productDetails.remove(ApiFields.Keys.IMAGE_FRONT);
        productDetails.remove(ApiFields.Keys.IMAGE_INGREDIENTS);
        productDetails.remove(ApiFields.Keys.IMAGE_NUTRITION);
        // Remove the status of the images from the HashMap before uploading the product details
        productDetails.remove(ApiFields.Keys.IMAGE_FRONT_UPLOADED);
        productDetails.remove(ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED);
        productDetails.remove(ApiFields.Keys.IMAGE_NUTRITION_UPLOADED);

        Iterator<Map.Entry<String, String>> it = productDetails.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            if (TextUtils.isEmpty(entry.getValue())) {
                //remove null values
                it.remove();
            }
        }

        Log.d(LOG_TAG, product.getBarcode() + " Uploading data: " + productDetails.toString());

        try {
            State state = apiClient
                .saveProductSingle(product.getBarcode(), productDetails, OpenFoodAPIClient.getCommentToUpload())
                .blockingGet();

            boolean isResponseOk = state.getStatus() == 1;

            if (isResponseOk) {
                product.setIsDataUploaded(true);
                OfflineProductService.getOfflineProductDAO().insertOrReplace(product);
                Log.i(LOG_TAG, "product " + product.getBarcode() + " uploaded");

                EventBus.getDefault().post(new ProductNeedsRefreshEvent(product.getBarcode()));

                return true;
            } else {
                Log.i(LOG_TAG, "could not upload product?");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return false;
    }

    private static String imageTypeFromImageField(ProductImageField imageField) {
        switch (imageField) {
            case FRONT:
                return "front";
            case INGREDIENTS:
                return "ingredients";
            case NUTRITION:
                return "nutrition";
        }
        return "other";
    }

    private static boolean needImageUpload(HashMap<String, String> productDetails, String imageType) {
        boolean imageUploaded = "true".equals(productDetails.get("image_" + imageType + "_uploaded"));
        String imageFilePath = productDetails.get("image_" + imageType);
        return !imageUploaded && !TextUtils.isEmpty(imageFilePath);
    }

    private boolean uploadImageIfNeeded(OfflineSavedProduct product, ProductImageField imageField) {
        String imageType = imageTypeFromImageField(imageField);

        String code = product.getBarcode();
        HashMap<String, String> productDetails = product.getProductDetailsMap();

        String imageFilePath = productDetails.get("image_" + imageType);

        if (imageFilePath == null || !needImageUpload(productDetails, imageType)) {
            // no need or nothing to upload
            Log.d(LOG_TAG, "No need to upload image_" + imageType + " for product " + code);
            return true;
        }

        Log.d(LOG_TAG, "Uploading image_" + imageType + " for product " + code);

        Map<String, RequestBody> imgMap = createRequestBodyMap(code, productDetails, imageField);
        RequestBody image = ProductImage.createImageRequest(new File(imageFilePath));
        imgMap.put("imgupload_" + imageType + "\"; filename=\"" + imageType + "_" + product.getLanguage() + ".png\"", image);

        try {
            JsonNode jsonNode = apiClient.saveImageSingle(imgMap)
                .blockingGet();
            String status = jsonNode.get("status").asText();
            if (status.equals("status not ok")) {
                String error = jsonNode.get("error").asText();
                if (error.equals("This picture has already been sent.")) {
                    productDetails.put("image_" + imageType + "_uploaded", "true");
                    product.setProductDetailsMap(productDetails);
                    getOfflineProductDAO().insertOrReplace(product);
                    return true;
                }
                Log.e(LOG_TAG, "Error uploading " + imageType + ": " + error);
                return false;
            }

            productDetails.put("image_" + imageType + "_uploaded", "true");
            product.setProductDetailsMap(productDetails);
            getOfflineProductDAO().insertOrReplace(product);

            Log.d(LOG_TAG, "Uploaded image_" + imageType + " for product " + code);

            EventBus.getDefault().post(new ProductNeedsRefreshEvent(code));

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return false;
        }
    }

    private static Map<String, RequestBody> createRequestBodyMap(String code, HashMap<String, String> productDetails, ProductImageField front) {
        Map<String, RequestBody> imgMap = new HashMap<>();
        RequestBody barcode = RequestBody.create(MediaType.parse(OpenFoodAPIClient.MIME_TEXT), code);
        RequestBody imageField = RequestBody.create(MediaType.parse(OpenFoodAPIClient.MIME_TEXT), front.toString() + '_' + productDetails.get("lang"));
        imgMap.put("code", barcode);
        imgMap.put("imagefield", imageField);
        return imgMap;
    }

    private static Map<String, String> buildQueryMap(JsonNode jsonNode, String login) {
        Map<String, String> queryMap = buildImageQueryMap(jsonNode);
        queryMap.put("comment", OpenFoodAPIClient.getCommentToUpload(login));
        return queryMap;
    }

    private static Map<String, String> buildImageQueryMap(JsonNode jsonNode) {
        String imagefield = jsonNode.get("imagefield").asText();
        String imgid = jsonNode.get("image").get("imgid").asText();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("imgid", imgid);
        queryMap.put("id", imagefield);
        return queryMap;
    }
}
