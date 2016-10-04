package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodUserClientUsage;
import openfoodfacts.github.scrachx.openfood.models.SaveItem;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.SaveListAdapter;

public class OfflineEditFragment extends BaseFragment {

    private ArrayList<SaveItem> saveItems;

    @BindView(R.id.listOfflineSave) ListView listView;
    @BindView(R.id.buttonSendAll) Button buttonSend;
    private String loginS, passS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_offline_edit);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SharedPreferences settingsLogin = getContext().getSharedPreferences("login", 0);
        final SharedPreferences settingsUsage = getContext().getSharedPreferences("usage", 0);
        saveItems = new ArrayList<>();
        loginS = settingsLogin.getString("user", "");
        passS = settingsLogin.getString("pass", "");
        boolean firstUse = settingsUsage.getBoolean("firstOffline", false);
        if(!firstUse) {
            new MaterialDialog.Builder(getContext())
                    .title(R.string.title_info_dialog)
                    .content(R.string.text_offline_info_dialog)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            SharedPreferences.Editor editor = settingsUsage.edit();
                            editor.putBoolean("firstOffline", true);
                            editor.apply();
                        }
                    })
                    .positiveText(R.string.txtOk)
                    .show();
        }
        buttonSend.setEnabled(false);
    }

    @OnItemClick(R.id.listOfflineSave)
    protected void OnClickListOffline(int position) {
        SaveItem si = (SaveItem) listView.getItemAtPosition(position);
        SharedPreferences settings = getActivity().getSharedPreferences("temp", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("barcode", si.getBarcode());
        editor.apply();
        Intent intent = new Intent(getActivity(), SaveProductOfflineActivity.class);
        startActivity(intent);
    }

    @OnItemLongClick(R.id.listOfflineSave)
    protected boolean OnLongClickListOffline(int position) {
        final int lapos = position;
        new MaterialDialog.Builder(getActivity())
                .title(R.string.txtDialogsTitle)
                .content(R.string.txtDialogsContentDelete)
                .positiveText(R.string.txtYes)
                .negativeText(R.string.txtNo)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String barcode = saveItems.get(lapos).getBarcode();
                        SendProduct.deleteAll(SendProduct.class, "barcode = ?", barcode);
                        final SaveListAdapter sl = (SaveListAdapter) listView.getAdapter();
                        saveItems.remove(lapos);
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                sl.notifyDataSetChanged();
                            }
                        });
                    }
                })
                .show();
        return true;
    }

    @OnClick(R.id.buttonSendAll)
    protected void onSendAllProducts() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.txtDialogsTitle)
                .content(R.string.txtDialogsContentSend)
                .positiveText(R.string.txtYes)
                .negativeText(R.string.txtNo)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        List<SendProduct> listSaveProduct = SendProduct.listAll(SendProduct.class);
                        FoodUserClientUsage user = new FoodUserClientUsage();
                        for (int i = 0; i < listSaveProduct.size(); i++) {
                            SendProduct sp = listSaveProduct.get(i);
                            if (sp.getBarcode().isEmpty() || sp.getImgupload_front().isEmpty()) {
                                continue;
                            }

                            RequestParams params = new RequestParams();
                            params.put("code", sp.getBarcode());
                            if(!loginS.isEmpty() && !passS.isEmpty()) {
                                params.put("user_id", loginS);
                                params.put("password", passS);
                            }
                            if(!sp.getName().isEmpty()) params.put("product_name", sp.getName());
                            if(!sp.getWeight().isEmpty()) {
                                if(sp.getWeight_unit().trim().isEmpty()) {
                                    params.put("quantity", sp.getWeight());
                                } else {
                                    params.put("quantity", sp.getWeight() + " " + sp.getWeight_unit());
                                }
                            }
                            if(!sp.getStores().isEmpty()) params.put("stores", sp.getStores());
                            params.put("comment", "added with the new Android app");

                            if(!sp.getImgupload_ingredients().isEmpty()) Utils.compressImage(sp.getImgupload_ingredients());
                            if(!sp.getImgupload_nutrition().isEmpty()) Utils.compressImage(sp.getImgupload_nutrition());
                            if(!sp.getImgupload_front().isEmpty()) Utils.compressImage(sp.getImgupload_front());

                            user.postSaved(getActivity(), params, sp.getImgupload_front().replace(".png", "_small.png"), sp.getImgupload_ingredients().replace(".png", "_small.png"), sp.getImgupload_nutrition().replace(".png", "_small.png"), sp.getBarcode(), listView, i, saveItems);
                        }
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        new FillAdapter().execute(getActivity());
    }

    public class FillAdapter extends AsyncTask<Context, Void, Context> {

        @Override
        protected void onPreExecute() {
            saveItems.clear();
            List<SendProduct> listSaveProduct = SendProduct.listAll(SendProduct.class);
            if (listSaveProduct.size() == 0) {
                Toast.makeText(getActivity(), R.string.txtNoData, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.txtLoading, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Context doInBackground(Context... ctx) {
            List<SendProduct> listSaveProduct = SendProduct.listAll(SendProduct.class);

            int imageIcon = R.drawable.ic_ok;
            for (int i = 0; i < listSaveProduct.size(); i++) {
                SendProduct sp = listSaveProduct.get(i);
                if (sp.getBarcode().isEmpty() || sp.getImgupload_front().isEmpty()
                        || sp.getStores().isEmpty() || sp.getWeight().isEmpty() || sp.getName().isEmpty()) {
                    imageIcon = R.drawable.ic_no;
                }
                Bitmap imgUrl = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(sp.getImgupload_front()), 200, 200, true);
                saveItems.add(new SaveItem(sp.getName(), imageIcon, imgUrl, sp.getBarcode()));
            }

            return ctx[0];
        }

        @Override
        protected void onPostExecute(Context ctx) {
            List<SendProduct> listSaveProduct = SendProduct.listAll(SendProduct.class);
            if (listSaveProduct.size() > 0) {
                SaveListAdapter adapter = new SaveListAdapter(ctx, saveItems);
                listView.setAdapter(adapter);
                buttonSend.setEnabled(true);
                for (SendProduct sp : listSaveProduct) {
                    if (sp.getBarcode().isEmpty() || sp.getImgupload_front().isEmpty()) {
                        buttonSend.setEnabled(false);
                    }
                }
            }
        }
    }
}
