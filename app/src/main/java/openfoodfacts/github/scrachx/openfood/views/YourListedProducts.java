package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.*;
import openfoodfacts.github.scrachx.openfood.utils.*;
import openfoodfacts.github.scrachx.openfood.views.adapters.YourListedProductsAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class YourListedProducts extends BaseActivity implements SwipeControllerActions {
    @BindView(R.id.rvYourListedProducts)
    RecyclerView recyclerView;
    @BindView(R.id.tvInfoYourListedProducts)
    TextView tvInfo;
    @BindView(R.id.scanFirstYourListedProduct)
    Button btnScanFirst;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    private ProductLists thisProductList;
    private List<YourListedProduct> products;
    private YourListedProductDao yourListedProductDao;
    private Long id;
    private YourListedProductsAdapter adapter;
    private Boolean isLowBatteryMode = false;
    private Product p;
    private String listName;
    private Boolean emptyList = false;
    private Boolean isEatenList = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_listed_products);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (Utils.isDisableImageLoad(this) && Utils.getBatteryLevel(this)) {
            isLowBatteryMode = true;
        }
        ProductListsDao  productListsDao = Utils.getDaoSession(this).getProductListsDao();
        yourListedProductDao = Utils.getAppDaoSession(this).getYourListedProductDao();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            id = bundle.getLong("listId");
            listName = bundle.getString("listName");
            setTitle(listName);
            p = (Product) bundle.get("product");
        }
        String locale = LocaleHelper.getLanguage(getBaseContext());
        if (p != null && p.getCode() != null && p.getProductName() != null
            && p.getImageSmallUrl(locale) != null) {

            String barcode = p.getCode();
            String productName = p.getProductName();

            String productDetails = getProductBrandsQuantityDetails(p);
            String imageUrl = p.getImageSmallUrl(locale);
            YourListedProduct product = new YourListedProduct();
            product.setBarcode(barcode);
            product.setListId(id);
            product.setListName(listName);
            product.setProductName(productName);
            product.setProductDetails(productDetails);
            product.setImageUrl(imageUrl);
            yourListedProductDao.insertOrReplace(product);
        }

        thisProductList = productListsDao.load(id);
        if(thisProductList==null){
            return;
        }
        thisProductList.resetProducts();
        if (thisProductList.getId() == 1L) {
            isEatenList = true;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        products = thisProductList.getProducts();
        if (products.isEmpty()) {
            emptyList = true;
            tvInfo.setVisibility(View.VISIBLE);
            btnScanFirst.setVisibility(View.VISIBLE);
            setInfo(tvInfo);
        }

        if (products != null) {
            adapter = new YourListedProductsAdapter(this, products, isLowBatteryMode);
            recyclerView.setAdapter(adapter);

            SwipeController swipeController = new SwipeController(this, YourListedProducts.this);
            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(recyclerView);
        }
        BottomNavigationListenerInstaller.install(bottomNavigationView, this, getBaseContext());
    }

    public static String getProductBrandsQuantityDetails(Product p) {
        return getProductBrandsQuantityDetails(p.getBrands(), p.getQuantity());
    }

    public static String getProductBrandsQuantityDetails(HistoryItem p) {
        return getProductBrandsQuantityDetails(p.getBrands(), p.getQuantity());
    }

    public static String getProductBrandsQuantityDetails(String brands, String quantity) {
        StringBuilder stringBuilder = new StringBuilder();
        if (isNotEmpty(brands)) {
            stringBuilder.append(capitalize(brands.split(",")[0].trim()));
        }
        if (isNotEmpty(quantity)) {
            stringBuilder.append(" - ").append(quantity);
        }
        return stringBuilder.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_your_listed_products, menu);
        menu.findItem(R.id.action_export_all_listed_products)
            .setVisible(!emptyList);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent data = new Intent();
                data.putExtra("update", true);
                setResult(RESULT_OK, data);
                finish();
                return true;
            case R.id.action_export_all_listed_products:
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.scanFirstYourListedProduct)
    protected void onScanFirst() {
        if (Utils.isHardwareCameraInstalled(getBaseContext())) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(YourListedProducts.this, Manifest.permission.CAMERA)) {
                    new MaterialDialog.Builder(this)
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(YourListedProducts.this, new String[]{Manifest
                            .permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                        .show();
                } else {
                    ActivityCompat.requestPermissions(YourListedProducts.this, new String[]{Manifest.permission.CAMERA}, Utils
                        .MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                Intent intent = new Intent(YourListedProducts.this, ContinuousScanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }
    }

    public void setInfo(TextView view) {
        if (isEatenList) {
            view.setText(getString(R.string.txt_info_eaten_products));
        } else {
            view.setText(R.string.txt_info_your_listed_products);
        }
    }

    public void exportCSV() {
        String folderMain = FileUtils.getCsvFolderName();
        Toast.makeText(this, R.string.txt_exporting_your_listed_products, Toast.LENGTH_LONG).show();
        File baseDir = new File(Environment.getExternalStorageDirectory(), folderMain);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        String productListName = thisProductList.getListName();
        String fileName = BuildConfig.FLAVOR.toUpperCase() + "-" + productListName + "-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";
        File f = new File(baseDir,fileName);
        boolean isDownload;
        try (CSVPrinter writer = new CSVPrinter(new FileWriter(f), CSVFormat.DEFAULT.withHeader(getResources().getStringArray(R.array.your_products_headers)))) {
            List<YourListedProduct> listProducts = thisProductList.getProducts();
            for (YourListedProduct product : listProducts) {
                writer.printRecord(product.getBarcode(), product.getProductName(), product.getListName(), product.getProductDetails());
            }
            Toast.makeText(this, R.string.txt_your_listed_products_exported, Toast.LENGTH_LONG).show();
            isDownload = true;
        } catch (IOException e) {
            isDownload = false;
            Log.e(YourListedProducts.class.getSimpleName(), "exportCSV", e);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
        downloadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri csvUri = FileProvider.getUriForFile(this, this.getPackageName() + ".provider", f);
        downloadIntent.setDataAndType(csvUri, "text/csv");
        downloadIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("downloadChannel", "ChannelCSV", importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            String channelId = "export_channel";
            CharSequence channelName = getString(R.string.notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.setDescription(getString(R.string.notify_channel_description));
            notificationManager.createNotificationChannel(notificationChannel);
        }

        if (isDownload) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "export_channel")
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(getString(R.string.notify_content))
                .setContentIntent(PendingIntent.getActivity(this, 4, downloadIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher);
            notificationManager.notify(8, builder.build());
        }
    }

    @Override
    public void onRightClicked(int position) {
        if (CollectionUtils.isNotEmpty(products)) {
            final YourListedProduct productToRemove = products.get(position);
            yourListedProductDao.delete(productToRemove);
            adapter.remove(productToRemove);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, adapter.getItemCount());
        }
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("update", true);
        setResult(RESULT_OK, data);
        super.onBackPressed();
    }
}
