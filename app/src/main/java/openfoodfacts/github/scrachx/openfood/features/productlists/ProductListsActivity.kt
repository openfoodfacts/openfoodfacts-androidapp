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
package openfoodfacts.github.scrachx.openfood.features.productlists

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductListsBinding
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.listeners.RecyclerItemClickListener
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity.Companion.KEY_LIST_ID
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity.Companion.KEY_LIST_NAME
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity.Companion.KEY_PRODUCT_TO_ADD
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import openfoodfacts.github.scrachx.openfood.models.entities.ProductListsDao
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProduct
import openfoodfacts.github.scrachx.openfood.utils.SwipeController
import openfoodfacts.github.scrachx.openfood.utils.SwipeControllerActions
import openfoodfacts.github.scrachx.openfood.utils.Utils
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class ProductListsActivity : BaseActivity(), SwipeControllerActions {
    private lateinit var adapter: ProductListsAdapter
    private var _binding: ActivityProductListsBinding? = null
    private val binding get() = _binding!!
    private val disp = CompositeDisposable()
    private lateinit var productLists: MutableList<ProductLists>
    private lateinit var productListsDao: ProductListsDao
    override fun onDestroy() {
        disp.dispose()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductListsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setTitle(R.string.your_lists)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.bottomNavigation.bottomNavigation.installBottomNavigation(this)
        binding.fabAdd.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_plus_blue_24, 0, 0, 0)

        productListsDao = getProductListsDaoWithDefaultList(this)
        productLists = productListsDao.loadAll()

        adapter = ProductListsAdapter(this, productLists)

        binding.productListsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.productListsRecyclerView.adapter = adapter

        binding.tipBox.loadToolTip()

        val bundle = intent.extras
        if (bundle != null) {
            val productToAdd = bundle[KEY_PRODUCT] as Product?
            showCreateListDialog(productToAdd)
        }

        binding.productListsRecyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(this) { _, position ->
                    val id = productLists[position].id
                    val listName = productLists[position].listName
                    Intent(this, ProductListActivity::class.java).apply {
                        putExtra(KEY_LIST_ID, id)
                        putExtra(KEY_LIST_NAME, listName)
                        startActivityForResult(this, 1)
                    }
                }
        )
        val swipeController = SwipeController(this, this)
        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(binding.productListsRecyclerView)

        binding.fabAdd.setOnClickListener { showCreateListDialog() }
    }

    private fun showCreateListDialog(productToAdd: Product? = null) {
        MaterialDialog.Builder(this)
                .title(R.string.txt_create_new_list)
                .alwaysCallInputCallback()
                .input(R.string.create_new_list_list_name, R.string.empty, false) { dialog, listName ->
                    // validate if there is another list with the same name
                    val cannotAdd = checkListNameExist(listName.toString())

                    dialog.inputEditText?.error = if (cannotAdd) resources.getString(R.string.error_duplicate_listname) else null
                    dialog.getActionButton(DialogAction.POSITIVE).isEnabled = !cannotAdd
                }
                .positiveText(R.string.dialog_create)
                .negativeText(R.string.dialog_cancel)
                .onPositive { dialog, _ ->  // this enable to avoid dismissing dialog if list name already exist
                    Log.d("CreateListDialog", "Positive clicked")
                    val inputEditText = dialog.inputEditText!!
                    val listName = inputEditText.text.toString()
                    val productList = ProductLists(listName, if (productToAdd != null) 1 else 0)

                    productLists.add(productList)
                    productListsDao.insert(productList)

                    adapter.notifyDataSetChanged()

                    if (productToAdd != null) {
                        val id = productList.id
                        Intent(this@ProductListsActivity, ProductListActivity::class.java).apply {
                            putExtra(KEY_LIST_ID, id)
                            putExtra(KEY_LIST_NAME, listName)
                            putExtra(KEY_PRODUCT_TO_ADD, productToAdd)
                            startActivityForResult(this, 1)
                        }
                    } else {
                        dialog.dismiss()
                    }
                }.show()
    }

    /**
     * Check if listname already in products lists.
     */
    private fun checkListNameExist(listName: String) =
            productLists.firstOrNull { it.listName == listName } != null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data!!.extras!!.getBoolean("update")) {
            adapter.notifyDataSetChanged()
        } else if (requestCode == ACTIVITY_CHOOSE_FILE && resultCode == RESULT_OK) {
            try {
                val inputStream = contentResolver.openInputStream(data!!.data!!)
                parseCSV(inputStream)
            } catch (e: Exception) {
                Log.e(ProductListsActivity::class.simpleName, "Error importing CSV.", e)
            }
        }
    }

    override fun onRightClicked(position: Int) {
        if (!productLists.isNullOrEmpty()) {
            val productToRemove = productLists[position]
            productListsDao.delete(productToRemove)
            adapter.remove(productToRemove)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(position, adapter.itemCount)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_product_lists, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_import_csv -> selectCSVFile()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun selectCSVFile() {
        startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
        }, getString(R.string.open_csv)), ACTIVITY_CHOOSE_FILE)
    }

    public override fun onResume() {
        super.onResume()
        binding.bottomNavigation.bottomNavigation.selectNavigationItem(R.id.my_lists)
    }

    private fun parseCSV(inputStream: InputStream?) {
        val progressDialog = ProgressDialog(this@ProductListsActivity)
        progressDialog.show()
        Observable.create { emitter: ObservableEmitter<Int?> ->
            Single.fromCallable {
                val yourListedProductDao = Utils.daoSession.yourListedProductDao
                val list: MutableList<YourListedProduct> = ArrayList()
                try {
                    CSVParser(InputStreamReader(inputStream), CSVFormat.DEFAULT.withFirstRecordAsHeader()).use { csvParser ->
                        val result = csvParser.records
                        val size = result.size
                        var count = 0
                        var id: Long
                        for (record in result) {
                            var lists = productListsDao.queryBuilder().where(ProductListsDao.Properties.ListName.eq(record[2])).list()
                            if (lists.isEmpty()) {
                                //create new list
                                val productList = ProductLists(record[2], 0)
                                productLists.add(productList)
                                productListsDao.insert(productList)
                                lists = productListsDao.queryBuilder().where(ProductListsDao.Properties.ListName.eq(record[2])).list()
                            }
                            id = lists[0].id
                            val yourListedProduct = YourListedProduct().apply {
                                barcode = record[0]
                                productName = record[1]
                                listName = record[2]
                                productDetails = record[3]
                                listId = id
                            }
                            list.add(yourListedProduct)
                            count++
                            emitter.onNext((count.toFloat() * 100 / size.toFloat()).toInt())
                        }
                        yourListedProductDao.insertOrReplaceInTx(list)
                        return@fromCallable true
                    }
                } catch (e: Exception) {
                    Log.e("ParseCSV", e.message, e)
                    return@fromCallable false
                }
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe { success ->
                progressDialog.dismiss()
                if (success) {
                    Toast.makeText(this@ProductListsActivity, getString(R.string.toast_import_csv), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProductListsActivity, getString(R.string.toast_import_csv_error), Toast.LENGTH_SHORT).show()
                }
            }.addTo(disp)
        }.subscribe { value -> progressDialog.progress = value!! }.addTo(disp)
    }

    companion object {
        private const val ACTIVITY_CHOOSE_FILE = 123
        private const val KEY_PRODUCT = "product"

        @JvmStatic
        fun start(context: Context, productToAdd: Product?) = context.startActivity(
                Intent(context, ProductListsActivity::class.java).apply {
                    putExtra(KEY_PRODUCT, productToAdd)
                })

        @JvmStatic
        fun start(context: Context) = context.startActivity(Intent(context, ProductListsActivity::class.java))

        @JvmStatic
        fun getProductListsDaoWithDefaultList(context: Context): ProductListsDao {
            val productListsDao = Utils.daoSession.productListsDao
            if (productListsDao.loadAll().isEmpty()) {
                productListsDao.insert(ProductLists(context.getString(R.string.txt_eaten_products), 0))
                productListsDao.insert(ProductLists(context.getString(R.string.txt_products_to_buy), 0))
            }
            return productListsDao
        }
    }
}