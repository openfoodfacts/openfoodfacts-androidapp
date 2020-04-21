package openfoodfacts.github.scrachx.openfood.utils;

import android.os.AsyncTask;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import openfoodfacts.github.scrachx.openfood.images.ProductImage;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProductDao;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public class OfflineProductService {
    private static final String LOG_TAG = "OfflineProductService";
    private final OpenFoodAPIService apiClient;
    private boolean isRunning = false;

    private static class Loader {
        // static synchronized singleton
        static volatile OfflineProductService INSTANCE = new OfflineProductService();
    }

    public static OfflineProductService sharedInstance() {
        return Loader.INSTANCE;
    }

    private OfflineProductService() {
        super();
        this.apiClient = CommonApiManager.getInstance().getOpenFoodApiService();
    }

    private static OfflineSavedProductDao getOfflineProductDAO() {
        return OFFApplication.getInstance().getDaoSession().getOfflineSavedProductDao();
    }

    public static OfflineSavedProduct getOfflineProductByBarcode(String barcode) {
        return getOfflineProductDAO().queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(barcode)).unique();
    }

    public static List<OfflineSavedProduct> getListOfflineProductsNeedingSync() {
        return getOfflineProductDAO().queryBuilder()
            .where(OfflineSavedProductDao.Properties.Barcode.isNotNull())
            .list();
    }

    public void startUploadQueue() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            AsyncTask.execute(this::startUploadQueue);
            return;
        }
        startUploadQueueSynchronous();
    }

    //TODO: check PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enableMobileDataUpload", true) and current network value
    private void startUploadQueueSynchronous() {
        boolean shouldActuallyRunThisTime = false;
        if (!isRunning) {
            synchronized (OfflineProductService.class) {
                if (!isRunning) {
                    isRunning = true;
                    shouldActuallyRunThisTime = true;
                }
            }
        }

        if (!shouldActuallyRunThisTime) {
            Log.d(LOG_TAG, "Do not startUploadQueueSynchronous because it is already running");
            return;
        }

        Log.d(LOG_TAG, "startUploadQueueSynchronous");

        final List<OfflineSavedProduct> listSaveProduct = getListOfflineProductsNeedingSync();

        for (final OfflineSavedProduct product : listSaveProduct) {
            if (TextUtils.isEmpty(product.getBarcode())) {
                Log.d(LOG_TAG, "Ignore product because empty barcode: " + product.toString());
                continue;
            }

            Log.d(LOG_TAG, "Start treating of product " + product.toString());

            try {
                boolean ok = addProductToServerIfNeeded(product);
                ok = ok && uploadImageIfNeeded(product, ProductImageField.FRONT);
                ok = ok && uploadImageIfNeeded(product, ProductImageField.INGREDIENTS);
                ok = ok && uploadImageIfNeeded(product, ProductImageField.NUTRITION);

                if (ok) {
                    getOfflineProductDAO().deleteByKey(product.getId());
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error getting the product", e);
            }
        }

        isRunning = false;
        Log.d(LOG_TAG, "END OF uploadQueueSynchronous");
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
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_FRONT);
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_INGREDIENTS);
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_NUTRITION);
        // Remove the status of the images from the HashMap before uploading the product details
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_FRONT_UPLOADED);
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_INGREDIENTS_UPLOADED);
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_NUTRITION_UPLOADED);

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
            State state = this.apiClient
                .saveProductSingle(product.getBarcode(), productDetails, OpenFoodAPIService.PRODUCT_API_COMMENT + " " + Utils.getVersionName(OFFApplication.getInstance()))
                .blockingGet();

            boolean isResponseOk = state.getStatus() == 1;

            if (isResponseOk) {
                product.setIsDataUploaded(true);
                getOfflineProductDAO().insertOrReplace(product);
                Log.i(LOG_TAG, "product " + product.getBarcode() + " uploaded");

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
        return imageUploaded || TextUtils.isEmpty(imageFilePath);
    }

    private boolean uploadImageIfNeeded(OfflineSavedProduct product, ProductImageField imageField) {
        String imageType = imageTypeFromImageField(imageField);

        String code = product.getBarcode();
        HashMap<String, String> productDetails = product.getProductDetailsMap();

        String imageFilePath = productDetails.get("image_" + imageType);

        if (imageFilePath == null || !needImageUpload(productDetails, imageType)) {
            // no need or nothing to upload
            return true;
        }

        Log.d(LOG_TAG, "Uploading image_" + imageType + " for product " + code);

        Map<String, RequestBody> imgMap = createRequestBodyMap(code, productDetails, imageField);
        RequestBody image = ProductImage.createImageRequest(new File(imageFilePath));
        imgMap.put("imgupload_" + imageType + "\"; filename=\"" + imageType + "_" + product.getLanguage() + ".png\"", image);

        try {
            JsonNode jsonNode = this.apiClient.saveImageSingle(imgMap)
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

            Map<String, String> queryMap = buildQueryMap(jsonNode, OpenFoodAPIClient.fillWithUserLoginInfo(imgMap));

            JsonNode node = OfflineProductService.this.apiClient
                .editImageSingle(code, queryMap)
                .blockingGet();

            productDetails.put("image_" + imageType + "_uploaded", "true");
            product.setProductDetailsMap(productDetails);
            getOfflineProductDAO().insertOrReplace(product);

            Log.d(LOG_TAG, "Uploaded image_" + imageType + " for product " + code + " /node= " + node.toString());

            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return false;
        }
    }

    private Map<String, RequestBody> createRequestBodyMap(String code, HashMap<String, String> productDetails, ProductImageField front) {
        Map<String, RequestBody> imgMap = new HashMap<>();
        RequestBody barcode = RequestBody.create(MediaType.parse(OpenFoodAPIClient.TEXT_PLAIN), code);
        RequestBody imageField = RequestBody.create(MediaType.parse(OpenFoodAPIClient.TEXT_PLAIN), front.toString() + '_' + productDetails.get("lang"));
        imgMap.put("code", barcode);
        imgMap.put("imagefield", imageField);
        return imgMap;
    }

    private Map<String, String> buildQueryMap(JsonNode jsonNode, String login) {
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
