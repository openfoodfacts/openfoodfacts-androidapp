/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.features.productlists;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductListsBinding;
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller;
import openfoodfacts.github.scrachx.openfood.features.listeners.RecyclerItemClickListener;
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity;
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists;
import openfoodfacts.github.scrachx.openfood.models.entities.ProductListsDao;
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProduct;
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProductDao;
import openfoodfacts.github.scrachx.openfood.utils.SwipeController;
import openfoodfacts.github.scrachx.openfood.utils.SwipeControllerActions;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public class ProductListsActivity extends BaseActivity implements SwipeControllerActions {
    private static final int ACTIVITY_CHOOSE_FILE = 123;
    private ProductListsAdapter adapter;
    private ActivityProductListsBinding binding;
    private CompositeDisposable disp = new CompositeDisposable();
    private List<ProductLists> productLists;
    private ProductListsDao productListsDao;

    public static void start(Context context) {
        Intent starter = new Intent(context, ProductListsActivity.class);
        context.startActivity(starter);
    }

    @NonNull
    public static ProductListsDao getProductListsDaoWithDefaultList(@NonNull Context context) {
        ProductListsDao productListsDao = Utils.getDaoSession().getProductListsDao();
        if (productListsDao.loadAll().isEmpty()) {
            ProductLists eatenList = new ProductLists(context.getString(R.string.txt_eaten_products), 0);
            productListsDao.insert(eatenList);
            ProductLists toBuyList = new ProductLists(context.getString(R.string.txt_products_to_buy), 0);
            productListsDao.insert(toBuyList);
        }
        return productListsDao;
    }

    @Override
    protected void onDestroy() {
        disp.dispose();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle(R.string.your_lists);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CommonBottomListenerInstaller.install(this, binding.bottomNavigation.bottomNavigation);
        binding.fabAdd.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_plus_blue_24, 0, 0, 0);

        productListsDao = getProductListsDaoWithDefaultList(this);
        productLists = productListsDao.loadAll();

        adapter = new ProductListsAdapter(this, productLists);
        binding.productListsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.productListsRecyclerView.setAdapter(adapter);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Product productToAdd = (Product) bundle.get("product");

            showCreateListDialog(productToAdd);
        }

        binding.productListsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(ProductListsActivity.this,
            ((view, position) -> {
                Long id = productLists.get(position).getId();
                String listName = productLists.get(position).getListName();
                Intent intent = new Intent(this, ProductListActivity.class);
                intent.putExtra("listId", id);
                intent.putExtra("listName", listName);
                startActivityForResult(intent, 1);
            }))
        );

        SwipeController swipeController = new SwipeController(this, ProductListsActivity.this);
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(binding.productListsRecyclerView);

        binding.fabAdd.setOnClickListener(view -> showCreateListDialog(null));
    }

    private void showCreateListDialog(@Nullable Product productToAdd) {
        new MaterialDialog.Builder(this)
            .title(R.string.txt_create_new_list)
            .alwaysCallInputCallback()
            .input(R.string.create_new_list_list_name, R.string.empty, false, (dialog, listName) -> {
                // validate if there is another list with the same name
                EditText inputField = dialog.getInputEditText();
                boolean isAlreadyIn = checkListNameExist(listName.toString());
                if (inputField != null) {
                    inputField.setError(isAlreadyIn ? getResources().getString(R.string.error_duplicate_listname) : null);
                }
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(!isAlreadyIn);
            })
            .positiveText(R.string.dialog_create)
            .negativeText(R.string.dialog_cancel)
            .onPositive((dialog, which) -> { // this enable to avoid dismissing dialog if list name already exist
                Log.d("CreateListDialog", "Positive clicked");
                final EditText inputEditText = dialog.getInputEditText();
                if (inputEditText == null) {
                    dialog.dismiss();
                    return;
                }
                final String listName = inputEditText.getText().toString();
                ProductLists productList = new ProductLists(listName, productToAdd != null ? 1 : 0);
                productLists.add(productList);
                productListsDao.insert(productList);
                if (productToAdd != null) {
                    Long id = productList.getId();
                    Intent intent = new Intent(ProductListsActivity.this, ProductListActivity.class);
                    intent.putExtra("listId", id);
                    intent.putExtra("listName", listName);
                    intent.putExtra("product", productToAdd);
                    startActivityForResult(intent, 1);
                } else {
                    dialog.dismiss();
                    adapter.notifyDataSetChanged();
                }
            }).show();
    }

    /**
     * Check if listname already in products lists.
     */
    private boolean checkListNameExist(String listName) {
        for (ProductLists productList : productLists) {
            if (productList.getListName().equals(listName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data.getExtras().getBoolean("update")) {
            adapter.notifyDataSetChanged();
        } else if (requestCode == ACTIVITY_CHOOSE_FILE && resultCode == RESULT_OK) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                parseCSV(inputStream);
            } catch (Exception e) {
                Log.e(ProductListsActivity.class.getSimpleName(), "Error importing CSV: " + e.getMessage());
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

    @Override
    public void onResume() {
        super.onResume();
        CommonBottomListenerInstaller.selectNavigationItem(binding.bottomNavigation.bottomNavigation, R.id.my_lists);
    }

    private void parseCSV(InputStream inputStream) {
        ProgressDialog progressDialog = new ProgressDialog(ProductListsActivity.this);
        progressDialog.show();
        disp.add(Observable.create((ObservableEmitter<Integer> emitter) ->
            disp.add(Single.fromCallable(() -> {
                YourListedProductDao yourListedProductDao = Utils.getDaoSession().getYourListedProductDao();
                List<YourListedProduct> list = new ArrayList<>();

                try (CSVParser csvParser = new CSVParser(new InputStreamReader(inputStream), CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                    List<CSVRecord> result = csvParser.getRecords();
                    int size = result.size();
                    int count = 0;
                    long id;
                    for (CSVRecord record : result) {
                        List<ProductLists> lists = productListsDao.queryBuilder().where(ProductListsDao.Properties.ListName.eq(record.get(2))).list();
                        if (lists.isEmpty()) {
                            //create new list
                            ProductLists productList = new ProductLists(record.get(2), 0);
                            productLists.add(productList);
                            productListsDao.insert(productList);
                            lists = productListsDao.queryBuilder().where(ProductListsDao.Properties.ListName.eq(record.get(2))).list();
                        }
                        id = lists.get(0).getId();

                        YourListedProduct yourListedProduct = new YourListedProduct();
                        yourListedProduct.setBarcode(record.get(0));
                        yourListedProduct.setProductName(record.get(1));
                        yourListedProduct.setListName(record.get(2));
                        yourListedProduct.setProductDetails(record.get(3));
                        yourListedProduct.setListId(id);
                        list.add(yourListedProduct);

                        count++;
                        emitter.onNext(((int) ((float) count * 100 / (float) size)));
                    }
                    yourListedProductDao.insertOrReplaceInTx(list);
                    return true;
                } catch (Exception e) {
                    Log.e("ParseCSV", e.getMessage(), e);
                    return false;
                }
            })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(success -> {
                    progressDialog.dismiss();
                    if (success) {
                        Toast.makeText(ProductListsActivity.this, getString(R.string.toast_import_csv), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductListsActivity.this, getString(R.string.toast_import_csv_error), Toast.LENGTH_SHORT).show();
                    }
                }))).subscribe(progressDialog::setProgress));
    }
}
