package openfoodfacts.github.scrachx.openfood.features.search

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductBrowsingListBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.ProductsRecyclerViewAdapter
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.listeners.EndlessRecyclerViewScrollListener
import openfoodfacts.github.scrachx.openfood.features.listeners.RecyclerItemClickListener
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.Search
import openfoodfacts.github.scrachx.openfood.models.SearchInfo
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import java.text.NumberFormat
import java.util.*

class ProductSearchActivity : BaseActivity() {
    private var _binding: ActivityProductBrowsingListBinding? = null
    private val binding get() = _binding!!

    private lateinit var client: OpenFoodAPIClient
    private lateinit var mSearchInfo: SearchInfo
    private lateinit var adapter: ProductsRecyclerViewAdapter

    private var contributionType = 0
    private var disp = CompositeDisposable()
    private val lowBatteryMode by lazy { isDisableImageLoad() && isBatteryLevelLow() }

    /**
     * boolean to determine if image should be loaded or not
     */
    private var setupDone = false
    private var mCountProducts = 0
    private var pageAddress = 1

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(LocaleHelper.onCreate(newBase))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductBrowsingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // OnClick
        binding.buttonTryAgain.setOnClickListener { reloadSearch() }
        binding.addProduct.setOnClickListener { addProduct() }

        binding.textCountProduct.visibility = View.INVISIBLE

        // Get the search information (query, title, type) that we will use in this activity
        val extras = intent.extras
        if (extras != null) {
            mSearchInfo = extras.getParcelable(SEARCH_INFO) ?: SearchInfo.emptySearchInfo()
        } else if (Intent.ACTION_VIEW == intent.action) {
            // the user has entered the activity via a url
            val data = intent.data
            if (data != null) {
                // TODO: If we open advanced search from app it redirects here
                val paths = data.toString().split("/")
                mSearchInfo = SearchInfo.emptySearchInfo()

                if (paths[3] == "cgi" && paths[4].contains("search.pl")) {
                    mSearchInfo.searchTitle = data.getQueryParameter("search_terms") ?: ""
                    mSearchInfo.searchQuery = data.getQueryParameter("search_terms") ?: ""
                    mSearchInfo.searchType = SearchType.SEARCH
                } else {
                    mSearchInfo.searchTitle = paths[4]
                    mSearchInfo.searchQuery = paths[4]
                    mSearchInfo.searchType = SearchType.fromUrl(paths[3]) ?: SearchType.SEARCH
                }

            } else {
                Log.i(LOG_TAG, "No data was passed in with URL. Exiting.")
                finish()
            }
        } else {
            Log.e(LOG_TAG, "No data passed to the activity. Exiting.")
            finish()
        }
        newSearchQuery()

        binding.navigationBottom.bottomNavigation.selectNavigationItem(0)
        binding.navigationBottom.bottomNavigation.installBottomNavigation(this)
    }

    override fun onDestroy() {
        disp.dispose()
        _binding = null
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchMenuItem = menu.findItem(R.id.action_search)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mSearchInfo.searchQuery = query
                mSearchInfo.searchType = SearchType.SEARCH
                newSearchQuery()
                return true
            }

            override fun onQueryTextChange(newText: String) = true
        })
        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem) = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                supportActionBar?.title = ""
                finish()
                return true
            }
        })
        if (SearchType.CONTRIBUTOR == mSearchInfo.searchType) {
            menu.findItem(R.id.action_set_type).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_set_type -> {
                val contributionTypes = arrayOf(
                        getString(R.string.products_added),
                        getString(R.string.products_incomplete),
                        getString(R.string.product_pictures_contributed),
                        getString(R.string.picture_contributed_incomplete),
                        getString(R.string.product_info_added),
                        getString(R.string.product_info_tocomplete)
                )
                MaterialDialog.Builder(this).apply {
                    title(R.string.show_by)
                    items(*contributionTypes)
                    itemsCallback { _, _, position, _ ->
                        when (position) {
                            1, 2, 3, 4, 5 -> {
                                contributionType = position
                                newSearchQuery()
                            }
                            else -> {
                                contributionType = 0
                                newSearchQuery()
                            }
                        }
                    }
                }.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupHungerGames() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val actualCountryTag = sharedPref.getString(getString(R.string.pref_country_key), "")
        if ("" == actualCountryTag) {
            ProductRepository.getCountryByCC2OrWorld(LocaleHelper.getLocaleFromContext().country)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { it.tag }
                    .defaultIfEmpty("en:world")
                    .subscribe { setupUrlHungerGames(it) }
                    .addTo(disp)
        } else {
            setupUrlHungerGames(actualCountryTag)
        }
    }

    private fun setupUrlHungerGames(countryTag: String?) {
        val url = Uri.parse("https://hunger.openfoodfacts.org/questions?type=${mSearchInfo.searchType.url}&value_tag=${mSearchInfo.searchQuery}&country=$countryTag")
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        binding.btnHungerGames.visibility = View.VISIBLE
        binding.btnHungerGames.text = resources.getString(R.string.hunger_game_call_to_action, mSearchInfo.searchTitle)
        binding.btnHungerGames.setOnClickListener {
            CustomTabActivityHelper.openCustomTab(this, customTabsIntent, url, null)
        }
    }

    private fun newSearchQuery() {
        supportActionBar?.title = mSearchInfo.searchTitle
        when (mSearchInfo.searchType) {
            SearchType.BRAND -> {
                supportActionBar!!.setSubtitle(R.string.brand_string)
                setupHungerGames()
            }
            SearchType.LABEL -> {
                supportActionBar!!.subtitle = getString(R.string.label_string)
                setupHungerGames()
            }
            SearchType.CATEGORY -> {
                supportActionBar!!.subtitle = getString(R.string.category_string)
                setupHungerGames()
            }
            SearchType.COUNTRY -> supportActionBar!!.setSubtitle(R.string.country_string)
            SearchType.ORIGIN -> supportActionBar!!.setSubtitle(R.string.origin_of_ingredients)
            SearchType.MANUFACTURING_PLACE -> supportActionBar!!.setSubtitle(R.string.manufacturing_place)
            SearchType.ADDITIVE -> supportActionBar!!.setSubtitle(R.string.additive_string)
            SearchType.SEARCH -> supportActionBar!!.setSubtitle(R.string.search_string)
            SearchType.STORE -> supportActionBar!!.setSubtitle(R.string.store_subtitle)
            SearchType.PACKAGING -> supportActionBar!!.setSubtitle(R.string.packaging_subtitle)
            SearchType.CONTRIBUTOR -> supportActionBar!!.subtitle = getString(R.string.contributor_string)
            SearchType.ALLERGEN -> supportActionBar!!.subtitle = getString(R.string.allergen_string)
            SearchType.INCOMPLETE_PRODUCT -> supportActionBar!!.title = getString(R.string.products_to_be_completed)
            SearchType.STATE -> {
                // TODO: 26/07/2020 use resources
                supportActionBar!!.subtitle = "State"
            }
            else -> error("No match case found for ${mSearchInfo.searchType}")
        }
        client = OpenFoodAPIClient(this@ProductSearchActivity)
        binding.progressBar.visibility = View.VISIBLE
        reloadSearch()
    }

    private fun reloadSearch() {
        binding.offlineCloudLinearLayout.visibility = View.INVISIBLE
        binding.textCountProduct.visibility = View.INVISIBLE
        binding.noResultsLayout.visibility = View.INVISIBLE

        pageAddress = 1
        loadDataFromAPI()
    }

    /**
     * When no matching products are found in the database then noResultsLayout is displayed.
     * This method is called when the user clicks on the add photo button in the noResultsLayout.
     */
    private fun addProduct() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                MaterialDialog.Builder(this)
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .onNeutral { _, _ -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA) }
                        .show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            }
        } else {
            val intent = Intent(this, ContinuousScanActivity::class.java)
            startActivity(intent)
        }
    }

    fun loadDataFromAPI() {
        val searchQuery = mSearchInfo.searchQuery
        when (mSearchInfo.searchType) {
            SearchType.BRAND -> client.getProductsByBrand(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_brand_products)

            SearchType.COUNTRY -> client.getProductsByCountry(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_country_products)

            SearchType.ORIGIN -> client.getProductsByOrigin(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_country_products)

            SearchType.MANUFACTURING_PLACE -> client.getProductsByManufacturingPlace(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_country_products)

            SearchType.ADDITIVE -> client.getProductsByAdditive(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_additive_products)

            SearchType.STORE -> client.getProductsByStore(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_store_products)

            SearchType.PACKAGING -> client.getProductsByPackaging(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_packaging_products)

            SearchType.SEARCH -> {
                if (isBarcodeValid(searchQuery)) {
                    client.openProduct(searchQuery, this)
                } else {
                    client.searchProductsByName(searchQuery, pageAddress)
                            .startSearch(R.string.txt_no_matching_products, R.string.txt_broaden_search)
                }
            }
            SearchType.LABEL -> client.getProductsByLabel(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_label_products)

            SearchType.CATEGORY -> client.getProductsByCategory(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching__category_products)

            SearchType.ALLERGEN -> client.getProductsByAllergen(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_allergen_products)

            SearchType.CONTRIBUTOR -> loadDataForContributor(searchQuery)

            SearchType.STATE -> client.getProductsByStates(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_allergen_products)

            // Get Products to be completed data and input it to loadData function
            SearchType.INCOMPLETE_PRODUCT -> client.getIncompleteProducts(pageAddress)
                    .startSearch(R.string.txt_no_matching_incomplete_products)

            else -> Log.e("Products Browsing", "No match case found for " + mSearchInfo.searchType)
        }
    }


    private fun Single<Search>.startSearch(@StringRes noMatchMsg: Int, @StringRes extendedMsg: Int = -1) {
        observeOn(AndroidSchedulers.mainThread()).subscribe { search, throwable ->
            displaySearch(throwable == null, search, noMatchMsg, extendedMsg)
        }.addTo(disp)
    }

    private fun loadDataForContributor(searchQuery: String) {
        when (contributionType) {
            1 -> client.getToBeCompletedProductsByContributor(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_contributor_products)

            2 -> client.getPicturesContributedProducts(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_contributor_products)

            3 -> client.getPicturesContributedIncompleteProducts(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_contributor_products)

            4 -> client.getInfoAddedProducts(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_contributor_products)

            5 -> client.getInfoAddedIncompleteProductsSingle(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_contributor_products)

            else -> client.getProductsByContributor(searchQuery, pageAddress)
                    .startSearch(R.string.txt_no_matching_contributor_products)
        }
    }

    private fun showResponse(isResponseOk: Boolean, response: Search?) {
        if (isResponseOk && response != null) {
            showSuccessfulResponse(response)
        } else {
            showOfflineCloud()
        }
    }

    private fun showSuccessfulResponse(response: Search) {
        mCountProducts = response.count.toInt()
        if (pageAddress == 1) {
            val number = NumberFormat.getInstance(Locale.getDefault()).format(response.count.toLong())
            binding.textCountProduct.text = "${resources.getString(R.string.number_of_results)} $number"
            val products: MutableList<Product?> = response.products.toMutableList()
            if (products.size < mCountProducts) {
                products += null
            }
            if (setupDone) {
                adapter = ProductsRecyclerViewAdapter(products, lowBatteryMode, this)
                binding.productsRecyclerView.adapter = adapter
            }
            setUpRecyclerView(products)
        } else if (adapter.products.size - 1 < mCountProducts + 1) {
            val posStart = adapter.itemCount
            adapter.products.removeAt(adapter.itemCount - 1)
            adapter.products += response.products
            if (adapter.products.size < mCountProducts) {
                adapter.products += null
            }
            adapter.notifyItemRangeChanged(posStart - 1, adapter.products.size - 1)
        }
    }

    private fun showOfflineCloud() {
        binding.swipeRefresh.isRefreshing = false
        binding.textCountProduct.visibility = View.GONE
        binding.productsRecyclerView.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.INVISIBLE
        binding.offlineCloudLinearLayout.visibility = View.VISIBLE
    }

    /**
     * Shows UI indicating that no matching products were found.
     *
     * Called by [displaySearch].
     *
     * @param message message to display when there are no results for given search
     * @param extendedMessage additional message to display, -1 if no message is displayed
     */
    private fun showEmptyResponse(@StringRes message: Int, @StringRes extendedMessage: Int) {
        binding.swipeRefresh.isRefreshing = false

        binding.productsRecyclerView.visibility = View.INVISIBLE
        binding.progressBar.visibility = View.INVISIBLE
        binding.offlineCloudLinearLayout.visibility = View.INVISIBLE
        binding.textCountProduct.visibility = View.GONE

        binding.textNoResults.setText(message)
        if (extendedMessage != -1) binding.textExtendSearch.setText(extendedMessage)
        binding.noResultsLayout.bringToFront()
        binding.noResultsLayout.visibility = View.VISIBLE

    }

    /**
     * Loads the search results into the UI, otherwise shows UI indicating that no matching
     * products were found.
     *
     * @param isResponseSuccessful true if the search response was successful
     * @param response the search results
     * @param emptyMessage message to display if there are no results
     * @param extendedMessage extended message to display if there are no results
     */
    private fun displaySearch(
            isResponseSuccessful: Boolean,
            response: Search?,
            @StringRes emptyMessage: Int,
            @StringRes extendedMessage: Int = -1
    ) = if (response == null) {
        showResponse(isResponseSuccessful, null)
    } else {
        val count = try {
            response.count.toInt()
        } catch (e: NumberFormatException) {
            throw NumberFormatException("Cannot parse ${response.count}.")
        }
        if (!isResponseSuccessful || count != 0) {
            showResponse(isResponseSuccessful, response)
        } else {
            showEmptyResponse(emptyMessage, extendedMessage)
        }
    }

    private fun setUpRecyclerView(mProducts: MutableList<Product?>) {
        binding.swipeRefresh.isRefreshing = false

        binding.progressBar.visibility = View.INVISIBLE
        binding.offlineCloudLinearLayout.visibility = View.INVISIBLE

        binding.textCountProduct.visibility = View.VISIBLE
        binding.productsRecyclerView.visibility = View.VISIBLE

        if (!setupDone) {
            binding.productsRecyclerView.setHasFixedSize(true)
            val mLayoutManager = LinearLayoutManager(this@ProductSearchActivity, LinearLayoutManager.VERTICAL, false)
            binding.productsRecyclerView.layoutManager = mLayoutManager
            adapter = ProductsRecyclerViewAdapter(mProducts, lowBatteryMode, this)
            binding.productsRecyclerView.adapter = adapter
            val dividerItemDecoration = DividerItemDecoration(binding.productsRecyclerView.context, DividerItemDecoration.VERTICAL)
            binding.productsRecyclerView.addItemDecoration(dividerItemDecoration)

            // Retain an instance so that you can call `resetState()` for fresh searches
            // Adds the scroll listener to RecyclerView
            binding.productsRecyclerView.addOnScrollListener(object : EndlessRecyclerViewScrollListener(mLayoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
                    if (mProducts.size < mCountProducts) {
                        pageAddress = page
                        loadDataFromAPI()
                    }
                }
            })
            binding.productsRecyclerView.addOnItemTouchListener(RecyclerItemClickListener(this) { _, position ->
                val product = adapter.getProduct(position) ?: return@RecyclerItemClickListener
                val barcode = product.code
                if (Utils.isNetworkConnected(this)) {
                    client.openProduct(barcode, this)
                    try {
                        val viewWithFocus = currentFocus
                        if (viewWithFocus != null) {
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(viewWithFocus.windowToken, 0)
                        }
                    } catch (e: NullPointerException) {
                        Log.e(LOG_TAG, "addOnItemTouchListener", e)
                    }
                } else {
                    MaterialDialog.Builder(this@ProductSearchActivity).apply {
                        title(R.string.device_offline_dialog_title)
                        content(R.string.connectivity_check)
                        positiveText(R.string.txt_try_again)
                        onPositive { _, _ ->
                            if (Utils.isNetworkConnected(this@ProductSearchActivity)) {
                                client.openProduct(barcode, this@ProductSearchActivity)
                            } else {
                                Toast.makeText(this@ProductSearchActivity, R.string.device_offline_dialog_title, Toast.LENGTH_SHORT).show()
                            }
                        }
                        negativeText(R.string.dismiss)
                        onNegative { dialog, _ -> dialog.dismiss() }
                    }.show()
                }
                return@RecyclerItemClickListener
            })
            binding.swipeRefresh.setOnRefreshListener {
                mProducts.clear()
                adapter.notifyDataSetChanged()
                binding.textCountProduct.text = resources.getString(R.string.number_of_results)
                pageAddress = 1
                reloadSearch()
                if (binding.swipeRefresh.isRefreshing) {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
        setupDone = true
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = true
            pageAddress = 1
            reloadSearch()
        }
    }

    companion object {
        /**
         * Must be public to be visible by TakeScreenshotIncompleteProductsTest class.
         */
        const val SEARCH_INFO = "search_info"

        /**
         * Start a new [ProductSearchActivity] given a search information
         *
         * @param context the context to use to start this activity
         * @param searchQuery the search query
         * @param searchTitle the title used in the activity for this search query
         * @param type the type of search
         */
        @JvmStatic
        fun start(context: Context, type: SearchType, searchQuery: String, searchTitle: String = searchQuery) {
            start(context, SearchInfo(type, searchQuery, searchTitle))
        }

        /**
         * @see [start]
         */
        @JvmStatic
        private fun start(context: Context, searchInfo: SearchInfo) {
            context.startActivity(Intent(context, ProductSearchActivity::class.java).apply {
                putExtra(SEARCH_INFO, searchInfo)
            })
        }

        private val LOG_TAG: String = this::class.simpleName!!
    }
}
