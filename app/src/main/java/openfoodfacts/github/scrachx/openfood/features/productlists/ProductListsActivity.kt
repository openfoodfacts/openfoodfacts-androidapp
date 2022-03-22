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
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductListsBinding
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_PRODUCT
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity.Companion.KEY_LIST_ID
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity.Companion.KEY_LIST_NAME
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity.Companion.KEY_PRODUCT_TO_ADD
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.listeners.RecyclerItemClickListener
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProductDao
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists
import openfoodfacts.github.scrachx.openfood.models.entities.ProductListsDao
import openfoodfacts.github.scrachx.openfood.utils.SwipeController
import openfoodfacts.github.scrachx.openfood.utils.isEmpty
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject


@AndroidEntryPoint
class ProductListsActivity : BaseActivity(), SwipeController.Actions {
    private var _binding: ActivityProductListsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var daoSession: DaoSession

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    private lateinit var adapter: ProductListsAdapter
    private lateinit var productListsDao: ProductListsDao

    private val disp = CompositeDisposable()

    override fun onDestroy() {
        disp.dispose()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductListsBinding.inflate(layoutInflater)
        fixFabIcon()

        setContentView(binding.root)
        setTitle(R.string.your_lists)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.bottomNavigation.bottomNavigation.installBottomNavigation(this)

        binding.fabAdd.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_plus_blue_24, 0, 0, 0)

        // FIXME: remove runBlocking
        productListsDao = runBlocking { daoSession.getProductListsDaoWithDefaultList(this@ProductListsActivity) }
        val productLists = productListsDao.loadAll().toMutableList()

        adapter = ProductListsAdapter(this@ProductListsActivity, productLists)


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

    // On Android < 5, the drawableStart attribute in XML will cause a crash
    // That's why, it's instead done here in the code
    private fun fixFabIcon() {
        binding.fabAdd.setCompoundDrawablesRelative(
            ContextCompat.getDrawable(this, R.drawable.ic_plus_blue_24),
            null, null, null
        )
    }

    private fun showCreateListDialog(productToAdd: Product? = null) {
        val inputEditText = EditText(this).apply {
            setHint(R.string.create_new_list_list_name)
        }
        val view = FrameLayout(this).apply {
            val margin = resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)
            setPadding(margin, margin / 2, margin, margin / 2)
            addView(inputEditText)
        }
        val dialog = MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(R.string.txt_create_new_list)
            .setView(view)
            .setPositiveButton(R.string.dialog_create, null)
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            // this enable to avoid dismissing dialog if list name already exist
            val newListName = inputEditText.text?.toString()

            if (!newListName.isNullOrEmpty() && !checkListNameExist(newListName)) {
                inputEditText.error = null
                addNewListName(newListName, productToAdd)
                dialog.dismiss()
            } else {
                inputEditText.error = resources.getString(R.string.error_duplicate_listname)
            }
        }
    }

    private fun addNewListName(listName: String, productToAdd: Product?) {
        matomoAnalytics.trackEvent(AnalyticsEvent.ShoppingListCreated)
        val productList = ProductLists(listName, if (productToAdd != null) 1 else 0)

        adapter.lists.add(productList)
        productListsDao.insert(productList)

        adapter.notifyDataSetChanged()

        if (productToAdd != null) {
            val id = productList.id
            val intent = Intent(this@ProductListsActivity, ProductListActivity::class.java).apply {
                putExtra(KEY_LIST_ID, id)
                putExtra(KEY_LIST_NAME, listName)
                putExtra(KEY_PRODUCT_TO_ADD, productToAdd)
            }
            startActivityForResult(intent, 1)
        }
    }

    /**
     * Check if listname already in products lists.
     */
    private fun checkListNameExist(listName: String) =
        adapter.lists.firstOrNull { it.listName == listName } != null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data!!.extras!!.getBoolean("update")) {
            adapter.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private val chooseFileContract = registerForActivityResult(ActivityResultContracts.OpenDocument())
    { uri ->
        if (uri != null) {
            try {
                parseCSV(contentResolver.openInputStream(uri))
            } catch (e: Exception) {
                Log.e(ProductListsActivity::class.simpleName, "Error importing CSV.", e)
            }
        }
    }

    override fun onRightClicked(position: Int) {
        if (adapter.lists.isNullOrEmpty()) return
        val list = adapter.lists[position]

        // delete the product from YOUR_LISTED_PRODUCT_TABLE
        daoSession.listedProductDao.queryBuilder()
            .where(ListedProductDao.Properties.ListId.eq(list.id))
            .buildDelete()
            .executeDeleteWithoutDetachingEntities()
        daoSession.clear()

        productListsDao.delete(list)
        adapter.remove(list)
        adapter.notifyItemRangeChanged(position, adapter.itemCount)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_product_lists, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_import_csv -> {
            openCSVToImport()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun openCSVToImport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            chooseFileContract.launch(arrayOf("text/csv"))
        } else {
            Toast.makeText(this, "Feature disabled for your android version.", Toast.LENGTH_LONG).show()
        }
    }

    public override fun onResume() {
        super.onResume()
        binding.bottomNavigation.bottomNavigation.selectNavigationItem(R.id.my_lists)
    }

    private fun parseCSV(inputStream: InputStream?) {
        val progressDialog = ProgressDialog(this@ProductListsActivity)
        progressDialog.show()

        importStream(inputStream, progressDialog).let {
            lifecycleScope.launch { it.collect { value -> progressDialog.progress = value } }
        }
    }

    private fun importStream(inputStream: InputStream?, progressDialog: ProgressDialog) = flow {
        val listProductDao = daoSession.listedProductDao
        val list = mutableListOf<ListedProduct>()

        val success = try {
            CSVParser(
                InputStreamReader(inputStream),
                CSVFormat.DEFAULT.withFirstRecordAsHeader()
            ).use { parser ->
                val size = parser.records.size
                parser.records.withIndex().forEach { (index, record) ->
                    val listName = record[2]
                    var daoList = productListsDao.queryBuilder()
                        .where(ProductListsDao.Properties.ListName.eq(listName)).unique()
                    if (daoList == null) {
                        //create new list
                        val productList = ProductLists(listName, 0)
                        adapter.lists.add(productList)
                        productListsDao.insert(productList)
                        daoList = productListsDao.queryBuilder()
                            .where(ProductListsDao.Properties.ListName.eq(listName)).unique()
                    }
                    val yourListedProduct = ListedProduct().apply {
                        this.barcode = record[0]
                        this.productName = record[1]
                        this.listName = listName
                        this.productDetails = record[3]
                        this.listId = daoList.id
                    }
                    list += yourListedProduct
                    emit(index * 100 / size)
                }
                listProductDao.insertOrReplaceInTx(list)
                true
            }
        } catch (e: Exception) {
            Log.e("ParseCSV", e.message, e)
            false
        }

        progressDialog.dismiss()

        if (success) {
            Toast.makeText(this@ProductListsActivity, getString(R.string.toast_import_csv), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@ProductListsActivity, getString(R.string.toast_import_csv_error), Toast.LENGTH_SHORT).show()
        }
    }.flowOn(Dispatchers.IO)

    companion object {

        @JvmStatic
        fun start(context: Context, productToAdd: Product) = context.startActivity(
            Intent(context, ProductListsActivity::class.java).apply {
                putExtra(KEY_PRODUCT, productToAdd)
            })

        @JvmStatic
        fun start(context: Context) = context.startActivity(Intent(context, ProductListsActivity::class.java))

        suspend fun DaoSession.getProductListsDaoWithDefaultList(context: Context): ProductListsDao = withContext(Dispatchers.IO) {
            if (productListsDao.isEmpty()) {
                productListsDao.insertInTx(
                    ProductLists(context.getString(R.string.txt_eaten_products), 0),
                    ProductLists(context.getString(R.string.txt_products_to_buy), 0)
                )
            }
            return@withContext productListsDao
        }
    }
}
