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
    public static ProductState requireStateFromArguments(@NonNull Fragment fragment) {
        final ProductState productState = getStateFromArguments(fragment);
        if (productState == null) {
            throw new IllegalStateException("Fragment" + fragment + "started without without product state (not passed as argument).");
        }
        return productState;
    }

    @NonNull
    public static <T extends Fragment> T applyBundle(@NonNull T fragment, @NonNull Bundle bundle) {
        fragment.setArguments(bundle);
        return fragment;
    }
}
