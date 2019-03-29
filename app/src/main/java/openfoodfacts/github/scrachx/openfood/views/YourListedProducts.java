package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.*;
import openfoodfacts.github.scrachx.openfood.utils.SwipeController;
import openfoodfacts.github.scrachx.openfood.utils.SwipeControllerActions;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.YourListedProductsAdapter;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class YourListedProducts extends BaseActivity implements SwipeControllerActions {
    @BindView(R.id.rvYourListedProducts)
    RecyclerView recyclerView;
    @BindView(R.id.tvInfoYourListedProducts)
    TextView tvInfo;
    @BindView(R.id.scanFirstYourListedProduct)
    Button btnScanFirst;

    private ProductListsDao productListsDao;
    private ProductLists thisProductList;
    private List<YourListedProduct> products;
    private YourListedProductDao yourListedProductDao;
    private Long id;
    private YourListedProductsAdapter adapter;
    private Boolean isLowBatteryMode=false;
    private Product p;
    private String listName;
    private Boolean emptyList=false;
    private Boolean isEatenList=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_listed_products);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(YourListedProducts.this);
        Utils.DISABLE_IMAGE_LOAD = preferences.getBoolean("disableImageLoad", false);
        if (Utils.DISABLE_IMAGE_LOAD && Utils.getBatteryLevel(this)) {
            isLowBatteryMode = true;
        }
        productListsDao=Utils.getDaoSession(this).getProductListsDao();
        yourListedProductDao=Utils.getAppDaoSession(this).getYourListedProductDao();

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            id=bundle.getLong("listId");
            listName=bundle.getString("listName");
            setTitle(listName);
            p=(Product) bundle.get("product");

        }
        if(p!=null && p.getCode()!=null && p.getProductName()!=null
                && p.getImageSmallUrl()!=null){

            String barcode=p.getCode();
            String productName=p.getProductName();

            String productDetails = getProductBrandsQuantityDetails(p);
            String imageUrl=p.getImageSmallUrl();
            YourListedProduct product=new YourListedProduct();
            product.setBarcode(barcode);
            product.setListId(id);
            product.setListName(listName);
            product.setProductName(productName);
            product.setProductDetails(productDetails);
            product.setImageUrl(imageUrl);
            yourListedProductDao.insertOrReplace(product);
        }

        thisProductList=productListsDao.load(id);
        thisProductList.resetProducts();
        if(thisProductList.getId()==1L){
            isEatenList=true;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        products=thisProductList.getProducts();
        if(products.size() == 0){
            emptyList=true;
            tvInfo.setVisibility(View.VISIBLE);
            btnScanFirst.setVisibility(View.VISIBLE);
                setInfo(tvInfo);
        }

        if(products!=null){
             adapter= new YourListedProductsAdapter(this,products,isLowBatteryMode);
            recyclerView.setAdapter(adapter);

            SwipeController swipeController = new SwipeController(this, YourListedProducts.this);
            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(recyclerView);
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                    swipeController.onDraw(c);
                }
            });
        }
    }

    public static String getProductBrandsQuantityDetails(Product p) {
        return  getProductBrandsQuantityDetails(p.getBrands(),p.getQuantity());
    }
    public static String getProductBrandsQuantityDetails(HistoryItem p) {
        return  getProductBrandsQuantityDetails(p.getBrands(),p.getQuantity());
    }

    public static String getProductBrandsQuantityDetails(String  brands,String quantity) {
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
        switch (item.getItemId()){
            case android.R.id.home:
                Intent data=new Intent();
                data.putExtra("update",true);
                setResult(RESULT_OK,data);
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

    public void setInfo(TextView view){
        if(isEatenList){
            view.setText(getString(R.string.txt_info_eaten_products));
        } else{
            view.setText(R.string.txt_info_your_listed_products);
        }
    }

    public void exportCSV() {
        boolean isDownload = false;
        String folder_main = " ";
        String appname = " ";
        if ((BuildConfig.FLAVOR.equals("off"))) {
            folder_main = " Open Food Facts ";
            appname = "OFF";
        } else if ((BuildConfig.FLAVOR.equals("opff"))) {
            folder_main = " Open Pet Food Facts ";
            appname = "OPFF";
        } else if ((BuildConfig.FLAVOR.equals("opf"))) {
            folder_main = " Open Products Facts ";
            appname = "OPF";
        } else {
            folder_main = " Open Beauty Facts ";
            appname = "OBF";
        }
        Toast.makeText(this, R.string.txt_exporting_your_listed_products, Toast.LENGTH_LONG).show();
        File baseDir = new File(Environment.getExternalStorageDirectory(), folder_main);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        String productListName=thisProductList.getListName();
        String fileName = appname + "-" +productListName+"-"+ new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        CSVWriter writer;
        FileWriter fileWriter;
        try {
            if (f.exists() && !f.isDirectory()) {
                fileWriter = new FileWriter(filePath, false);
                writer = new CSVWriter(fileWriter);
            } else {
                writer = new CSVWriter(new FileWriter(filePath));
            }
            String[] headers = getResources().getStringArray(R.array.your_products_headers);
            writer.writeNext(headers);
            List<YourListedProduct> listProducts=thisProductList.getProducts();
            for (YourListedProduct product : listProducts) {
                String[] line = {product.getBarcode(), product.getProductName(),product.getListName(),product.getProductDetails()};
                writer.writeNext(line);
            }
            writer.close();
            Toast.makeText(this, R.string.txt_your_listed_products_exported, Toast.LENGTH_LONG).show();
            isDownload = true;
        } catch (IOException e) {
            e.printStackTrace();
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "export_channel")
                .setContentTitle(getString(R.string.notify_title))
                .setContentText(getString(R.string.notify_content))
                .setContentIntent(PendingIntent.getActivity(this, 4, downloadIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher);

        if (isDownload) {
            notificationManager.notify(8, builder.build());
        }
    }

    @Override
    public void onRightClicked(int position) {
        if(products!=null && products.size()>0){
            yourListedProductDao.delete(products.get(position));
        }
        adapter.remove(products.get(position));
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, adapter.getItemCount());

    }

    @Override
    public void onBackPressed() {
        Intent data=new Intent();
        data.putExtra("update",true);
        setResult(RESULT_OK,data);
        super.onBackPressed();

    }
}
