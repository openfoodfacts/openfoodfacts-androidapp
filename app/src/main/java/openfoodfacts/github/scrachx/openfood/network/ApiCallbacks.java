package openfoodfacts.github.scrachx.openfood.network;

import openfoodfacts.github.scrachx.openfood.models.Search;
import openfoodfacts.github.scrachx.openfood.models.State;

public class ApiCallbacks {
    public interface OnProductsCallback {
        void onProductsResponse(boolean isOk, Search searchResponse, int countProducts);
    }

    public interface OnAllergensCallback {
        void onAllergensResponse(boolean value, Search allergen);
    }

    public interface OnStoreCallback {
        void onStoreResponse(boolean value, Search store);
    }

    public interface OnPackagingCallback {
        void onPackagingResponse(boolean value, Search packaging);
    }

    public interface OnAdditiveCallback {
        void onAdditiveResponse(boolean value, Search brand);
    }

    public interface OnProductSentCallback {
        void onProductSentResponse(boolean value);
    }

    public interface OnStateListenerCallback {
        void onStateResponse(State newState);
    }

    public interface OnEditImageCallback {
        void onEditResponse(boolean value, String response);
    }

    public interface OnImagesCallback {
        void onImageResponse(boolean value, String response);
    }

    public interface OnCountryCallback {
        void onCountryResponse(boolean value, Search country);
    }

    public interface OnLabelCallback {
        void onLabelResponse(boolean value, Search label);
    }

    public interface OnCategoryCallback {
        void onCategoryResponse(boolean value, Search category);
    }

    public interface OnContributorCallback {
        void onContributorResponse(boolean value, Search contributor);
    }
}
