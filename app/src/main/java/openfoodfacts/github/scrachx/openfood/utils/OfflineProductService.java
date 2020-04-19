package openfoodfacts.github.scrachx.openfood.utils;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.File;
import java.util.HashMap;
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
            .where(OfflineSavedProductDao.Properties.IsDataUploaded.eq(false))
            .list();
    }

    public void startUploadQueue() {
        //TODO: launch on a bg thread !
        startUploadQueueSynchronous();
    }

    //TODO: make sure this queue is run on a bg thread. On a background service if possible.
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
            return;
        }

        //TODO: check PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enableMobileDataUpload", true) and current network value

        final List<OfflineSavedProduct> listSaveProduct = getListOfflineProductsNeedingSync();

        for (final OfflineSavedProduct product : listSaveProduct) {
            if (TextUtils.isEmpty(product.getBarcode()) || TextUtils.isEmpty(product.getImageFront())) {
                continue;
            }

            String fields = TextUtils.join(",",
                new String[]{
                    "link", "quantity", "image_ingredients_url",
                    OfflineSavedProduct.KEYS.GET_PARAM_INGREDIENTS(product.getLanguage()),
                    OfflineSavedProduct.KEYS.GET_PARAM_NAME(product.getLanguage())
                });

            //TODO: make this call synchronous ?
            try {
                State state = this.apiClient
                    .getProductByBarcodeSingle(product.getBarcode(), fields, Utils.getUserAgent(Utils.HEADER_USER_AGENT_SEARCH))
                    .blockingGet();

                //TODO: put those in the right place
                uploadImageIfNeeded(product, ProductImageField.FRONT);
                uploadImageIfNeeded(product, ProductImageField.INGREDIENTS);
                uploadImageIfNeeded(product, ProductImageField.NUTRITION);
                addProductToServer(product);

                if (state.getStatus() == 0) {
                    //TODO: Product doesn't exist yet on the server. Add as it is.
                } else {
                    //TODO: Product already exists on the server. Compare values saved locally with the values existing on server.
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error getting the product", e);
            }
        }

        isRunning = false;
        //TODO: queue is done !
    }

    /**
     * Performs network call and uploads the product to the server.
     *
     * @param product The offline product to be uploaded to the server.
     */
    private void addProductToServer(OfflineSavedProduct product) {
        HashMap<String, String> productDetails = product.getProductDetailsMap();
        // Remove the images from the HashMap before uploading the product details
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_FRONT);
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_INGREDIENTS);
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_NUTRITION);
        // Remove the status of the images from the HashMap before uploading the product details
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_FRONT_UPLOADED);
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_INGREDIENTS_UPLOADED);
        productDetails.remove(OfflineSavedProduct.KEYS.IMAGE_NUTRITION_UPLOADED);

        //TODO: make sure only updated properties are sent
        try {
            State state = this.apiClient
                .saveProductSingle(product.getBarcode(), productDetails, OpenFoodAPIService.PRODUCT_API_COMMENT + " " + Utils.getVersionName(OFFApplication.getInstance()))
                .blockingGet();

            //TODO: check the response status
            boolean isResponseOk = true;

            if (isResponseOk) {
                product.setIsDataUploaded(false);
                getOfflineProductDAO().insertOrReplace(product);
            }
            //TODO: mark the product as "uploaded"

            //TODO: delete the offline product ? not sure, maybe we want to keep it locally ?
            // mOfflineSavedProductDao.deleteInTx(mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(product.getBarcode())).list());
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    private boolean uploadImageIfNeeded(OfflineSavedProduct product, ProductImageField imageField) {
        String imageType = "";
        switch (imageField) {
            case FRONT:
                imageType = "front";
                break;
            case INGREDIENTS:
                imageType = "ingredients";
                break;
            case NUTRITION:
                imageType = "nutrition";
                break;
        }

        String code = product.getBarcode();
        HashMap<String, String> productDetails = product.getProductDetailsMap();

        boolean imageUploaded = "true".equals(productDetails.get("image_" + imageType + "_uploaded"));
        String imageFilePath = productDetails.get("image_" + imageType);

        if (imageUploaded || TextUtils.isEmpty(imageFilePath)) {
            // no need or nothing to upload
            return true;
        }

        Map<String, RequestBody> imgMap = createRequestBodyMap(code, productDetails, imageField);
        RequestBody image = ProductImage.createImageRequest(new File(imageFilePath));
        imgMap.put("imgupload_" + imageType + "\"; filename=\"" + imageType + "_" + product.getLanguage() + ".png\"", image);

        String finalImageType = imageType;
        try {
            JsonNode jsonNode = this.apiClient.saveImageSingle(imgMap)
                .blockingGet();
            String status = jsonNode.get("status").asText();
            if (status.equals("status not ok")) {
                String error = jsonNode.get("error").asText();
                if (error.equals("This picture has already been sent.")) {
                    productDetails.put("image_" + finalImageType + "_uploaded", "true");
                    product.setProductDetailsMap(productDetails);
                    return true;
                }
                Log.e(LOG_TAG, "Error uploading " + finalImageType + ": " + error);
                return false;
            }
            productDetails.put("image_" + finalImageType + "_uploaded", "true");
            product.setProductDetailsMap(productDetails);

            Map<String, String> queryMap = buildQueryMap(jsonNode, OpenFoodAPIClient.fillWithUserLoginInfo(imgMap));

            JsonNode node = OfflineProductService.this.apiClient
                .editImageSingle(code, queryMap)
                .blockingGet();
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
