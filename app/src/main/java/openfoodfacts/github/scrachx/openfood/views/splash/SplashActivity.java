package openfoodfacts.github.scrachx.openfood.views.splash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.opencsv.CSVReader;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import butterknife.BindView;
import okhttp3.ResponseBody;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.OfflineProduct;
import openfoodfacts.github.scrachx.openfood.models.OfflineProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.WelcomeActivity;
import pl.aprilapps.easyphotopicker.EasyImage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends BaseActivity implements ISplashPresenter.View {

    private static final String TAG = "SplashActivity";
    @BindView(R.id.tagline)
    TextView tagline;
    @BindView(R.id.text_loading)
    TextView textLoading;
    int i = 0;
    private ISplashPresenter.Actions presenter;
    private LoadToast toast;
    private String[] taglines;
    SharedPreferences settings;

    /*
    To show different slogans below the logo while content is being downloaded.
     */
    Runnable changeTagline = new Runnable() {
        @Override
        public void run() {
            i++;
            if (i > taglines.length - 1)
                i = 0;
            tagline.setText(taglines[i]);
            tagline.postDelayed(changeTagline, 1500);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_splash);
        taglines = getResources().getStringArray(R.array.taglines_array);
        tagline.post(changeTagline);

        toast = new LoadToast(this);

        presenter = new SplashPresenter(getSharedPreferences("prefs", 0), this);
        settings = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        if (!settings.getBoolean("is_offline_data_available", false)) {
            //Dialog to ask for download
            new MaterialDialog.Builder(this)
                    .content(R.string.download_offline_product)
                    .positiveText(R.string.txtDownload)
                    .negativeText(R.string.txtPictureNeededDialogNo)
                    .onPositive((dialog, which) -> doDownload())
                    .onNegative((dialog, which) -> presenter.refreshData())
                    .show();
        } else {
            presenter.refreshData();
        }
    }

    @Override
    public void navigateToMainActivity() {
        EasyImage.configuration(this)
                .setImagesFolderName("OFF_Images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);
        Intent mainIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public void showLoading() {
        toast.setText(SplashActivity.this.getString(R.string.toast_retrieving));
        toast.setBackgroundColor(ContextCompat.getColor(SplashActivity.this, R.color.blue));
        toast.setTextColor(ContextCompat.getColor(SplashActivity.this, R.color.white));
        toast.show();
    }

    @Override
    public void hideLoading(boolean isError) {
        if (isError)
            toast.error();
        else
            toast.success();
    }

    @Override
    public AssetManager getAssetManager() {
        return getAssets();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            downloadOfflineProducts();
        }
    }

    private void doDownload() {
        if (Utils.isStoragePermissionGranted(this)) {
            downloadOfflineProducts();
        }
    }

    /**
     * This method download the csv file and save it to the database.
     */
    private void downloadOfflineProducts() {
        if (Utils.isNetworkConnected(this) && !Utils.isConnectedToMobileData(this)) {
            textLoading.setText("Downloading Data...");
            OpenFoodAPIClient client = new OpenFoodAPIClient(this);
            String fileURL = "http://fr.openfoodfacts.org/data/offline/fr.openfoodfacts.org.products.small.zip";
            client.getAPIService().downloadFileWithDynamicUrlSync(fileURL)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                textLoading.setText(getString(R.string.txtProcessing));
                                Log.d(TAG, "server contacted and has file");
                                unzip(response.body(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "openfoodfacts").getAbsolutePath());
                            } else {
                                new MaterialDialog.Builder(SplashActivity.this)
                                        .content("Connection failed!")
                                        .positiveText("Retry")
                                        .negativeText(R.string.txtPictureNeededDialogNo)
                                        .onPositive((dialog, which) -> doDownload())
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                textLoading.setText(getString(R.string.txtLoading));
                                                presenter.refreshData();
                                            }
                                        })
                                        .show();
                                Log.d(TAG, "server contact failed");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            new MaterialDialog.Builder(SplashActivity.this)
                                    .content("Connection failed!")
                                    .positiveText("Retry")
                                    .negativeText(R.string.txtPictureNeededDialogNo)
                                    .onPositive((dialog, which) -> doDownload())
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            textLoading.setText(getString(R.string.txtLoading));
                                            presenter.refreshData();
                                        }
                                    })
                                    .show();
                            Log.e(TAG, "error");
                        }
                    });
        } else {
            new MaterialDialog.Builder(SplashActivity.this)
                    .content("Wifi Connection not found!")
                    .positiveText("Retry")
                    .negativeText(R.string.txtPictureNeededDialogNo)
                    .onPositive((dialog, which) -> doDownload())
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            textLoading.setText(getString(R.string.txtLoading));
                            presenter.refreshData();
                        }
                    })
                    .show();
        }
    }

    public void unzip(ResponseBody responseBody, String _location) {
        try {
            ZipInputStream zin = new ZipInputStream(responseBody.byteStream());

            byte b[] = new byte[1024];
            String string = "";

            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());

                if (ze.isDirectory()) {
                    _dirChecker(_location, ze.getName());
                } else {

                    if (!new File(_location).isDirectory()) {
                        new File(_location).mkdirs();
                    }
                    FileOutputStream fout = new FileOutputStream(_location + File.separator + ze.getName());
                    string = ze.getName();
                    BufferedInputStream in = new BufferedInputStream(zin);
                    BufferedOutputStream out = new BufferedOutputStream(fout);

                    int n;
                    while ((n = in.read(b, 0, 1024)) >= 0) {
                        out.write(b, 0, n);
                    }

                    zin.closeEntry();
                    out.close();
                }

            }
            new saveCSVToDb(new File(_location + File.separator + string), settings, this).execute();
            zin.close();
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
            new MaterialDialog.Builder(SplashActivity.this)
                    .content(R.string.txtConnectionFailed)
                    .positiveText(getString(R.string.retry))
                    .negativeText(R.string.txtPictureNeededDialogNo)
                    .onPositive((dialog, which) -> doDownload())
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            textLoading.setText(getString(R.string.txtLoading));
                            presenter.refreshData();
                        }
                    })
                    .show();
        }

    }

    private void _dirChecker(String _location, String dir) {
        File f = new File(_location + dir);

        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    private class saveCSVToDb extends AsyncTask<Void, Void, Boolean> {
        File file;
        @SuppressLint("StaticFieldLeak")
        Context context;
        SharedPreferences settings;

        private saveCSVToDb(File file, SharedPreferences settings, Context context) {
            this.file = file;
            this.settings = settings;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean is) {
            super.onPostExecute(is);
            if (is) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("is_offline_data_available", true);
                editor.apply();
                textLoading.setText(getString(R.string.txtLoading));
                presenter.refreshData();
            } else {
                new MaterialDialog.Builder(SplashActivity.this)
                        .content(getString(R.string.txtConnectionFailed))
                        .positiveText(getString(R.string.retry))
                        .negativeText(R.string.txtPictureNeededDialogNo)
                        .onPositive((dialog, which) -> doDownload())
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                textLoading.setText(getString(R.string.txtLoading));
                                presenter.refreshData();
                            }
                        })
                        .show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            OfflineProductDao mOfflineProductDao = Utils.getAppDaoSession(context).getOfflineProductDao();
            //parsing csv file
            try (CSVReader reader = new CSVReader(new FileReader(file.getAbsolutePath()), '\t')) {
                List<String[]> records = reader.readAll();
                List<OfflineProduct> list = new ArrayList<>();
                Iterator<String[]> iterator = records.iterator();
                // To skip header in the csv file
                iterator.hasNext();
                long size = records.size();
                long count = 0;
                while (iterator.hasNext()) {
                    String[] record = iterator.next();
                    OfflineProduct offlineProduct = new OfflineProduct(record[1], record[3], record[0], record[2], record[4]);
                    list.add(offlineProduct);
                    if (count % 15000 == 0) {
                        // saving to db
                        mOfflineProductDao.insertOrReplaceInTx(list);
                        list.clear();
                    }
                    count++;
                    Log.d(TAG, "completed " + count + " / " + size);
                    long finalCount = count;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textLoading.setText(getString(R.string.txtSaving) + ((finalCount * 100) / size) + "%)");
                        }
                    });
                    Log.d(TAG, "progress " + ((float) count / (float) size) * 100 + "% ");
                }
                Log.d(TAG, "done progress " + ((float) count / (float) size) * 100 + "% ");
                mOfflineProductDao.insertOrReplaceInTx(list);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }

}