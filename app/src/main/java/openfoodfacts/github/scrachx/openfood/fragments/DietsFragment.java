package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.Diet;
import openfoodfacts.github.scrachx.openfood.models.DietDao;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.adapters.DietsAdapter;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_DIET;

/**
 * @see R.layout#fragment_diets
 * Created by dobriseb on 2018.10.15.
 * Diet gest in local database.
 */
public class DietsFragment extends NavigationBaseFragment {

    private RecyclerView mRvDiet;
    private SharedPreferences mSettings;

    @Override
    public int getNavigationDrawerType() {
        return ITEM_DIET;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return createView(inflater, container, R.layout.fragment_diets);
    }

    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Warning message, data stay on device.
        mSettings = getActivity().getSharedPreferences("prefs", 0);
        boolean firstRunDiets = mSettings.getBoolean("firstRunDiets", true);
        if (firstRunDiets) {
            new MaterialDialog.Builder(view.getContext())
                    .title(R.string.diets_dialog_warning_title)
                    .content(R.string.warning_diets_data)
                    .positiveText(R.string.ok_button)
                    .show();
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean("firstRunDiets", false);
            editor.apply();
        }

        //Looking for data to be load in the Recycler
        mRvDiet = (RecyclerView) view.findViewById(R.id.diets_recycler);
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        DietDao dietDao = daoSession.getDietDao();
        List<Diet> dietList = dietDao.loadAll();

        //Activate the recycler with the good adapter
        mRvDiet.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRvDiet.setAdapter(new DietsAdapter(dietList, new ClickListener() {
            @Override
            public void onPositionClicked(int position, View v) {
                Fragment fragment = new EditDietFragment();
                if (v.getTag() != null) {
                    Bundle args = new Bundle();
                    args.putString("dietName", v.getTag().toString());
                    fragment.setArguments(args);
                }
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment );
                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                transaction.commit();
            }
        }));
    }

    /**
     * Add a diet.
     */
    @OnClick(R.id.fab)
    void openFragmentAddDiet () {
        Fragment fragment = new EditDietFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment );
        transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
        transaction.commit();
    }

    public void onResume() {
        super.onResume();
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.your_diets));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public interface ClickListener {
        void onPositionClicked(int position, View v);
    }
}
