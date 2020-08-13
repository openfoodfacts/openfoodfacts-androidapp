package openfoodfacts.github.scrachx.openfood.utils;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import openfoodfacts.github.scrachx.openfood.models.ProductState;

public class FragmentUtils {
    private FragmentUtils() {

    }

    @Nullable
    public static ProductState getStateFromArguments(@NonNull Fragment fragment) {
        final Bundle args = fragment.getArguments();
        if (args == null) {
            return null;
        }
        return (ProductState) args.getSerializable("state");
    }

    @NonNull
    public static ProductState requireStateFromArguments(Fragment fragment) {
        final ProductState productState = getStateFromArguments(fragment);
        if (productState == null) {
            throw new IllegalStateException("cannot start fragment without product state (not passed as argument)");
        }
        return productState;
    }
}
