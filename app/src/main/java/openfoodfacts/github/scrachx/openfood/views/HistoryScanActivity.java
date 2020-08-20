package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityHistoryScanBinding;
import openfoodfacts.github.scrachx.openfood.models.HistoryItem;
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct;
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.SwipeController;
import openfoodfacts.github.scrachx.openfood.utils.SwipeControllerActions;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.HistoryListAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;

public class HistoryScanActivity extends BaseActivity implements SwipeControllerActions {
    private ActivityHistoryScanBinding binding;
    private List<HistoryItem> productItems;
    private boolean emptyHistory;
    private HistoryProductDao mHistoryProductDao;
    private HistoryListAdapter adapter;
    private Disposable disposable;
    private List<HistoryProduct> listHistoryProducts;
    //boolean to determine if image should be loaded or not
    private boolean isLowBatteryMode = false;
    private static String SORT_TYPE = "none";

    public static void start(Context context) {
        Intent starter = new Intent(context, HistoryScanActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        binding = ActivityHistoryScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.scanFirst.setOnClickListener(v -> onScanFirst());

        setTitle(getString(R.string.scan_history_drawer));

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (Utils.isDisableImageLoad(this) && Utils.isBatteryLevelLow(this)) {
            isLowBatteryMode = true;
        }

        mHistoryProductDao = Utils.getDaoSession().getHistoryProductDao();
        productItems = new ArrayList<>();
        setInfo(binding.emptyHistoryInfo);

        binding.srRefreshHistoryScanList.setOnRefreshListener(() -> {
            mHistoryProductDao = Utils.getDaoSession().getHistoryProductDao();
            productItems = new ArrayList<>();
            setInfo(binding.emptyHistoryInfo);
            fillView();
            binding.srRefreshHistoryScanList.setRefreshing(false);
        });

        BottomNavigationListenerInstaller.install(binding.navigationBottom.bottomNavigation, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //to fill the view in any case even if the user scans products from History screen...
        fillView();
    }

    @Override
    public void onRightClicked(int position) {
        if (CollectionUtils.isNotEmpty(listHistoryProducts)) {
            mHistoryProductDao.delete(listHistoryProducts.get(position));
        }
        adapter.remove(productItems.get(position));
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, adapter.getItemCount());

        if (adapter.getItemCount() == 0) {
            binding.emptyHistoryInfo.setVisibility(View.VISIBLE);
            binding.scanFirst.setVisibility(View.VISIBLE);
        }
    }

    public void exportCSV() {

        String folderMain = FileUtils.getCsvFolderName();
        Toast.makeText(this, R.string.txt_exporting_history, Toast.LENGTH_LONG).show();
        File baseDir = new File(Environment.getExternalStorageDirectory(), folderMain);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        Log.d("dir", String.valueOf(baseDir));
        String fileName = BuildConfig.FLAVOR.toUpperCase() + "-" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) + ".csv";
        File csvFile = new File(baseDir, fileName);
        boolean isDownload = false;
        try (CSVPrinter writer = new CSVPrinter(new FileWriter(csvFile), CSVFormat.DEFAULT.withHeader(getResources().getStringArray(R.array.headers)))) {
            for (HistoryProduct hp : mHistoryProductDao.loadAll()) {
                writer.printRecord(hp.getBarcode(), hp.getTitle(), hp.getBrands());
            }
            Toast.makeText(this, R.string.txt_history_exported, Toast.LENGTH_LONG).show();
            isDownload = true;
        } catch (IOException e) {
            Log.e(HistoryScanActivity.class.getSimpleName(), "can export to " + csvFile, e);
        }

        Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
        NotificationManager notificationManager = YourListedProductsActivity.createNotification(csvFile, downloadIntent, this);

        if (isDownload) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "export_channel")
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(getString(R.string.notify_content))
                .setContentIntent(PendingIntent.getActivity(this, 4, downloadIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher);
            notificationManager.notify(7, builder.build());
        }
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
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        binding = null;
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
                        final RecyclerView.Adapter adapter = binding.listHistoryScan.getAdapter();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        binding.emptyHistoryInfo.setVisibility(View.VISIBLE);
                        binding.scanFirst.setVisibility(View.VISIBLE);
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
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Utils
                            .MY_PERMISSIONS_REQUEST_STORAGE);
                    }
                } else {
                    exportCSV();
                }
                return true;

            case R.id.sort_history:
                MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
                builder.title(R.string.sort_by);
                String[] sortTypes;
                if (BuildConfig.FLAVOR.equals("off")) {
                    sortTypes = new String[]{getString(R.string.by_title), getString(R.string.by_brand), getString(R.string.by_nutrition_grade), getString(
                        R.string.by_barcode), getString(R.string.by_time)};
                } else {
                    sortTypes = new String[]{getString(R.string.by_title), getString(R.string.by_brand), getString(R.string.by_time), getString(R.string.by_barcode)};
                }
                builder.items(sortTypes);
                builder.itemsCallback((dialog, itemView, position, text) -> {

                    switch (position) {

                        case 0:
                            SORT_TYPE = "title";
                            fillView();
                            break;

                        case 1:
                            SORT_TYPE = "brand";
                            fillView();
                            break;

                        case 2:

                            if (BuildConfig.FLAVOR.equals("off")) {
                                SORT_TYPE = "grade";
                            } else {
                                SORT_TYPE = "time";
                            }
                            fillView();
                            break;

                        case 3:
                            SORT_TYPE = "barcode";
                            fillView();
                            break;

                        default:
                            SORT_TYPE = "time";
                            fillView();
                            break;
                    }
                });
                builder.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utils.MY_PERMISSIONS_REQUEST_STORAGE) {
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
        } else if (requestCode == Utils.MY_PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager
                .PERMISSION_GRANTED) {
                Intent intent = new Intent(HistoryScanActivity.this, ContinuousScanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationListenerInstaller.selectNavigationItem(binding.navigationBottom.bottomNavigation, R.id.history_bottom_nav);
    }

    protected void onScanFirst() {

        if (Utils.isHardwareCameraInstalled(getBaseContext())) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(HistoryScanActivity.this, Manifest.permission.CAMERA)) {
                    new MaterialDialog.Builder(this)
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(HistoryScanActivity.this, new String[]{Manifest
                            .permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                        .show();
                } else {
                    ActivityCompat.requestPermissions(HistoryScanActivity.this, new String[]{Manifest.permission.CAMERA}, Utils
                        .MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                Intent intent = new Intent(HistoryScanActivity.this, ContinuousScanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    public void setInfo(TextView view) {

        String info = getString(R.string.scan_first_string);

        view.setText(info);
    }

    /**
     * Function to compare history items based on title, brand, barcode, time and nutrition grade
     *
     * @param sortType String to determine type of sorting
     * @param productItems List of history items to be sorted
     */
    private void sort(String sortType, List<HistoryItem> productItems) {

        switch (sortType) {

            case "title":

                Collections.sort(productItems, (historyItem, t1) -> {
                    if (TextUtils.isEmpty(historyItem.getTitle())) {
                        historyItem.setTitle(getResources().getString(R.string.no_title));
                    }
                    if (TextUtils.isEmpty(t1.getTitle())) {
                        t1.setTitle(getResources().getString(R.string.no_title));
                    }
                    return historyItem.getTitle().compareToIgnoreCase(t1.getTitle());
                });

                break;

            case "brand":

                Collections.sort(productItems, (historyItem, t1) -> {
                    if (TextUtils.isEmpty(historyItem.getBrands())) {
                        historyItem.setBrands(getResources().getString(R.string.no_brand));
                    }
                    if (TextUtils.isEmpty(t1.getBrands())) {
                        t1.setBrands(getResources().getString(R.string.no_brand));
                    }
                    return historyItem.getBrands().compareToIgnoreCase(t1.getBrands());
                });

                break;

            case "barcode":

                Collections.sort(productItems, (historyItem, t1) -> historyItem.getBarcode().compareTo(t1.getBarcode()));
                break;

            case "grade":
                Collections.sort(productItems, (historyItem, t1) -> {
                    String nGrade1;
                    String nGrade2;
                    if (historyItem.getNutritionGrade() == null) {
                        nGrade1 = "E";
                    } else {
                        nGrade1 = historyItem.getNutritionGrade();
                    }
                    if (t1.getNutritionGrade() == null) {
                        nGrade2 = "E";
                    } else {
                        nGrade2 = t1.getNutritionGrade();
                    }
                    return nGrade1.compareToIgnoreCase(nGrade2);
                });

                break;

            default:

                Collections.sort(productItems, (historyItem, t1) -> 0);
        }
    }

    private void fillView() {
        if (disposable != null) {
            disposable.dispose();
        }
        Log.i(HistoryScanActivity.class.getSimpleName(), "task fillview started...");
        disposable = getFillViewCompletable().subscribe(() ->
            Log.i(HistoryScanActivity.class.getSimpleName(), "task fillview ended"));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private Completable getFillViewCompletable() {
        final Completable refreshAct = Completable.fromAction(() -> {
            if (binding.srRefreshHistoryScanList.isRefreshing()) {
                binding.historyProgressbar.setVisibility(View.GONE);
            } else {
                binding.historyProgressbar.setVisibility(View.VISIBLE);
            }
        });
        final Single<List<HistoryProduct>> dbSingle = Single.fromCallable(() -> {
            productItems.clear();
            List<HistoryProduct> historyProducts = mHistoryProductDao.queryBuilder().orderDesc(HistoryProductDao.Properties.LastSeen).list();
            for (HistoryProduct historyProduct : historyProducts) {
                productItems.add(new HistoryItem(historyProduct.getTitle(), historyProduct.getBrands(), historyProduct.getUrl(), historyProduct
                    .getBarcode(), historyProduct.getLastSeen(), historyProduct.getQuantity(), historyProduct.getNutritionGrade()));
            }
            return historyProducts;
        });
        final Function<List<HistoryProduct>, CompletableSource> updateUiFunc = historyProducts -> {
            if (historyProducts.isEmpty()) {
                emptyHistory = true;
                binding.historyProgressbar.setVisibility(View.GONE);
                binding.emptyHistoryInfo.setVisibility(View.VISIBLE);
                binding.scanFirst.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
                return Completable.complete();
            }

            sort(SORT_TYPE, productItems);

            adapter = new HistoryListAdapter(productItems, HistoryScanActivity.this, isLowBatteryMode);
            binding.listHistoryScan.setAdapter(adapter);
            binding.listHistoryScan.setLayoutManager(new LinearLayoutManager(HistoryScanActivity.this));
            binding.historyProgressbar.setVisibility(View.GONE);

            SwipeController swipeController = new SwipeController(HistoryScanActivity.this, HistoryScanActivity.this);
            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(binding.listHistoryScan);

            return Completable.complete();
        };

        return refreshAct.subscribeOn(AndroidSchedulers.mainThread()) // Change ui on main thread

            .observeOn(Schedulers.io()) // Switch for db operations
            .andThen(dbSingle)

            .observeOn(AndroidSchedulers.mainThread()) // Change ui on main thread
            .flatMapCompletable(updateUiFunc);
    }
}
