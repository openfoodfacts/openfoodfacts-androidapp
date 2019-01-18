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
import android.widget.LinearLayout;

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
    private List<AllergenName> mAllergensFromDao;
    private AllergensAdapter mAdapter;
    private RecyclerView mRvAllergens;
    private SharedPreferences mSettings;
    private IProductRepository productRepository;
    private View mView;
    private LinearLayout mEmptyMessageView;                                         // Empty View containing the message that will be shown if the list is empty
    private DataObserver mDataObserver;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return createView(inflater, container, R.layout.fragment_alert_allergens);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRvAllergens = (RecyclerView) view.findViewById(R.id.allergens_recycle);
        mEmptyMessageView = (LinearLayout) view.findViewById(R.id.emptyAllergensView);
        productRepository = ProductRepository.getInstance();
        mDataObserver = new DataObserver();
        productRepository.getAllergensByEnabledAndLanguageCode(true, Locale.getDefault().getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(allergens -> {
                    mAllergensEnabled = allergens;
                    mAdapter = new AllergensAdapter(productRepository, mAllergensEnabled, getActivity());
                    mRvAllergens.setAdapter(mAdapter);
                    mRvAllergens.setLayoutManager(new LinearLayoutManager(view.getContext()));
                    mRvAllergens.setHasFixedSize(true);
                    mAdapter.registerAdapterDataObserver(mDataObserver);
                    mDataObserver.onChanged();
                }, Throwable::printStackTrace);

        productRepository.getAllergensByLanguageCode(Locale.getDefault().getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(allergens -> {
                    mAllergensFromDao = allergens;
                }, Throwable::printStackTrace);


        mView = view;
        mSettings = getActivity().getSharedPreferences("prefs", 0);
    }

    /**
     * Add an allergen to be checked for when browsing products.
     */
    @OnClick(R.id.fab)
    protected void onAddAllergens() {
        if (mAllergensEnabled != null && mAllergensFromDao != null && mAllergensFromDao.size() > 0) {
            productRepository.getAllergensByEnabledAndLanguageCode(false, Locale.getDefault().getLanguage())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((List<AllergenName> allergens) -> {
                        Collections.sort(allergens, new Comparator<AllergenName>() {
                            @Override
                            public int compare(AllergenName a1, AllergenName a2) {
                                return a1.getName().compareToIgnoreCase(a2.getName());
                            }
                        });
                        List<String> allergensNames = new ArrayList<String>();
                        for (AllergenName allergenName : allergens) {
                            allergensNames.add(allergenName.getName());
                        }
                        new MaterialDialog.Builder(mView.getContext())
                                .title(R.string.title_dialog_alert)
                                .items(allergensNames)
                                .itemsCallback((dialog, view, position, text) -> {
                                    productRepository.setAllergenEnabled(allergens.get(position).getAllergenTag(), true);
                                    mAllergensEnabled.add(allergens.get(position));
                                    mAdapter.notifyItemInserted(mAllergensEnabled.size() - 1);
                                    mRvAllergens.scrollToPosition(mAdapter.getItemCount() - 1);
                                })
                                .show();
                    }, Throwable::printStackTrace);
        } else {
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                final LoadToast lt = new LoadToast(getContext());
                lt.setText(getContext().getString(R.string.toast_retrieving));
                lt.setBackgroundColor(getContext().getResources().getColor(R.color.blue));
                lt.setTextColor(getContext().getResources().getColor(R.color.white));
                lt.show();
                final SharedPreferences.Editor editor = mSettings.edit();
                productRepository.getAllergens(true)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .toObservable()
                        .subscribe(allergens -> {
                            editor.putBoolean("errorAllergens", false).apply();
                            productRepository.saveAllergens(allergens);
                            mAdapter.setAllergens(mAllergensEnabled);
                            mAdapter.notifyDataSetChanged();
                            updateAllergenDao();
                            onAddAllergens();
                            lt.success();
                        }, e -> {
                            editor.putBoolean("errorAllergens", true).apply();
                            lt.error();
                        });
            } else {
                new MaterialDialog.Builder(mView.getContext())
                        .title(R.string.title_dialog_alert)
                        .content(R.string.info_download_data_connection)
                        .neutralText(R.string.txtOk)
                        .show();
            }
        }
    }

    private void updateAllergenDao() {
        productRepository.getAllergensByEnabledAndLanguageCode(true, Locale.getDefault().getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(allergens -> {
                    mAllergensEnabled = allergens;
                }, Throwable::printStackTrace);

        productRepository.getAllergensByLanguageCode(Locale.getDefault().getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(allergens -> {
                    mAllergensFromDao = allergens;
                }, Throwable::printStackTrace);
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_ALERT;
    }

    public void onResume() {
        super.onResume();
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.alert_drawer));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mRvAllergens != null){
            mRvAllergens.getAdapter().unregisterAdapterDataObserver(mDataObserver);
        }
    }

    class DataObserver extends RecyclerView.AdapterDataObserver{
        DataObserver() {
            super();
        }

        private void setAppropriateView() {
            if (mEmptyMessageView != null && mAdapter != null) {
                boolean isListEmpty = mAdapter.getItemCount() == 0;
                mEmptyMessageView.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
                mRvAllergens.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
            }
        }
        @Override
        public void onChanged() {
            super.onChanged();
            setAppropriateView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            setAppropriateView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            setAppropriateView();
        }
    }
}