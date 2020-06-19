package openfoodfacts.github.scrachx.openfood.utils;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import openfoodfacts.github.scrachx.openfood.models.State;

public class FragmentUtils {
    private FragmentUtils() {

    }

    @Nullable
    public static State getStateFromArguments(Fragment fragment) {
        final Bundle args = fragment.getArguments();
        if (args == null) {
            return null;
        }
        return (State) args.getSerializable("state");
    }

    @NonNull
    public static State requireStateFromArguments(Fragment fragment) {
        final State state = getStateFromArguments(fragment);
        if (state == null) {
            throw new IllegalStateException("cannot start fragment without product state (not passed as argument)");
        }
        return state;
    }
}
