package openfoodfacts.github.scrachx.openfood.models.entities;

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

import openfoodfacts.github.scrachx.openfood.network.ApiFields;
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
        return getProductDetailsMap().get(ApiFields.Keys.LANG);
    }

    @Nullable
    public String getName() {
        HashMap<String, String> map = getProductDetailsMap();
        String language = Utils.firstNotEmpty(map.get(ApiFields.Keys.LANG), "en");
        return Utils.firstNotEmpty(map.get(ApiFields.Keys.lcProductNameKey(language)), map.get(ApiFields.Keys.lcProductNameKey("en")));
    }

    @Nullable
    public String getIngredients() {
        HashMap<String, String> map = getProductDetailsMap();
        String language = Utils.firstNotEmpty(map.get(ApiFields.Keys.LANG), "en");
        return Utils.firstNotEmpty(map.get(ApiFields.Keys.lcIngredientsKey(language)), map.get(ApiFields.Keys.lcIngredientsKey("en")));
    }

    @Nullable
    public String getImageFront() {
        return getProductDetailsMap().get(ApiFields.Keys.IMAGE_FRONT);
    }

    @Nullable
    public String getImageIngredients() {
        return getProductDetailsMap().get(ApiFields.Keys.IMAGE_INGREDIENTS);
    }

    @Nullable
    public String getImageNutrition() {
        return getProductDetailsMap().get(ApiFields.Keys.IMAGE_NUTRITION);
    }

    @Nullable
    public String getImageFrontLocalUrl() {
        String localUrl = getProductDetailsMap().get(ApiFields.Keys.IMAGE_FRONT);
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
}
