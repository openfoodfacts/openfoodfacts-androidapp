package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;

import java.util.List;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductLists;
import openfoodfacts.github.scrachx.openfood.models.ProductListsDao;
import openfoodfacts.github.scrachx.openfood.models.YourListedProduct;
import openfoodfacts.github.scrachx.openfood.models.YourListedProductDao;
import openfoodfacts.github.scrachx.openfood.utils.SwipeController;
import openfoodfacts.github.scrachx.openfood.utils.SwipeControllerActions;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.YourListedProductsAdapter;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class YourListedProducts extends BaseActivity implements SwipeControllerActions {
    @BindView(R.id.rvYourListedProducts)
    RecyclerView recyclerView;

    private ProductListsDao productListsDao;
    private ProductLists thisProductList;
    private List<YourListedProduct> products;
    private YourListedProductDao yourListedProductDao;
    private Long id;
    private YourListedProductsAdapter adapter;
    private Boolean isLowBatteryMode=false;
    private Product p;
    private String listName;

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

            StringBuilder stringBuilder = new StringBuilder();
            if (isNotEmpty(p.getBrands())) {
                stringBuilder.append(capitalize(p.getBrands().split(",")[0].trim()));
            }
            if (isNotEmpty(p.getQuantity())) {
                stringBuilder.append(" - ").append(p.getQuantity());
            }
            String productDetails=stringBuilder.toString();
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        products=thisProductList.getProducts();
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent data=new Intent();
                data.putExtra("update",true);
                setResult(RESULT_OK,data);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
