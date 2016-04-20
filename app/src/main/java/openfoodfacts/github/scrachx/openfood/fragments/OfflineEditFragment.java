package openfoodfacts.github.scrachx.openfood.fragments;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.FoodUserClientUsage;
import openfoodfacts.github.scrachx.openfood.models.SaveItem;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.SaveListAdapter;

public class OfflineEditFragment extends BaseFragment {

    private ArrayList<SaveItem> saveItems;

    @Bind(R.id.listOfflineSave) ListView listView;
    @Bind(R.id.buttonSendAll) Button buttonSend;
    private String loginS, passS;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_offline_edit);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SharedPreferences settings = getContext().getSharedPreferences("login", 0);
        saveItems = new ArrayList<>();

        loginS = settings.getString("user", "");
        passS = settings.getString("pass", "");

        buttonSend.setEnabled(false);


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
                                // void implementation
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
                                        params.put("quantity", sp.getWeight() + " " + sp.getWeight_unit());
                                        params.put("stores", sp.getStores());
                                        params.put("nutriment_energy", sp.getEnergy());
                                        params.put("nutriment_energy_unit", sp.getEnergy_unit());
                                        params.put("nutrition_data_per", "serving");

                                        File f = new File(sp.getImgupload_front());
                                        Bitmap bt = decodeFile(f);
                                        OutputStream fOut = null;
                                        File smallFile = new File(sp.getImgupload_front().replace(".png", "_small.png"));
                                        try {
                                            fOut = new FileOutputStream(smallFile);
                                            bt.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                            fOut.flush();
                                            fOut.close();
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        user.post(getActivity(), params, sp.getImgupload_front().replace(".png", "_small.png"), sp.getBarcode(), listView, i, saveItems);

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
    }

    @Override
    public void onResume() {
        super.onResume();
        new FillAdapter().execute(getActivity());
    }

    public class FillAdapter extends AsyncTask<Context, Void, Context> {

        @Override
        protected void onPreExecute() {
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
                if (sp.getBarcode().isEmpty() || sp.getEnergy().isEmpty() || sp.getImgupload_front().isEmpty()
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
                for (int i = 0; i < listSaveProduct.size(); i++) {
                    SendProduct sp = listSaveProduct.get(i);
                    if (sp.getBarcode().isEmpty() || sp.getEnergy().isEmpty() || sp.getImgupload_front().isEmpty()
                            || sp.getStores().isEmpty() || sp.getWeight().isEmpty() || sp.getName().isEmpty()) {
                        buttonSend.setEnabled(false);
                    }
                }
                if (loginS.isEmpty() || passS.isEmpty()) {
                    Toast.makeText(ctx, ctx.getString(R.string.txtInfoAddUser), Toast.LENGTH_LONG).show();
                    buttonSend.setEnabled(false);
                }
            } else {
                //Do nothing
            }
        }
    }

    // Decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 300;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
}
