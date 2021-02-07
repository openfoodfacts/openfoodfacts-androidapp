package openfoodfacts.github.scrachx.openfood.network;

import openfoodfacts.github.scrachx.openfood.models.Search;

public class ApiCallbacks {
    @FunctionalInterface
    public interface OnAllergensCallback {
        void onAllergensResponse(boolean value, Search allergen);
    }

    @FunctionalInterface
    public interface OnStoreCallback {
        void onStoreResponse(boolean value, Search store);
    }

    @FunctionalInterface
    public interface OnPackagingCallback {
        void onPackagingResponse(boolean value, Search packaging);
    }

    @FunctionalInterface
    public interface OnAdditiveCallback {
        void onAdditiveResponse(boolean value, Search brand);
    }

    @FunctionalInterface
    public interface OnEditImageCallback {
        void onEditResponse(boolean value, String response);
    }

    @FunctionalInterface
    public interface OnCountryCallback {
        void onCountryResponse(boolean value, Search country);
    }

    @FunctionalInterface
    public interface OnLabelCallback {
        void onLabelResponse(boolean value, Search label);
    }

    @FunctionalInterface
    public interface OnCategoryCallback {
        void onCategoryResponse(boolean value, Search category);
    }

    @FunctionalInterface
    public interface OnContributorCallback {
        void onContributorResponse(boolean value, Search contributor);
    }
}
