package openfoodfacts.github.scrachx.openfood.models.entities;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.firstNotEmpty;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.network.ApiFields;
import openfoodfacts.github.scrachx.openfood.utils.FileUtilsKt;

@Entity
public class OfflineSavedProduct implements Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("NotNullFieldNotInitialized")
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
    }

    @Keep
    public OfflineSavedProduct() {
        this.productDetails = new HashMap<>();
    }

    @NonNull
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(@NonNull String barcode) {
        this.barcode = barcode;
    }

    @Nullable
    public String getLanguage() {
        return productDetails.get(ApiFields.Keys.LANG);
    }

    @Nullable
    public String getName() {
        final String language = firstNotEmpty(productDetails.get(ApiFields.Keys.LANG), "en");
        return firstNotEmpty(
            productDetails.get(ApiFields.Keys.lcProductNameKey(language)),
            productDetails.get(ApiFields.Keys.lcProductNameKey("en"))
        );
    }

    @Nullable
    public String getIngredients() {
        final String language = firstNotEmpty(productDetails.get(ApiFields.Keys.LANG), "en");
        return firstNotEmpty(
            productDetails.get(ApiFields.Keys.lcIngredientsKey(language)),
            productDetails.get(ApiFields.Keys.lcIngredientsKey("en"))
        );
    }

    @Nullable
    public String getImageFront() {
        return productDetails.get(ApiFields.Keys.IMAGE_FRONT);
    }

    @Nullable
    public String getImageIngredients() {
        return productDetails.get(ApiFields.Keys.IMAGE_INGREDIENTS);
    }

    @Nullable
    public String getImageNutrition() {
        return productDetails.get(ApiFields.Keys.IMAGE_NUTRITION);
    }

    @Nullable
    public String getImageFrontLocalUrl() {
        String localUrl = productDetails.get(ApiFields.Keys.IMAGE_FRONT);
        if (!TextUtils.isEmpty(localUrl)) {
            return FileUtilsKt.LOCALE_FILE_SCHEME + localUrl;
        }
        return null;
    }

    @NonNull
    public Map<String, String> getProductDetails() {
        return this.productDetails;
    }

    public void setProductDetails(@NonNull Map<String, String> productDetails) {
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

    @NonNull
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append(id)
            .append(barcode)
            .append(isDataUploaded)
            .append(productDetails.toString())
            .toString();
    }
}
