package openfoodfacts.github.scrachx.openfood.views;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
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
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductListsAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;
import openfoodfacts.github.scrachx.openfood.views.listeners.RecyclerItemClickListener;

public class ProductListsActivity extends BaseActivity implements SwipeControllerActions {
    private static final int ACTIVITY_CHOOSE_FILE = 123;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fabAdd)
    Button fabAdd;
    @BindView(R.id.product_lists_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;
    private ProductListsAdapter adapter;
    private List<ProductLists> productLists;
    private ProductListsDao productListsDao;

    public static Intent getIntent(Context context) {
        return new Intent(context, ProductListsActivity.class);
    }

    public static ProductListsDao getProducListsDaoWithDefaultList(Context context) {
        ProductListsDao productListsDao = Utils.getDaoSession(context).getProductListsDao();
        if (productListsDao.loadAll().isEmpty()) {
            ProductLists eatenList = new ProductLists(context.getString(R.string.txt_eaten_products), 0);
            productListsDao.insert(eatenList);
            ProductLists toBuyList = new ProductLists(context.getString(R.string.txt_products_to_buy), 0);
            productListsDao.insert(toBuyList);
        }
        return productListsDao;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_lists);
        setTitle(R.string.your_lists);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        BottomNavigationListenerInstaller.install(bottomNavigationView, this, getBaseContext());
        fabAdd.setCompoundDrawablesWithIntrinsicBounds(R.drawable.plus_blue, 0, 0, 0);


        productListsDao = getProducListsDaoWithDefaultList(this);
        productLists = productListsDao.loadAll();

        adapter = new ProductListsAdapter(this, productLists);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Product p = (Product) bundle.get("product");

            MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.txt_create_new_list)
                .input(R.string.create_new_list_list_name,
                    R.string.empty, false, (d, input) -> {
                        // do nothing
                    }
                )
                .positiveText(R.string.txtSave)
                .negativeText(R.string.txt_discard)
                .show();
            // this enable to avoid dismissing dalog if list name already exist
            dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Positive", "Positive clicked");
                    String listName = dialog.getInputEditText().getText().toString();
                    boolean isAlreadyIn = checkListNameExist(listName);
                    if (!isAlreadyIn) {
                        ProductLists productList = new ProductLists(listName, 0);
                        productLists.add(productList);
                        productListsDao.insert(productList);
                        Long id = productList.getId();
                        Intent intent = new Intent(ProductListsActivity.this, YourListedProducts.class);
                        intent.putExtra("listId", id);
                        intent.putExtra("listName", listName);
                        intent.putExtra("product", p);
                        startActivityForResult(intent, 1);
                    } else {
                        Toast.makeText(ProductListsActivity.this, R.string.error_duplicate_listname, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        recyclerView.addOnItemTouchListener(
            new RecyclerItemClickListener(ProductListsActivity.this, ((view, position) -> {
                Long id = productLists.get(position).getId();
                String listName = productLists.get(position).getListName();
                Intent intent = new Intent(this, YourListedProducts.class);
                intent.putExtra("listId", id);
                intent.putExtra("listName", listName);
                startActivityForResult(intent, 1);
            }))
        );

        SwipeController swipeController = new SwipeController(this, ProductListsActivity.this);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        fabAdd.setOnClickListener(view -> {
            MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.txt_create_new_list)
                .input("List name", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    }
                })
                .positiveText(R.string.dialog_create)
                .negativeText(R.string.dialog_cancel)
                .show();
            // this enable to avoid dismissing dalog if list name already exist
            dialog.getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Positive", "Positive clicked");
                    String listName = dialog.getInputEditText().getText().toString();
                    boolean isAlreadyIn = checkListNameExist(listName);
                    if (!isAlreadyIn) {
                        ProductLists productList = new ProductLists(listName, 0);
                        productLists.add(productList);
                        productListsDao.insert(productList);
                        dialog.dismiss();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ProductListsActivity.this, R.string.error_duplicate_listname, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    /**
     * Check if listname already in products lists.
     *
     * @param listName
     */
    private boolean checkListNameExist(String listName) {
        for (Iterator<ProductLists> i = productLists.iterator(); i.hasNext(); ) {
            if (i.next().getListName().equals(listName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK && data.getExtras().getBoolean("update")) {
            adapter.notifyDataSetChanged();
        }else if (requestCode == ACTIVITY_CHOOSE_FILE) {
            if (resultCode == RESULT_OK) {
                new ParseCSV(new File(data.getData().getPath())).execute();
            }
        }
    }

    @Override
    public void onRightClicked(int position) {
        if (CollectionUtils.isNotEmpty(productLists)) {
            final ProductLists productToRemove = productLists.get(position);
            productListsDao.delete(productToRemove);
            adapter.remove(productToRemove);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, adapter.getItemCount());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_lists, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_import_csv) {
            selectCSVFile();
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectCSVFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.open_csv)), ACTIVITY_CHOOSE_FILE);
    }

    @SuppressLint("StaticFieldLeak")
    class ParseCSV extends AsyncTask<Void, Integer, Boolean> {
        File file;
        ProgressDialog progressDialog;

        ParseCSV(File file) {
            this.file = file;
            progressDialog = new ProgressDialog(ProductListsActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progressDialog.dismiss();
            if (!aBoolean) {
                Toast.makeText(ProductListsActivity.this, getString(R.string.toast_import_csv_error), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProductListsActivity.this, getString(R.string.toast_import_csv), Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            YourListedProductDao yourListedProductDao = Utils.getAppDaoSession(ProductListsActivity.this).getYourListedProductDao();
            List<YourListedProduct> list = new ArrayList<>();

            try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(file)).withSkipLines(1).build()) {
                List<String[]> result = csvReader.readAll();
                int size = result.size();
                int count = 0;
                long id;
                for (String[] row : result) {
                    List<ProductLists> lists = productListsDao.queryBuilder().where(ProductListsDao.Properties.ListName.eq(row[2])).list();
                    if (lists.size() <= 0) {
                        //create new list
                        ProductLists productList = new ProductLists(row[2], 0);
                        productLists.add(productList);
                        productListsDao.insert(productList);
                        lists = productListsDao.queryBuilder().where(ProductListsDao.Properties.ListName.eq(row[2])).list();
                    }
                    id = lists.get(0).getId();

                    YourListedProduct yourListedProduct = new YourListedProduct();
                    yourListedProduct.setBarcode(row[0]);
                    yourListedProduct.setProductName(row[1]);
                    yourListedProduct.setListName(row[2]);
                    yourListedProduct.setProductDetails(row[3]);
                    yourListedProduct.setListId(id);
                    list.add(yourListedProduct);

                    count++;
                    publishProgress((int) ((float) count * 100 / (float) size));
                }
                yourListedProductDao.insertOrReplaceInTx(list);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
