/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.features.allergensalert;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAlertAllergensBinding;
import openfoodfacts.github.scrachx.openfood.features.adapters.AllergensAdapter;
import openfoodfacts.github.scrachx.openfood.features.shared.NavigationBaseFragment;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName;
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;

import static openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage;
import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_ALERT;

/**
 * @see R.layout#fragment_alert_allergens
 */
public class AllergensAlertFragment extends NavigationBaseFragment {
    private FragmentAlertAllergensBinding binding;
    private List<AllergenName> mAllergensEnabled;
    private List<AllergenName> mAllergensFromDao;
    private AllergensAdapter mAdapter;
    private SharedPreferences mSettings;
    private ProductRepository productRepository;
    private CompositeDisposable dispCont = new CompositeDisposable();
    private View currentView;
    private DataObserver mDataObserver;

    public static <K, V> K getKey(@NonNull Map<K, V> map, V value) {
        K key = null;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if ((value == null && entry.getValue() == null) || (value != null && value.equals(entry.getValue()))) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }

    @NonNull
    public static AllergensAlertFragment newInstance() {

        Bundle args = new Bundle();

        AllergensAlertFragment fragment = new AllergensAlertFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        binding = FragmentAlertAllergensBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dispCont.dispose();
        binding.allergensRecycle.getAdapter().unregisterAdapterDataObserver(mDataObserver);
        binding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // OnClick
        binding.btnAdd.setOnClickListener(v -> onAddAllergens());

        productRepository = ProductRepository.getInstance();
        mDataObserver = new DataObserver();
        productRepository.getAllergensByEnabledAndLanguageCode(true, Locale.getDefault().getLanguage());

        final String language = getLanguage(requireActivity());
        dispCont.add(productRepository.getAllergensByEnabledAndLanguageCode(true, language)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                allergens -> {
                    mAllergensEnabled = allergens;
                    mAdapter = new AllergensAdapter(productRepository, mAllergensEnabled);
                    binding.allergensRecycle.setAdapter(mAdapter);
                    binding.allergensRecycle.setLayoutManager(new LinearLayoutManager(view.getContext()));
                    binding.allergensRecycle.setHasFixedSize(true);
                    mAdapter.registerAdapterDataObserver(mDataObserver);
                    mDataObserver.onChanged();
                },
                e -> Log.e(AllergensAlertFragment.class.getSimpleName(), "getAllergensByEnabledAndLanguageCode", e)
            ));

        dispCont.add(productRepository.getAllergensByLanguageCode(language)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                allergens -> mAllergensFromDao = allergens,
                e -> Log.e(AllergensAlertFragment.class.getSimpleName(), "getAllergensByLanguageCode", e)
            ));

        currentView = view;
        mSettings = requireActivity().getSharedPreferences("prefs", 0);
    }

    /**
     * Add an allergen to be checked for when browsing products.
     */
    private void onAddAllergens() {
        if (mAllergensEnabled != null && mAllergensFromDao != null && !mAllergensFromDao.isEmpty()) {
            dispCont.add(productRepository.getAllergensByEnabledAndLanguageCode(false, getLanguage(requireActivity()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((List<AllergenName> allergens) -> {
                    Collections.sort(allergens, (a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
                    List<String> allergensNames = new ArrayList<>();
                    for (AllergenName allergenName : allergens) {
                        allergensNames.add(allergenName.getName());
                    }
                    new MaterialDialog.Builder(currentView.getContext())
                        .title(R.string.title_dialog_alert)
                        .items(allergensNames)
                        .itemsCallback((dialog, view, position, text) -> {
                            productRepository.setAllergenEnabled(allergens.get(position).getAllergenTag(), true);
                            mAllergensEnabled.add(allergens.get(position));
                            mAdapter.notifyItemInserted(mAllergensEnabled.size() - 1);
                            binding.allergensRecycle.scrollToPosition(mAdapter.getItemCount() - 1);
                        })
                        .show();
                }, Throwable::printStackTrace));
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
                dispCont.add(productRepository.getAllergens()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toObservable()
                    .subscribe(allergens -> {
                        editor.putBoolean("errorAllergens", false).apply();
                        mAdapter.setAllergens(mAllergensEnabled);
                        mAdapter.notifyDataSetChanged();
                        updateAllergenDao();
                        onAddAllergens();
                        lt.success();
                    }, e -> {
                        editor.putBoolean("errorAllergens", true).apply();
                        lt.error();
                    }));
            } else {
                new MaterialDialog.Builder(currentView.getContext())
                    .title(R.string.title_dialog_alert)
                    .content(R.string.info_download_data_connection)
                    .neutralText(R.string.txtOk)
                    .show();
            }
        }
    }

    /**
     * Retrieve modified list of allergens from ProductRepository
     */
    private void updateAllergenDao() {
        final String language = getLanguage(getContext());
        dispCont.add(productRepository.getAllergensByEnabledAndLanguageCode(true, language)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(allergens -> mAllergensEnabled = allergens, e -> Log.e(AllergensAlertFragment.class.getSimpleName(), "getAllergensByEnabledAndLanguageCode", e)));

        dispCont.add(productRepository.getAllergensByLanguageCode(language)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(allergens -> mAllergensFromDao = allergens, e -> Log.e(AllergensAlertFragment.class.getSimpleName(), "getAllergensByLanguageCode", e)));
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_ALERT;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(getString(R.string.alert_drawer));
        } catch (IllegalStateException e) {
            Log.e(AllergensAlertFragment.class.getSimpleName(), "onResume", e);
        }
    }

    /**
     * Data observer of the Recycler Views
     */
    class DataObserver extends RecyclerView.AdapterDataObserver {
        DataObserver() {
            super();
        }

        private void setAppropriateView() {
            if (mAdapter != null) {
                boolean isListEmpty = mAdapter.getItemCount() == 0;
                binding.emptyAllergensView.setVisibility(isListEmpty ? View.VISIBLE : View.GONE);
                binding.allergensRecycle.setVisibility(isListEmpty ? View.GONE : View.VISIBLE);
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
