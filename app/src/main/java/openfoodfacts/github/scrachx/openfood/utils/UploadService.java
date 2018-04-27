package openfoodfacts.github.scrachx.openfood.utils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.models.SaveItem;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.SendProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by prajwalm on 04/04/18.
 */

public class UploadService extends IntentService {

    private List<SaveItem> saveItems;
    private String loginS, passS;
    private SendProductDao mSendProductDao;
    private int size;


    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (intent.getAction().equals("UploadJob")) {


            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(9);

            mSendProductDao = Utils.getAppDaoSession(getApplicationContext()).getSendProductDao();
            final SharedPreferences settingsLogin = getApplicationContext().getSharedPreferences("login", 0);
            final SharedPreferences settingsUsage = getApplicationContext().getSharedPreferences("usage", 0);
            saveItems = new ArrayList<>();
            loginS = settingsLogin.getString("user", "");
            passS = settingsLogin.getString("pass", "");
            uploadProducts();


        }


    }

    private void uploadProducts() {


        OpenFoodAPIClient apiClient = new OpenFoodAPIClient(getApplication());
        final List<SendProduct> listSaveProduct = mSendProductDao.loadAll();
        size = saveItems.size();

        for (final SendProduct product : listSaveProduct) {
            if (isEmpty(product.getBarcode()) || isEmpty(product.getImgupload_front())) {
                continue;
            }

            if (!loginS.isEmpty() && !passS.isEmpty()) {
                product.setUserId(loginS);
                product.setPassword(passS);
            }

            if (isNotEmpty(product.getImgupload_ingredients())) {
                product.compress(ProductImageField.INGREDIENTS);
            }

            if (isNotEmpty(product.getImgupload_nutrition())) {
                product.compress(ProductImageField.NUTRITION);
            }

            if (isNotEmpty(product.getImgupload_front())) {
                product.compress(ProductImageField.FRONT);
            }
            size--;

            apiClient.postForNotification(getApplicationContext(), product, value -> {
                if (value) {
                    int productIndex = listSaveProduct.indexOf(product);

                    if (productIndex >= 0 && productIndex < saveItems.size()) {
                        saveItems.remove(productIndex);
                    }
                    mSendProductDao.deleteInTx(mSendProductDao.queryBuilder().where(SendProductDao.Properties.Barcode.eq(product.getBarcode())).list());
                }
            });
        }

    }

}
