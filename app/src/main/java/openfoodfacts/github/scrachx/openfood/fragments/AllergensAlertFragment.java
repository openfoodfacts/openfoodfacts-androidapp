package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.views.adapters.AllergensAdapter;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_ALERT;

/**
 * @see R.layout#fragment_alert_allergens
 */
public class AllergensAlertFragment extends NavigationBaseFragment {

    private List<AllergenName> mAllergensEnabled;
    private List<AllergenName> mAllergensNotEnabled;
    private List<AllergenName> mAllergensFromDao;
    private AllergensAdapter mAdapter;
    private RecyclerView mRvAllergens;
    private SharedPreferences mSettings;
    private IProductRepository productRepository;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return createView(inflater, container, R.layout.fragment_alert_allergens);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productRepository = ProductRepository.getInstance();
        mAllergensEnabled = productRepository.getAllergensByEnabledAndLanguageCode(true, Locale.getDefault().getLanguage());
        mAllergensFromDao = productRepository.getAllergensByLanguageCode(Locale.getDefault().getLanguage());

        mView = view;
        mSettings = getActivity().getSharedPreferences("prefs", 0);
        boolean firstRunAlert = mSettings.getBoolean("firstRunAlert", true);
        if (firstRunAlert) {
            new MaterialDialog.Builder(getContext())
                    .title(R.string.alert_dialog_warning_title)
                    .content(R.string.warning_alert_data)
                    .positiveText(R.string.ok_button)
                    .show();
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putBoolean("firstRunAlert", false);
            editor.apply();
        }

        mRvAllergens = (RecyclerView) view.findViewById(R.id.allergens_recycle);
        mAdapter = new AllergensAdapter(productRepository, mAllergensEnabled, getActivity());
        mRvAllergens.setAdapter(mAdapter);
        mRvAllergens.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRvAllergens.setHasFixedSize(true);
    }

    /**
     * Add an allergen to be checked for when browsing products.
     */
    @OnClick(R.id.fab)
    protected void onAddAllergens() {
        if (mAllergensFromDao != null && mAllergensFromDao.size() > 0) {

            mAllergensNotEnabled = productRepository.getAllergensByEnabledAndLanguageCode(false, Locale.getDefault().getLanguage());
            Collections.sort(mAllergensNotEnabled, new Comparator<AllergenName>() {
                @Override
                public int compare(AllergenName a1, AllergenName a2) {
                    return a1.getName().compareToIgnoreCase(a2.getName());
                }
            });
            List<String> allergensNames = new ArrayList<String>();
            for (AllergenName allergenName : mAllergensNotEnabled) {
                allergensNames.add(allergenName.getName());
            }
            new MaterialDialog.Builder(mView.getContext())
                    .title(R.string.title_dialog_alert)
                    .items(allergensNames)
                    .itemsCallback((dialog, view, position, text) -> {
                        productRepository.setAllergenEnabled(mAllergensNotEnabled.get(position).getAllergenTag(), true);
                        mAllergensEnabled.add(mAllergensNotEnabled.get(position));
                        mAdapter.notifyItemInserted(mAllergensEnabled.size() - 1);
                        mRvAllergens.scrollToPosition(mAdapter.getItemCount() - 1);
                    })
                    .show();
        } else

        {
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                final LoadToast lt = new LoadToast(getContext());
                lt.setText(getContext().getString(R.string.toast_retrieving));
                lt.setBackgroundColor(getContext().getResources().getColor(R.color.blue));
                lt.setTextColor(getContext().getResources().getColor(R.color.white));
                lt.show();
                new MaterialDialog.Builder(mView.getContext())
                        .title(R.string.title_dialog_alert)
                        .content(R.string.info_download_data)
                        .positiveText(R.string.txtYes)
                        .negativeText(R.string.txtNo)
                        .onPositive((dialog, which) -> {
                            final SharedPreferences.Editor editor = mSettings.edit();
                            ProductRepository.getInstance().getAllergens(true)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .toObservable()
                                    .subscribe(allergens -> {
                                        editor.putBoolean("errorAllergens", false).apply();
                                        lt.success();
                                    }, e -> {
                                        editor.putBoolean("errorAllergens", true).apply();
                                        lt.error();
                                    }, dialog::hide);
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                lt.hide();
                            }
                        })
                        .show();
            } else {
                new MaterialDialog.Builder(mView.getContext())
                        .title(R.string.title_dialog_alert)
                        .content(R.string.info_download_data_connection)
                        .neutralText(R.string.txtOk)
                        .show();
            }
        }

    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_ALERT;
    }


    public static Integer getKey(HashMap<Integer, String> map, String value) {
        Integer key = null;
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if ((value == null && entry.getValue() == null) || (value != null && value.equals(entry.getValue()))) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }

    public void onResume() {

        super.onResume();

        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.alert_drawer));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

}
