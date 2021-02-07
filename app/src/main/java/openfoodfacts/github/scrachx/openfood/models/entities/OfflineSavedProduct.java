package openfoodfacts.github.scrachx.openfood.models.entities;

import android.text.TextUtils;
<<<<<<< HEAD

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.greenrobot.greendao.annotation.Convert;
=======
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

<<<<<<< HEAD
import java.io.Serializable;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.utils.FileUtilsKt;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.firstNotEmpty;

@Entity
public class OfflineSavedProduct implements Serializable {
    private static final long serialVersionUID = 1L;
    @Index(unique = true)
    @NonNull
    private String barcode;
    @Id
    private Long id;
    @Index
    private boolean isDataUploaded;
    @NonNull
    @Convert(converter = MapOfStringsToStringConverter.class, columnType = String.class)
    private Map<String, String> productDetails;

    public OfflineSavedProduct(@NonNull String barcode,
                               @NonNull Map<String, String> productDetails) {
        this.barcode = barcode;
        this.productDetails = productDetails;
        isDataUploaded = false;
    }

    @Generated(hash = 1699410715)
    public OfflineSavedProduct(@NonNull String barcode, Long id, boolean isDataUploaded,
                               @NonNull Map<String, String> productDetails) {
        this.barcode = barcode;
        this.id = id;
        this.isDataUploaded = isDataUploaded;
        this.productDetails = productDetails;
=======
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
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    @Generated(hash = 403273060)
    public OfflineSavedProduct() {
    }

<<<<<<< HEAD
    @NonNull
=======
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    public String getBarcode() {
        return barcode;
    }

<<<<<<< HEAD
    public void setBarcode(@NonNull String barcode) {
        this.barcode = barcode;
    }

    @Nullable
    public String getLanguage() {
        return productDetails.get(ApiFields.Keys.LANG);
=======
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
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    @Nullable
    public String getName() {
<<<<<<< HEAD
        final Map<String, String> map = productDetails;
        final String language = firstNotEmpty(map.get(ApiFields.Keys.LANG), "en");
        return firstNotEmpty(map.get(ApiFields.Keys.lcProductNameKey(language)), map.get(ApiFields.Keys.lcProductNameKey("en")));
=======
        HashMap<String, String> map = getProductDetailsMap();
        String language = Utils.firstNotEmpty(map.get(ApiFields.Keys.LANG), "en");
        return Utils.firstNotEmpty(map.get(ApiFields.Keys.lcProductNameKey(language)), map.get(ApiFields.Keys.lcProductNameKey("en")));
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    @Nullable
    public String getIngredients() {
<<<<<<< HEAD
        final Map<String, String> map = productDetails;
        final String language = firstNotEmpty(map.get(ApiFields.Keys.LANG), "en");
        return firstNotEmpty(map.get(ApiFields.Keys.lcIngredientsKey(language)), map.get(ApiFields.Keys.lcIngredientsKey("en")));
=======
        HashMap<String, String> map = getProductDetailsMap();
        String language = Utils.firstNotEmpty(map.get(ApiFields.Keys.LANG), "en");
        return Utils.firstNotEmpty(map.get(ApiFields.Keys.lcIngredientsKey(language)), map.get(ApiFields.Keys.lcIngredientsKey("en")));
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    @Nullable
    public String getImageFront() {
<<<<<<< HEAD
        return productDetails.get(ApiFields.Keys.IMAGE_FRONT);
=======
        return getProductDetailsMap().get(ApiFields.Keys.IMAGE_FRONT);
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    @Nullable
    public String getImageIngredients() {
<<<<<<< HEAD
        return productDetails.get(ApiFields.Keys.IMAGE_INGREDIENTS);
=======
        return getProductDetailsMap().get(ApiFields.Keys.IMAGE_INGREDIENTS);
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    @Nullable
    public String getImageNutrition() {
<<<<<<< HEAD
        return productDetails.get(ApiFields.Keys.IMAGE_NUTRITION);
=======
        return getProductDetailsMap().get(ApiFields.Keys.IMAGE_NUTRITION);
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }

    @Nullable
    public String getImageFrontLocalUrl() {
<<<<<<< HEAD
        String localUrl = productDetails.get(ApiFields.Keys.IMAGE_FRONT);
        if (!TextUtils.isEmpty(localUrl)) {
            return FileUtilsKt.LOCALE_FILE_SCHEME + localUrl;
=======
        String localUrl = getProductDetailsMap().get(ApiFields.Keys.IMAGE_FRONT);
        if (!TextUtils.isEmpty(localUrl)) {
            return FileUtils.LOCALE_FILE_SCHEME + localUrl;
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
        }
        return null;
    }

<<<<<<< HEAD
    @NonNull
    public Map<String, String> getProductDetails() {
        return this.productDetails;
    }

    public void setProductDetails(@NonNull Map<String, String> productDetails) {
=======
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
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
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

<<<<<<< HEAD
    @NonNull
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(id)
            .append(barcode)
            .append(isDataUploaded)
            .append(productDetails.toString())
            .toString();
=======
    @Override
    public String toString() {
        return "OfflineSavedProduct{" +
            "id=" + id +
            ", barcode='" + barcode + '\'' +
            ", isDataUploaded=" + isDataUploaded +
            ", map='" + getProductDetailsMap().toString() + '\'' +
            '}';
>>>>>>> b73375553e6727f1ebb0cf7dd743c16efdcd16ac
    }
}
