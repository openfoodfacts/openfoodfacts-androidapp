package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.RequestParams;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodUserClientUsage;
import openfoodfacts.github.scrachx.openfood.models.SaveItem;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.SaveListAdapter;

/**
 * Created by scotscriven on 09/05/15.
 */
public class OfflineEditFragment extends Fragment {

    private ArrayList<SaveItem> saveItems;
    private SaveListAdapter adapter;
    private ListView listView;
    private Button buttonSend;
    private String loginS;
    private String passS;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_offline_edit, container, false);

        listView = (ListView) rootView.findViewById(R.id.listOfflineSave);
        buttonSend = (Button) rootView.findViewById(R.id.buttonSendAll);
        final SharedPreferences settings = rootView.getContext().getSharedPreferences("login", 0);
        saveItems = new ArrayList<SaveItem>();

        loginS = settings.getString("user", "");
        passS = settings.getString("pass", "");

        if(loginS.isEmpty() || passS.isEmpty()){
            Toast.makeText(rootView.getContext(), rootView.getContext().getString(R.string.txtInfoAddUser), Toast.LENGTH_LONG).show();
            buttonSend.setEnabled(false);
        }

        new FillAdapter().execute(rootView.getContext());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SaveItem si = (SaveItem) parent.getItemAtPosition(position);
                String barcode = si.getBarcode();
                Intent intent = new Intent(getActivity(), SaveProductOfflineActivity.class);
                intent.putExtra("barcode", barcode);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int lapos = position;
                final AdapterView<?> laparent = parent;
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.txtDialogsTitle)
                        .content(R.string.txtDialogsContentDelete)
                        .positiveText(R.string.txtYes)
                        .negativeText(R.string.txtNo)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                String barcode = saveItems.get(lapos).getBarcode();
                                SendProduct.deleteAll(SendProduct.class, "barcode = ?", barcode);
                                SaveListAdapter sl = (SaveListAdapter) laparent.getAdapter();
                                saveItems.remove(lapos);
                                sl.notifyDataSetChanged();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                return;
                            }
                        })
                        .show();

                return true;
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity())
                    .title(R.string.txtDialogsTitle)
                    .content(R.string.txtDialogsContentSend)
                    .positiveText(R.string.txtYes)
                    .negativeText(R.string.txtNo)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            List<SendProduct> listSaveProduct = SendProduct.listAll(SendProduct.class);
                            FoodUserClientUsage user = new FoodUserClientUsage();
                            for (int i = 0; i < listSaveProduct.size(); i++) {
                                SendProduct sp = listSaveProduct.get(i);
                                if (sp.getBarcode().isEmpty() || sp.getEnergy().isEmpty() || sp.getImgupload_front().isEmpty()
                                        || sp.getStores().isEmpty() || sp.getWeight().isEmpty() || sp.getName().isEmpty()) {
                                    // Do nothing
                                } else {
                                    RequestParams params = new RequestParams();
                                    params.put("code", sp.getBarcode());
                                    params.put("user_id", loginS);
                                    params.put("password", passS);
                                    params.put("product_name", sp.getName());
                                    params.put("quantity", sp.getWeight());
                                    params.put("stores", sp.getStores());
                                    params.put("nutriment_energy", sp.getEnergy());
                                    params.put("nutriment_energy_unit", sp.getEnergy_unit());
                                    params.put("nutrition_data_per", "serving");
                                    user.post(getActivity(), params, sp.getImgupload_front(), sp.getBarcode());
                                }
                            }
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            return;
                        }
                    })
                    .show();
            }
        });


        return rootView;
    }

    private class FillAdapter extends AsyncTask<Context, Void, Context> {

        LoadToast lt = new LoadToast(getActivity());

        @Override
        protected void onPreExecute() {
            lt.setText(getResources().getString(R.string.txtLoading));
            lt.show();
        }

        @Override
        protected Context doInBackground(Context... ctx) {
            List<SendProduct> listSaveProduct = SendProduct.listAll(SendProduct.class);

            int imageIcon = R.drawable.ic_ok;
            for(int i = 0; i < listSaveProduct.size(); i++){
                SendProduct sp = listSaveProduct.get(i);
                if(sp.getBarcode().isEmpty() || sp.getEnergy().isEmpty() || sp.getImgupload_front().isEmpty()
                        || sp.getStores().isEmpty() || sp.getWeight().isEmpty() || sp.getName().isEmpty()){
                    imageIcon = R.drawable.ic_no;
                }
                Bitmap imgUrl = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(sp.getImgupload_front()), 200, 200, true);
                saveItems.add(new SaveItem(sp.getName(),imageIcon, imgUrl, sp.getBarcode()));
            }

            return ctx[0];
        }

        @Override
        protected void onPostExecute(Context ctx) {
            List<SendProduct> listSaveProduct = SendProduct.listAll(SendProduct.class);
            if(listSaveProduct.size() > 0){
                lt.success();
                adapter = new SaveListAdapter(ctx,saveItems);
                listView.setAdapter(adapter);
                buttonSend.setEnabled(true);
                for(int i = 0; i < listSaveProduct.size(); i++){
                    SendProduct sp = listSaveProduct.get(i);
                    if(sp.getBarcode().isEmpty() || sp.getEnergy().isEmpty() || sp.getImgupload_front().isEmpty()
                            || sp.getStores().isEmpty() || sp.getWeight().isEmpty() || sp.getName().isEmpty()){
                        buttonSend.setEnabled(false);
                    }

                }
            }else{
                lt.error();
            }

        }

    }

}
