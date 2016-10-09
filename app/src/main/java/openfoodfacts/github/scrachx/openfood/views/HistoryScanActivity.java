package openfoodfacts.github.scrachx.openfood.views;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.opencsv.CSVWriter;
import com.orm.query.Select;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.HistoryItem;
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.HistoryListAdapter;

public class HistoryScanActivity extends BaseActivity {

    private ShareActionProvider mShareActionProvider;
    private List<HistoryItem> productItems;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.listHistoryScan)
    RecyclerView recyclerHistoryScanView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_scan);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productItems = new ArrayList<>();
        new HistoryScanActivity.FillAdapter().execute(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_remove_all_history:
                new MaterialDialog.Builder(this)
                        .title(R.string.title_clear_history_dialog)
                        .content(R.string.text_clear_history_dialog)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                HistoryProduct.deleteAll(HistoryProduct.class);
                                productItems.clear();
                                recyclerHistoryScanView.getAdapter().notifyDataSetChanged();
                            }
                        })
                        .positiveText(R.string.txtYes)
                        .negativeText(R.string.txtNo)
                        .show();
                return true;
            case R.id.action_export_all_history:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        new MaterialDialog.Builder(this)
                                .title(R.string.action_about)
                                .content(R.string.permision_write_external_storage)
                                .neutralText(R.string.txtOk)
                                .show();
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
                    }
                } else {
                    exportCSV();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void exportCSV() {
        Toast.makeText(this, R.string.txt_exporting_history, Toast.LENGTH_LONG).show();
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d("dir", baseDir);
        String fileName = "exportHistoryOFF"+new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date())+".csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath );
        CSVWriter writer;
        FileWriter fileWriter;
        try {
            if(f.exists() && !f.isDirectory()) {
                fileWriter = new FileWriter(filePath , true);
                writer = new CSVWriter(fileWriter);
            } else {
                writer = new CSVWriter(new FileWriter(filePath));
            }
            String[] headers = {"Barcode", "Name", "Brands"};
            writer.writeNext(headers);
            List<HistoryProduct> listHistoryProducts = HistoryProduct.listAll(HistoryProduct.class);
            for (HistoryProduct hp : listHistoryProducts) {
                String[] line = {hp.getBarcode(), hp.getTitle(), hp.getBrands()};
                writer.writeNext(line);
            }
            writer.close();
            Toast.makeText(this, R.string.txt_history_exported, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public class FillAdapter extends AsyncTask<Context, Void, Context> {

        @Override
        protected void onPreExecute() {
            List<HistoryProduct> listHistoryProducts = HistoryProduct.listAll(HistoryProduct.class);
            if (listHistoryProducts.size() == 0) {
                Toast.makeText(getApplicationContext(), R.string.txtNoData, Toast.LENGTH_LONG).show();
                cancel(true);
            } else {
                Toast.makeText(getApplicationContext(), R.string.txtLoading, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Context doInBackground(Context... ctx) {
            List<HistoryProduct> listHistoryProducts = Select.from(HistoryProduct.class).orderBy("LAST_SEEN DESC").list();
            for (int i = 0; i < listHistoryProducts.size(); i++) {
                HistoryProduct hp = listHistoryProducts.get(i);
                Bitmap imgUrl = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_no), 200, 200, true);
                try {
                    URL url = new URL(hp.getUrl());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    imgUrl = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 200, 200, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                productItems.add(new HistoryItem(hp.getTitle(), hp.getBrands(), imgUrl, hp.getBarcode()));
            }

            return ctx[0];
        }

        @Override
        protected void onPostExecute(Context ctx) {
            HistoryListAdapter adapter = new HistoryListAdapter(productItems, getApplication());
            recyclerHistoryScanView.setAdapter(adapter);
            recyclerHistoryScanView.setLayoutManager(new LinearLayoutManager(ctx));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utils.MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportCSV();
                } else {
                    new MaterialDialog.Builder(this)
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
                break;
            }
        }
    }

}
