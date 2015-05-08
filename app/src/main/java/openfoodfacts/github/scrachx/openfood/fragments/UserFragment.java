package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import openfoodfacts.github.scrachx.openfood.R;

/**
 * Created by scotscriven on 08/05/15.
 */
public class UserFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user,container,false);
        return rootView;
    }
}
