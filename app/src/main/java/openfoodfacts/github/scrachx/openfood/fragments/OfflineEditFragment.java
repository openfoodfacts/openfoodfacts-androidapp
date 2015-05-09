package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import openfoodfacts.github.scrachx.openfood.R;

/**
 * Created by scotscriven on 09/05/15.
 */
public class OfflineEditFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_offline_edit, container, false);
        return rootView;
    }

}
