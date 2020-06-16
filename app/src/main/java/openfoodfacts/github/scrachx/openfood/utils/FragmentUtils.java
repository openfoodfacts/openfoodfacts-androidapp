package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import openfoodfacts.github.scrachx.openfood.models.State;

public class FragmentUtils {
    private FragmentUtils() {

    }

    @Nullable
    public static State getStateFromActivityIntent(Fragment fragment) {
        if (fragment.getActivity() != null) {
            Intent intent = fragment.getActivity().getIntent();
            if (intent != null && intent.getExtras() != null && intent.getExtras().getSerializable("state") != null) {
                return (State) intent.getExtras().getSerializable("state");
            }
        }
        return null;
    }

    @NonNull
    public static State requireStateFromActivityIntent(Fragment fragment) {
        final State state = getStateFromActivityIntent(fragment);
        if (state == null) {
            throw new IllegalStateException("cannot start fragment without product state (not passed as parameter)");
        } else {
            return state;
        }
    }
}
