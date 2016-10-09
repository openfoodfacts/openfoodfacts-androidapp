package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;

import net.steamcrafted.loadtoast.LoadToast;

import org.apache.commons.collections.IteratorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Allergen;
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openfoodfacts.github.scrachx.openfood.views.adapters.AllergensAdapter;

public class AlertUserFragment extends BaseFragment {

    private List<Allergen> mAllergens;
    private AllergensAdapter mAdapter;
    private RecyclerView mRvAllergens;
    private SharedPreferences mSettings;
    private View mView;
    @BindView(R.id.fab) FloatingActionButton mFab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_alert_allergens);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        mRvAllergens = (RecyclerView) view.findViewById(R.id.alergens_recycle);
        mAllergens = Allergen.find(Allergen.class, "enable = ?", "true");
        mAdapter = new AllergensAdapter(mAllergens);
        mRvAllergens.setAdapter(mAdapter);
        mRvAllergens.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRvAllergens.setHasFixedSize(true);
    }

    @OnClick(R.id.fab)
    protected void onAddAllergens() {
        final List<Allergen> all = IteratorUtils.toList(Allergen.findAll(Allergen.class));
        final LinkedHashMap<Integer,String> allS = new LinkedHashMap<>();
        int index = 0;
        for (Allergen a : all) {
            if (Locale.getDefault().getLanguage().contains("fr")){
                if(a.getIdAllergen().contains("fr:")) allS.put(index, a.getName().substring(a.getName().indexOf(":")+1));
            } else if (Locale.getDefault().getLanguage().contains("en")) {
                if(a.getIdAllergen().contains("en:")) allS.put(index, a.getName().substring(a.getName().indexOf(":")+1));
            }
            index++;
        }
        if(allS.size() > 0) {
            new MaterialDialog.Builder(mView.getContext())
                    .title(R.string.title_dialog_alert)
                    .items(allS.values())
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            boolean canAdd = true;
                            int index = -1;
                            String alergeneStringByPos = new ArrayList<String>(allS.values()).get(which);
                            for(Allergen a : mAllergens) {
                                if(a.getName().equals(all.get(which).getName())) canAdd = false;
                            }
                            for(Allergen a : all) {
                                if(a.getName().substring(a.getName().indexOf(":")+1).equalsIgnoreCase(alergeneStringByPos)) {
                                    index = getKey(allS, alergeneStringByPos);
                                    all.get(index).setEnable("true");
                                    all.get(index).save();
                                }
                            }
                            if(canAdd && index != -1) {
                                mAllergens.add(all.get(index));
                                mAdapter.notifyItemInserted(mAllergens.size() - 1);
                                mRvAllergens.scrollToPosition(mAdapter.getItemCount() - 1);
                            }
                        }
                    })
                    .show();
        } else {
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if(isConnected) {
                final LoadToast lt = new LoadToast(getContext());
                lt.setText(getContext().getString(R.string.toast_retrieving));
                lt.setBackgroundColor(getContext().getResources().getColor(R.color.indigo_600));
                lt.setTextColor(getContext().getResources().getColor(R.color.white));
                lt.show();
                new MaterialDialog.Builder(mView.getContext())
                        .title(R.string.title_dialog_alert)
                        .content(R.string.info_download_data)
                        .positiveText(R.string.txtYes)
                        .negativeText(R.string.txtNo)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                                final SharedPreferences.Editor editor = mSettings.edit();
                                FoodAPIRestClientUsage api = new FoodAPIRestClientUsage(getString(R.string.openfoodUrl));
                                api.getAllergens(new FoodAPIRestClientUsage.OnAllergensCallback() {
                                    @Override
                                    public void onAllergensResponse(boolean value) {
                                        if (!value) {
                                            editor.putBoolean("errorAllergens", true);
                                            editor.apply();
                                        } else {
                                            editor.putBoolean("errorAllergens", false);
                                            editor.apply();
                                        }
                                        lt.success();
                                        dialog.hide();
                                    }
                                });
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

    public static Integer getKey(HashMap<Integer, String> map, String value) {
        Integer key = null;
        for(Map.Entry<Integer, String> entry : map.entrySet()) {
            if((value == null && entry.getValue() == null) || (value != null && value.equals(entry.getValue()))) {
                key = entry.getKey();
                break;
            }
        }
        return key;
    }

}
