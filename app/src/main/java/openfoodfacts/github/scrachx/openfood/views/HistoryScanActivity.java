package openfoodfacts.github.scrachx.openfood.views;


import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.opencsv.CSVWriter;

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
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.HistoryListAdapter;

public class HistoryScanActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.listHistoryScan)
    RecyclerView recyclerHistoryScanView;
    private List<HistoryItem> productItems;
    private boolean emptyHistory;
    private HistoryProductDao mHistoryProductDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_scan);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHistoryProductDao = Utils.getAppDaoSession(this).getHistoryProductDao();
        productItems = new ArrayList<>();
        new HistoryScanActivity.FillAdapter(this).execute(this);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerHistoryScanView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerHistoryScanView.addItemDecoration(dividerItemDecoration);
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
                        .onPositive((dialog, which) -> {
                            mHistoryProductDao.deleteAll();
                            productItems.clear();
                            recyclerHistoryScanView.getAdapter().notifyDataSetChanged();
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
        String baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        Log.d("dir", baseDir);
        String fileName = "exportHistoryOFF" + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()) + ".csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        CSVWriter writer;
        FileWriter fileWriter;
        try {
            if (f.exists() && !f.isDirectory()) {
                fileWriter = new FileWriter(filePath, true);
                writer = new CSVWriter(fileWriter);
            } else {
                writer = new CSVWriter(new FileWriter(filePath));
            }
            String[] headers = {"Barcode", "Name", "Brands"};
            writer.writeNext(headers);
            List<HistoryProduct> listHistoryProducts = mHistoryProductDao.loadAll();
            for (HistoryProduct hp : listHistoryProducts) {
                String[] line = {hp.getBarcode(), hp.getTitle(), hp.getBrands()};
                writer.writeNext(line);
            }
            writer.close();
            addNotification(this,f);
            Toast.makeText(this, R.string.txt_history_exported, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void addNotification(Context context,File file){

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this,"notification_channel")
                        .addAction(R.drawable.ic_share_black_24dp, getString(R.string.share_notification), shareFileIntent(context,file))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.download_complete))
                        .setContentText(file.toString());

        Intent notificationIntent = openFile(file);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
    public PendingIntent shareFileIntent(Context context,File file){
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        File fileWithinMyDir = new File(String.valueOf(file));
        PendingIntent pendingIntent = null;

        if(fileWithinMyDir.exists()) {
            intentShareFile.setType("text/csv");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file));

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.sharing_file));
            intentShareFile.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharing_file));

            pendingIntent = PendingIntent.getActivity(context, 0, intentShareFile, PendingIntent.FLAG_CANCEL_CURRENT);

        }
        return pendingIntent;
    }

    public Intent openFile(File file) {

        Uri path = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent chooser = Intent.createChooser(intent, getString(R.string.open_file));
        intent.setDataAndType(path, "text/csv");
        return chooser;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);

        menu.findItem(R.id.action_export_all_history)
                .setVisible(!emptyHistory);

        menu.findItem(R.id.action_remove_all_history)
                .setVisible(!emptyHistory);

        return true;
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
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .show();
                }
                break;
            }
        }
    }

    public class FillAdapter extends AsyncTask<Context, Void, Context> {

        private Activity activity;

        public FillAdapter(Activity act) {
            activity = act;
        }

        @Override
        protected void onPreExecute() {
            List<HistoryProduct> listHistoryProducts = mHistoryProductDao.loadAll();
            if (listHistoryProducts.size() == 0) {
                Toast.makeText(getApplicationContext(), R.string.txtNoData, Toast.LENGTH_LONG).show();
                emptyHistory = true;
                invalidateOptionsMenu();
                cancel(true);
            } else {
                Toast.makeText(getApplicationContext(), R.string.txtLoading, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Context doInBackground(Context... ctx) {
            List<HistoryProduct> listHistoryProducts = mHistoryProductDao.queryBuilder().orderDesc(HistoryProductDao.Properties.LastSeen).list();

            for (HistoryProduct historyProduct : listHistoryProducts) {
                Bitmap imgUrl;
                HttpURLConnection connection = null;
                InputStream input = null;
                try {
                    URL url = new URL(historyProduct.getUrl());
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    input = connection.getInputStream();
                    imgUrl = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 200, 200, true);
                } catch (IOException e) {
                    Log.i("HISTORY", "unable to get the history product image", e);
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_no_red_24dp);
                    Canvas canvas = new Canvas();
                    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                    canvas.setBitmap(bitmap);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    drawable.draw(canvas);
                    imgUrl = bitmap;
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            // no job
                        }
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                }

                productItems.add(new HistoryItem(historyProduct.getTitle(), historyProduct.getBrands(), imgUrl, historyProduct.getBarcode()));
            }

            return ctx[0];
        }

        @Override
        protected void onPostExecute(Context ctx) {
            HistoryListAdapter adapter = new HistoryListAdapter(productItems, getString(R.string.website_product), activity);
            recyclerHistoryScanView.setAdapter(adapter);
            recyclerHistoryScanView.setLayoutManager(new LinearLayoutManager(ctx));
        }
    }

}
