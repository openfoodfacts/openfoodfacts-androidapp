package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.afollestad.materialdialogs.MaterialDialog;
import java.util.List;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductLists;
import openfoodfacts.github.scrachx.openfood.models.ProductListsDao;
import openfoodfacts.github.scrachx.openfood.utils.SwipeController;
import openfoodfacts.github.scrachx.openfood.utils.SwipeControllerActions;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductListsAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.RecyclerItemClickListener;

public class ProductListsActivity extends BaseActivity implements SwipeControllerActions {
    @BindView(R.id.fabAdd)
    FloatingActionButton fabAdd;
    @BindView(R.id.product_lists_recycler_view)
    RecyclerView recyclerView;
    ProductListsAdapter adapter;
    List<ProductLists> productLists;
    ProductListsDao productListsDao;

    public static Intent getIntent(Context context) {
        return new Intent(context, ProductListsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_lists);
        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle(R.string.your_lists);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productListsDao=Utils.getDaoSession(this).getProductListsDao();
        if(productListsDao.loadAll().size()==0) {
            ProductLists eatenList=new ProductLists(getString(R.string.txt_eaten_products),0);
            productListsDao.insert(eatenList);
            ProductLists toBuyList=new ProductLists(getString(R.string.txt_products_to_buy),0);
            productListsDao.insert(toBuyList);
        }
         productLists=productListsDao.loadAll();

        adapter= new ProductListsAdapter(this,productLists);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            Product p=(Product) bundle.get("product");

            new MaterialDialog.Builder(this)
                    .title(R.string.txt_create_new_list)
                    .input("List name","",false, (dialog, input) -> {
                                ProductLists productList=new ProductLists(input.toString(),0);
                                productLists.add(productList);
                                productListsDao.insert(productList);
                                String listName=input.toString();
                                Long id=productList.getId();
                                Intent intent=new Intent(this,YourListedProducts.class);
                                intent.putExtra("listId",id);
                                intent.putExtra("listName",listName);
                                intent.putExtra("product",p);
                                startActivityForResult(intent,1);
                            }
                    )
                    .positiveText(R.string.txt_discard)
                    .negativeText(R.string.txtSave)
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        adapter.notifyDataSetChanged();
                    })
                    .show();
        }

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(ProductListsActivity.this,((view, position) -> {
                    Long id=productLists.get(position).getId();
                    String listName=productLists.get(position).getListName();
                    Intent intent=new Intent(this,YourListedProducts.class);
                    intent.putExtra("listId",id);
                    intent.putExtra("listName",listName);
                    startActivityForResult(intent,1);
                }))
        );

        SwipeController swipeController = new SwipeController(this, ProductListsActivity.this);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        fabAdd.setOnClickListener(view -> new MaterialDialog.Builder(this)
                .title(R.string.txt_create_new_list)
                .input("List name","",false, (dialog, input) -> {
                    ProductLists productList=new ProductLists(input.toString(),0);
                    productLists.add(productList);
                    productListsDao.insert(productList);
                }
                )
                .positiveText(R.string.txtYes)
                .negativeText(R.string.txtNo)
                .onPositive((dialog, which) -> {
                    dialog.dismiss();
                    adapter.notifyDataSetChanged();
                })
                .show());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode ==RESULT_OK){
                Boolean update=data.getExtras().getBoolean("update");
                if(update){
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }


    @Override
    public void onRightClicked(int position) {
        if (productLists != null && productLists.size() > 0) {
            productListsDao.delete(productLists.get(position));
        }
        adapter.remove(productLists.get(position));
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, adapter.getItemCount());
    }

}
