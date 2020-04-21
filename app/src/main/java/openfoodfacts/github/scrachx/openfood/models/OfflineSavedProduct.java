package openfoodfacts.github.scrachx.openfood.models;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

@Entity(indexes = {
    @Index(value = "barcode", unique = true)
})

public class OfflineSavedProduct implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private Long id;
    private String barcode;
    private String productDetails;
    @Index
    private boolean isDataUploaded;

    @Generated(hash = 39695213)
    public OfflineSavedProduct(Long id, String barcode, String productDetails, boolean isDataUploaded) {
        this.id = id;
        this.barcode = barcode;
        this.productDetails = productDetails;
        this.isDataUploaded = isDataUploaded;
    }

    @Generated(hash = 403273060)
    public OfflineSavedProduct() {
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public HashMap<String, String> getProductDetailsMap() {
        if (this.getProductDetails() != null) {
            ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(this.productDetails, Base64.DEFAULT));
            try {
                ObjectInputStream in = new ObjectInputStream(bis);
                try {
                    @SuppressWarnings("unchecked")
                    HashMap<String, String> hashMap = (HashMap<String, String>) in.readObject();
                    return hashMap;
                } catch (ClassNotFoundException e) {
                    Log.e(OfflineSavedProduct.class.getSimpleName(), "getProductDetailsMap", e);
                }
            } catch (IOException e) {
                Log.e(OfflineSavedProduct.class.getSimpleName(), "getProductDetailsMap", e);
            }
        }
        return null;
    }

    @Nullable
    public String getLanguage() {
        return getProductDetailsMap().get(KEYS.PARAM_LANGUAGE);
    }

    @Nullable
    public String getName() {
        HashMap<String, String> map = getProductDetailsMap();
        String language = Utils.firstNotEmpty(map.get(KEYS.PARAM_LANGUAGE), "en");
        return Utils.firstNotEmpty(map.get(KEYS.GET_PARAM_NAME(language)), map.get(KEYS.GET_PARAM_NAME("en")));
    }

    @Nullable
    public String getIngredients() {
        HashMap<String, String> map = getProductDetailsMap();
        String language = Utils.firstNotEmpty(map.get(KEYS.PARAM_LANGUAGE), "en");
        return Utils.firstNotEmpty(map.get(KEYS.GET_PARAM_INGREDIENTS(language)), map.get(KEYS.GET_PARAM_INGREDIENTS("en")));
    }

    @Nullable
    public String getImageFront() {
        return getProductDetailsMap().get(KEYS.IMAGE_FRONT);
    }

    @Nullable
    public String getImageIngredients() {
        return getProductDetailsMap().get(KEYS.IMAGE_INGREDIENTS);
    }

    @Nullable
    public String getImageNutrition() {
        return getProductDetailsMap().get(KEYS.IMAGE_NUTRITION);
    }

    @Nullable
    public String getImageFrontLocalUrl() {
        String localUrl = getProductDetailsMap().get(KEYS.IMAGE_FRONT);
        if (!TextUtils.isEmpty(localUrl)) {
            return FileUtils.LOCALE_FILE_SCHEME + localUrl;
        }
        return null;
    }

    public void setProductDetailsMap(Map<String, String> detailsMap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(detailsMap);
            out.flush();
            this.productDetails = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
        } catch (IOException e) {
            Log.e(OfflineSavedProduct.class.getSimpleName(), "setProductDetailsMap", e);
        }
    }

    public String getProductDetails() {
        return this.productDetails;
    }

    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getIsDataUploaded() {
        return this.isDataUploaded;
    }

    public void setIsDataUploaded(boolean isDataUploaded) {
        this.isDataUploaded = isDataUploaded;
    }

    @Override
    public String toString() {
        return "OfflineSavedProduct{" +
            "id=" + id +
            ", barcode='" + barcode + '\'' +
            ", isDataUploaded=" + isDataUploaded +
            ", map='" + getProductDetailsMap().toString() + '\'' +
            '}';
    }

    public static class KEYS {
        // START OF OVERVIEW KEYS
        public static String GET_PARAM_NAME(String lang) {
            return "product_name_" + lang;
        }

        public static final String PARAM_LANGUAGE = "lang";
        public static final String IMAGE_FRONT = "image_front";
        public static final String IMAGE_FRONT_UPLOADED = "image_front_uploaded";
        public static final String IMAGE_INGREDIENTS = "image_ingredients";
        public static final String IMAGE_INGREDIENTS_UPLOADED = "image_ingredients_uploaded";
        public static final String IMAGE_NUTRITION = "image_nutrition";
        public static final String IMAGE_NUTRITION_UPLOADED = "image_nutrition_uploaded";
        public static final String PARAM_BARCODE = "code";
        public static final String PARAM_QUANTITY = "quantity";
        public static final String PARAM_BRAND = "add_brands";
        public static final String PARAM_INTERFACE_LANGUAGE = "lc";
        public static final String PARAM_PACKAGING = "add_packaging";
        public static final String PARAM_CATEGORIES = "add_categories";
        public static final String PARAM_LABELS = "add_labels";
        public static final String PARAM_PERIODS_AFTER_OPENING = "periods_after_opening";
        public static final String PARAM_ORIGIN = "add_origins";
        public static final String PARAM_MANUFACTURING_PLACE = "add_manufacturing_places";
        public static final String PARAM_EMB_CODE = "add_emb_codes";
        public static final String PARAM_LINK = "link";
        public static final String PARAM_PURCHASE = "add_purchase_places";
        public static final String PARAM_STORE = "add_stores";
        public static final String PARAM_COUNTRIES = "add_countries";
        // START OF NUTRITION FACTS KEYS
        public static final String PARAM_NO_NUTRITION_DATA = "no_nutrition_data";
        public static final String PARAM_NUTRITION_DATA_PER = "nutrition_data_per";
        public static final String PARAM_SERVING_SIZE = "serving_size";

        // START OF INGREDIENTS KEYS
        public static String GET_PARAM_INGREDIENTS(String lang) {
            return "ingredients_text_" + lang;
        }

        public static final String PARAM_TRACES = "add_traces";
    }
}
