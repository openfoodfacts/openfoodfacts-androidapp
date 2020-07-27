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
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityYourListedProductsBinding;
import openfoodfacts.github.scrachx.openfood.models.HistoryItem;
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct;
import openfoodfacts.github.scrachx.openfood.models.HistoryProductDao;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists;
import openfoodfacts.github.scrachx.openfood.models.entities.ProductListsDao;
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProduct;
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProductDao;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.SwipeController;
import openfoodfacts.github.scrachx.openfood.utils.SwipeControllerActions;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.YourListedProductsAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;

import static org.apache.commons.lang.StringUtils.capitalize;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class YourListedProductsActivity extends BaseActivity implements SwipeControllerActions {
    private ActivityYourListedProductsBinding binding;
    private ProductLists thisProductList;
    private List<YourListedProduct> products;
    private YourListedProductDao yourListedProductDao;
    private HistoryProductDao historyProductDao;
    private Long listID;
    private YourListedProductsAdapter adapter;
    private Boolean isLowBatteryMode = false;
    private Product prodToAdd;
    private String listName;
    private Boolean emptyList = false;
    private Boolean isEatenList = false;
    private String sortType = "none";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYourListedProductsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // OnClick
        binding.scanFirstYourListedProduct.setOnClickListener(v -> onFirstScan());

        if (Utils.isDisableImageLoad(this) && Utils.isBatteryLevelLow(this)) {
            isLowBatteryMode = true;
        }
        ProductListsDao productListsDao = Utils.getDaoSession().getProductListsDao();
        yourListedProductDao = Utils.getDaoSession().getYourListedProductDao();
        historyProductDao = Utils.getDaoSession().getHistoryProductDao();

        // Get listid and add product to list if bundle is present
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            listID = bundle.getLong("listId");
            listName = bundle.getString("listName");
            setTitle(listName);
            prodToAdd = (Product) bundle.get("product");
        }
        String locale = LocaleHelper.getLanguage(this);
        if (prodToAdd != null && prodToAdd.getCode() != null && prodToAdd.getProductName() != null
            && prodToAdd.getImageSmallUrl(locale) != null) {

            String barcode = prodToAdd.getCode();
            String productName = prodToAdd.getProductName();

            String productDetails = getProductBrandsQuantityDetails(prodToAdd);
            String imageUrl = prodToAdd.getImageSmallUrl(locale);
            YourListedProduct product = new YourListedProduct();
            product.setBarcode(barcode);
            product.setListId(listID);
            product.setListName(listName);
            product.setProductName(productName);
            product.setProductDetails(productDetails);
            product.setImageUrl(imageUrl);
            yourListedProductDao.insertOrReplace(product);
        }

        thisProductList = productListsDao.load(listID);
        if (thisProductList == null) {
            return;
        }
        thisProductList.resetProducts();
        if (thisProductList.getId() == 1L) {
            isEatenList = true;
        }
        binding.rvYourListedProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.rvYourListedProducts.setHasFixedSize(false);
        products = thisProductList.getProducts();

        if (products.isEmpty()) {
            emptyList = true;
            binding.tvInfoYourListedProducts.setVisibility(View.VISIBLE);
            binding.scanFirstYourListedProduct.setVisibility(View.VISIBLE);
            setInfo(binding.tvInfoYourListedProducts);
        }

        if (products != null) {
            adapter = new YourListedProductsAdapter(this, products, isLowBatteryMode);
            binding.rvYourListedProducts.setAdapter(adapter);

            SwipeController swipeController = new SwipeController(this, YourListedProductsActivity.this);
            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(binding.rvYourListedProducts);
        }

        BottomNavigationListenerInstaller.selectNavigationItem(binding.bottomNavigation.bottomNavigation, 0);
        BottomNavigationListenerInstaller.install(binding.bottomNavigation.bottomNavigation, this);
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
        menu.findItem(R.id.action_sort_listed_products)
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
            case R.id.action_sort_listed_products:
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
                            sortType = "title";
                            break;

                        case 1:
                            sortType = "brand";
                            break;

                        case 2:
                            if (BuildConfig.FLAVOR.equals("off")) {
                                sortType = "grade";
                            } else {
                                sortType = "time";
                            }
                            break;

                        case 3:
                            sortType = "barcode";
                            break;

                        default:
                            sortType = "time";
                            break;
                    }

                    sortProducts();
                    adapter = new YourListedProductsAdapter(this, products, isLowBatteryMode);
                    binding.rvYourListedProducts.setAdapter(adapter);
                });
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortProducts() {
        switch (sortType) {
            case "title":
                Collections.sort(products, (p1, p2) -> p1.getProductName().compareToIgnoreCase(p2.getProductName()));
                break;

            case "brand":
                Collections.sort(products, (p1, p2) -> p1.getProductDetails().compareToIgnoreCase(p2.getProductDetails()));
                break;

            case "barcode":
                Collections.sort(products, (p1, p2) -> p1.getBarcode().compareToIgnoreCase(p2.getBarcode()));
                break;
            case "grade":

                //get list of HistoryProduct items for the YourListProduct items
                WhereCondition[] conditionsGrade = new WhereCondition[products.size()];
                int i = 0;
                for (YourListedProduct p : products) {
                    conditionsGrade[i] = HistoryProductDao.Properties.Barcode.eq(p.getBarcode());
                    i++;
                }
                List<HistoryProduct> historyProductsGrade;
                QueryBuilder<HistoryProduct> qbGrade = historyProductDao.queryBuilder();
                qbGrade.whereOr(conditionsGrade[0], conditionsGrade[1], Arrays.copyOfRange(conditionsGrade, 2, conditionsGrade.length));
                historyProductsGrade = qbGrade.list();

                Collections.sort(products, (p1, p2) -> {

                    String g1 = "E";
                    String g2 = "E";

                    for (HistoryProduct h : historyProductsGrade) {
                        if (h.getBarcode().equals(p1.getBarcode())) {
                            if (h.getNutritionGrade() != null) {
                                g1 = h.getNutritionGrade();
                            }
                        }
                        if (h.getBarcode().equals(p2.getBarcode())) {
                            if (h.getNutritionGrade() != null) {
                                g2 = h.getNutritionGrade();
                            }
                        }
                    }
                    return g1.compareToIgnoreCase(g2);
                });
                break;

            case "time":
                //get list of HistoryProduct items for the YourListProduct items
                WhereCondition[] conditionsTime = new WhereCondition[products.size()];
                int j = 0;
                for (YourListedProduct p : products) {
                    conditionsTime[j] = HistoryProductDao.Properties.Barcode.eq(p.getBarcode());
                    j++;
                }
                List<HistoryProduct> historyProductsTime;
                QueryBuilder<HistoryProduct> qbTime = historyProductDao.queryBuilder();
                qbTime.whereOr(conditionsTime[0], conditionsTime[1], Arrays.copyOfRange(conditionsTime, 2, conditionsTime.length));
                historyProductsTime = qbTime.list();

                Collections.sort(products, (p1, p2) -> {

                    Date d1 = new Date(0);
                    Date d2 = new Date(0);

                    for (HistoryProduct h : historyProductsTime) {
                        if (h.getBarcode().equals(p1.getBarcode())) {
                            if (h.getLastSeen() != null) {
                                d1 = h.getLastSeen();
                            }
                        }
                        if (h.getBarcode().equals(p2.getBarcode())) {
                            if (h.getLastSeen() != null) {
                                d2 = h.getLastSeen();
                            }
                        }
                    }
                    //compare d2 to d1 because its recently viewed list, so items with later(higher) date come first in the list
                    return d2.compareTo(d1);
                });
                break;

            default:
                Collections.sort(products, (p1, p2) -> 0);
        }
    }

    private void onFirstScan() {
        if (!Utils.isHardwareCameraInstalled(this)) {
            Log.e(this.getClass().getSimpleName(), "device has no camera installed");
            return;
        }
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new MaterialDialog.Builder(this)
                    .title(R.string.action_about)
                    .content(R.string.permission_camera)
                    .neutralText(R.string.txtOk)
                    .onNeutral((dialog, which) ->
                        ActivityCompat.requestPermissions(YourListedProductsActivity.this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                    .show();
            } else {
                ActivityCompat.requestPermissions(YourListedProductsActivity.this, new String[]{Manifest.permission.CAMERA}, Utils
                    .MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            Intent intent = new Intent(this, ContinuousScanActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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
        String fileName = BuildConfig.FLAVOR.toUpperCase() + "-" + productListName + "-" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) + ".csv";
        File f = new File(baseDir, fileName);
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
            Log.e(YourListedProductsActivity.class.getSimpleName(), "exportCSV", e);
        }

        Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
        NotificationManager notificationManager = createNotification(f, downloadIntent, this);

        if (isDownload) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "export_channel")
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(getString(R.string.notify_content))
                .setContentIntent(PendingIntent.getActivity(this, 4, downloadIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher);
            notificationManager.notify(8, builder.build());
        }
    }

    static NotificationManager createNotification(File f, Intent downloadIntent, Context context) {
        Uri csvUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", f);
        downloadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        downloadIntent.setDataAndType(csvUri, "text/csv");
        downloadIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("downloadChannel", "ChannelCSV", importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId = "export_channel";
            CharSequence channelName = context.getString(R.string.notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.setDescription(context.getString(R.string.notify_channel_description));
            notificationManager.createNotificationChannel(notificationChannel);
        }
        return notificationManager;
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
