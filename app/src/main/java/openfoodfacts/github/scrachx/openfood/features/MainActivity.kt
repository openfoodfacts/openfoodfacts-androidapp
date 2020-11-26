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
package openfoodfacts.github.scrachx.openfood.features

import android.Manifest
import android.app.SearchManager
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.*
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.ActivityMainBinding
import openfoodfacts.github.scrachx.openfood.features.LoginActivity.LoginContract
import openfoodfacts.github.scrachx.openfood.features.adapters.PhotosAdapter
import openfoodfacts.github.scrachx.openfood.features.additives.AdditiveListActivity
import openfoodfacts.github.scrachx.openfood.features.allergensalert.AllergensAlertFragment
import openfoodfacts.github.scrachx.openfood.features.categories.activity.CategoryActivity
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.install
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.productlists.ProductListsActivity
import openfoodfacts.github.scrachx.openfood.features.scan.ContinuousScanActivity
import openfoodfacts.github.scrachx.openfood.features.scanhistory.ScanHistoryActivity
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.features.searchbycode.SearchByCodeFragment
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.jobs.OfflineProductWorker.Companion.scheduleSync
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.onCreate
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.setLocale
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.*
import openfoodfacts.github.scrachx.openfood.utils.Utils.hideKeyboard
import openfoodfacts.github.scrachx.openfood.utils.Utils.isApplicationInstalled
import openfoodfacts.github.scrachx.openfood.utils.Utils.isHardwareCameraInstalled
import openfoodfacts.github.scrachx.openfood.utils.Utils.isNetworkConnected
import openfoodfacts.github.scrachx.openfood.utils.Utils.scheduleProductUploadJob
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

class MainActivity : BaseActivity(), NavigationDrawerListener {
    private var barcode: String? = null
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var contributeUri: Uri? = null
    private var customTabActivityHelper: CustomTabActivityHelper? = null

    /**
     * Used to re-create the fragment after activity recreation
     */
    private var customTabsIntent: CustomTabsIntent? = null
    private var discoverUri: Uri? = null
    private val disp = CompositeDisposable()
    private lateinit var drawerResult: Drawer
    private var headerResult: AccountHeader? = null
    private var prefManager: PrefManager? = null
    private var searchMenuItem: MenuItem? = null
    private var userAccountUri: Uri? = null
    private val loginThenUpdateLauncher = registerForActivityResult(LoginContract()) { isLoggedIn: Boolean ->
        if (isLoggedIn) {
            updateConnectedState()
        }
    }
    private var userContributeUri: Uri? = null
    private val loginThenContributionsLauncher = registerForActivityResult(
            LoginContract()) { isLoggedIn: Boolean ->
        if (isLoggedIn) {
            openMyContributionsInBrowser()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideKeyboard(this)
        val profile = userProfile
        setLocale(this, getLanguage(this))
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        swapToHomeFragment()

        // chrome custom tab init
        customTabActivityHelper = CustomTabActivityHelper()
        customTabActivityHelper!!.setConnectionCallback(object : CustomTabActivityHelper.ConnectionCallback {
            override fun onCustomTabsConnected() {}
            override fun onCustomTabsDisconnected() {}
        })
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(this,
                customTabActivityHelper!!.session)

        // Create the AccountHeader
        var accountHeaderBuilder = AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withTextColorRes(R.color.white)
                .addProfiles(profile)
                .withOnAccountHeaderProfileImageListener(object : AccountHeader.OnAccountHeaderProfileImageListener {
                    override fun onProfileImageClick(view: View, profile: IProfile<*>, current: Boolean): Boolean {
                        if (!isUserLoggedIn) {
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        }
                        return false
                    }

                    override fun onProfileImageLongClick(view: View, profile: IProfile<*>, current: Boolean): Boolean {
                        return false
                    }
                })
                .withOnAccountHeaderSelectionViewClickListener(object : AccountHeader.OnAccountHeaderSelectionViewClickListener {
                    override fun onClick(view: View, profile: IProfile<*>): Boolean {
                        if (!isUserLoggedIn) {
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        }
                        return false
                    }
                })
                .withSelectionListEnabledForSingleProfile(false)
                .withOnAccountHeaderListener(object : AccountHeader.OnAccountHeaderListener {
                    override fun onProfileChanged(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
                        if (profile is IDrawerItem<*> && profile.identifier == ITEM_MANAGE_ACCOUNT.toLong()) {
                            CustomTabActivityHelper.openCustomTab(this@MainActivity,
                                    customTabsIntent!!,
                                    userAccountUri!!,
                                    WebViewFallback())
                        }
                        return false
                    }
                })
                .withSavedInstance(savedInstanceState)
        accountHeaderBuilder = try {
            accountHeaderBuilder.withHeaderBackground(R.drawable.header)
        } catch (e: OutOfMemoryError) {
            Log.w(LOG_TAG, "Device has too low memory, loading color drawer header...", e)
            accountHeaderBuilder.withHeaderBackground(ColorDrawable(ContextCompat.getColor(this, R.color.primary_dark)))
        }
        headerResult = accountHeaderBuilder.build()

        // Add Manage Account profile if the user is connected
        val preferences = getSharedPreferences(PreferencesFragment.LOGIN_PREF, 0)
        val userSessionPrefs = preferences.getString("user_session", null)
        val isUserConnected = isUserLoggedIn && userSessionPrefs != null
        if (isUserConnected) {
            updateProfileForCurrentUser()
        }
        //Create the drawer
        drawerResult = DrawerBuilder()
                .withActivity(this)
                .withToolbar(binding.toolbarInclude.toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult!!) //set the AccountHeader we created earlier for the header
                .withOnDrawerListener(object : Drawer.OnDrawerListener {
                    override fun onDrawerOpened(drawerView: View) {
                        hideKeyboard(this@MainActivity)
                    }

                    override fun onDrawerClosed(drawerView: View) {}
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        hideKeyboard(this@MainActivity)
                    }
                })
                .addDrawerItems(
                        PrimaryDrawerItem().withName(R.string.home_drawer).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(ITEM_HOME.toLong()),
                        SectionDrawerItem().withName(R.string.search_drawer),
                        PrimaryDrawerItem().withName(R.string.search_by_barcode_drawer).withIcon(GoogleMaterial.Icon.gmd_dialpad).withIdentifier(ITEM_SEARCH_BY_CODE.toLong()),
                        PrimaryDrawerItem().withName(R.string.search_by_category).withIcon(GoogleMaterial.Icon.gmd_filter_list).withIdentifier(ITEM_CATEGORIES.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.additives).withIcon(R.drawable.ic_additives).withIdentifier(ITEM_ADDITIVES.toLong())
                                .withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.scan_search).withIcon(R.drawable.barcode_grey_24dp).withIdentifier(ITEM_SCAN.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.compare_products).withIcon(GoogleMaterial.Icon.gmd_swap_horiz).withIdentifier(ITEM_COMPARE.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.advanced_search_title).withIcon(GoogleMaterial.Icon.gmd_insert_chart).withIdentifier(ITEM_ADVANCED_SEARCH.toLong())
                                .withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.scan_history_drawer).withIcon(GoogleMaterial.Icon.gmd_history).withIdentifier(ITEM_HISTORY.toLong()).withSelectable(false),
                        SectionDrawerItem().withName(R.string.user_drawer).withIdentifier(USER_ID),
                        PrimaryDrawerItem().withName(getString(R.string.action_contributes)).withIcon(GoogleMaterial.Icon.gmd_rate_review).withIdentifier(ITEM_MY_CONTRIBUTIONS.toLong())
                                .withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.your_lists).withIcon(GoogleMaterial.Icon.gmd_list).withIdentifier(ITEM_YOUR_LISTS.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.products_to_be_completed).withIcon(GoogleMaterial.Icon.gmd_edit).withIdentifier(ITEM_INCOMPLETE_PRODUCTS.toLong())
                                .withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.alert_drawer).withIcon(GoogleMaterial.Icon.gmd_warning).withIdentifier(ITEM_ALERT.toLong()),
                        PrimaryDrawerItem().withName(R.string.action_preferences).withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(ITEM_PREFERENCES.toLong()),
                        DividerDrawerItem(),
                        PrimaryDrawerItem().withName(R.string.action_discover).withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(ITEM_ABOUT.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.contribute).withIcon(GoogleMaterial.Icon.gmd_group).withIdentifier(ITEM_CONTRIBUTE.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.open_other_flavor_drawer).withIcon(GoogleMaterial.Icon.gmd_shop).withIdentifier(ITEM_OBF.toLong()).withSelectable(false)
                )
                .withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
                    override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                        var newFragment: Fragment? = null
                        when (drawerItem.identifier.toInt()) {
                            ITEM_HOME -> newFragment = HomeFragment.newInstance()
                            ITEM_SEARCH_BY_CODE -> {
                                newFragment = SearchByCodeFragment()
                                selectNavigationItem(binding.bottomNavigationInclude.bottomNavigation, 0)
                            }
                            ITEM_CATEGORIES -> CategoryActivity.start(this@MainActivity)
                            ITEM_ADDITIVES -> AdditiveListActivity.start(this@MainActivity)
                            ITEM_SCAN -> openScan()
                            ITEM_COMPARE -> ProductCompareActivity.start(this@MainActivity)
                            ITEM_HISTORY -> ScanHistoryActivity.start(this@MainActivity)
                            ITEM_LOGIN -> loginThenUpdateLauncher.launch(null)
                            ITEM_ALERT -> newFragment = AllergensAlertFragment.newInstance()
                            ITEM_PREFERENCES -> newFragment = PreferencesFragment.newInstance()
                            ITEM_ABOUT -> CustomTabActivityHelper.openCustomTab(this@MainActivity, customTabsIntent!!, discoverUri!!, WebViewFallback())
                            ITEM_CONTRIBUTE -> CustomTabActivityHelper.openCustomTab(this@MainActivity, customTabsIntent!!, contributeUri!!, WebViewFallback())
                            ITEM_INCOMPLETE_PRODUCTS ->                         // Search and display the products to be completed by moving to ProductBrowsingListActivity
                                start(this@MainActivity, "", SearchType.INCOMPLETE_PRODUCT)
                            ITEM_OBF -> {
                                val otherOFAppInstalled = isApplicationInstalled(this@MainActivity, BuildConfig.OFOTHERLINKAPP)
                                if (otherOFAppInstalled) {
                                    val launchIntent = packageManager
                                            .getLaunchIntentForPackage(BuildConfig.OFOTHERLINKAPP)
                                    if (launchIntent != null) {
                                        startActivity(launchIntent)
                                    } else {
                                        Toast.makeText(this@MainActivity, R.string.app_disabled_text, Toast.LENGTH_SHORT).show()
                                        val intent = Intent()
                                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        val uri = Uri.fromParts("package", BuildConfig.OFOTHERLINKAPP, null)
                                        intent.data = uri
                                        startActivity(intent)
                                    }
                                } else {
                                    try {
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.OFOTHERLINKAPP)))
                                    } catch (anfe: ActivityNotFoundException) {
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" +
                                                BuildConfig.OFOTHERLINKAPP)))
                                    }
                                }
                            }
                            ITEM_ADVANCED_SEARCH -> {
                                val builder = CustomTabsIntent.Builder()
                                val customTabsIntent = builder.build()
                                CustomTabActivityHelper.openCustomTab(this@MainActivity, customTabsIntent, Uri.parse(getString(R.string.advanced_search_url)), WebViewFallback())
                            }
                            ITEM_MY_CONTRIBUTIONS -> openMyContributions()
                            ITEM_YOUR_LISTS -> ProductListsActivity.start(this@MainActivity)
                            ITEM_LOGOUT -> MaterialDialog.Builder(this@MainActivity)
                                    .title(R.string.confirm_logout)
                                    .content(R.string.logout_dialog_content)
                                    .positiveText(R.string.txtOk)
                                    .negativeText(R.string.dialog_cancel)
                                    .onPositive { _, _ -> logout() }
                                    .onNegative { dialog, _ ->
                                        dialog.dismiss()
                                        Snackbar.make(binding.root, "Cancelled", BaseTransientBottomBar.LENGTH_SHORT).show()
                                    }.show()
                            else -> {
                            }
                        }
                        if (newFragment != null) {
                            supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, newFragment)
                                    .addToBackStack(null)
                                    .commit()
                        }
                        return false
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(false)
                .build()
        drawerResult.actionBarDrawerToggle!!.isDrawerIndicatorEnabled = true

        // Add Drawer items for the connected user
        drawerResult.addItemsAtPosition(drawerResult.getPosition(ITEM_MY_CONTRIBUTIONS.toLong()), if (isUserLoggedIn) logoutDrawerItem else loginDrawerItem)
        when {
            isFlavors(AppFlavors.OBF) -> {
                drawerResult.removeItem(ITEM_ALERT.toLong())
                drawerResult.removeItem(ITEM_ADDITIVES.toLong())
                drawerResult.updateName(ITEM_OBF.toLong(), StringHolder(getString(R.string.open_other_flavor_drawer)))
            }
            isFlavors(AppFlavors.OPFF) -> {
                drawerResult.removeItem(ITEM_ALERT.toLong())
            }
            isFlavors(AppFlavors.OPF) -> {
                drawerResult.removeItem(ITEM_ALERT.toLong())
                drawerResult.removeItem(ITEM_ADDITIVES.toLong())
                drawerResult.removeItem(ITEM_ADVANCED_SEARCH.toLong())
            }
        }
        if (!isApplicationInstalled(this@MainActivity, BuildConfig.OFOTHERLINKAPP)) {
            drawerResult.updateName(ITEM_OBF.toLong(), StringHolder(getString(R.string.install) + " " + getString(R.string.open_other_flavor_drawer)))
        } else {
            drawerResult.updateName(ITEM_OBF.toLong(), StringHolder(getString(R.string.open_other_flavor_drawer)))
        }

        // Remove scan item if the device does not have a camera, for example, Chromebooks or
        // Fire devices
        if (!isHardwareCameraInstalled(this)) {
            drawerResult.removeItem(ITEM_SCAN.toLong())
        }

//        //if you have many different types of DrawerItems you can magically pre-cache those items
//        // to get a better scroll performance
//        //make sure to init the cache after the DrawerBuilder was created as this will first
//        // clear the cache to make sure no old elements are in
//        new RecyclerViewCacheUtil<IDrawerItem>().withCacheSize(2).apply(result.getRecyclerView(),
//            result.getDrawerItems());

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 1
            drawerResult.setSelection(ITEM_HOME.toLong(), false)

            //set the active profile
            headerResult!!.activeProfile = profile
        }
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        if (settings.getBoolean("startScan", false)) {
            val cameraIntent = Intent(this@MainActivity, ContinuousScanActivity::class.java)
            cameraIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(cameraIntent)
        }

        // prefetch uris
        contributeUri = Uri.parse(getString(R.string.website_contribute))
        discoverUri = Uri.parse(getString(R.string.website_discover))
        userContributeUri = Uri.parse(getString(R.string.website_contributor) + userLogin)
        customTabActivityHelper!!.mayLaunchUrl(contributeUri, null, null)
        customTabActivityHelper!!.mayLaunchUrl(discoverUri, null, null)
        customTabActivityHelper!!.mayLaunchUrl(userContributeUri, null, null)
        if (CONTRIBUTIONS_SHORTCUT == intent.action) {
            openMyContributions()
        }
        if (SCAN_SHORTCUT == intent.action) {
            openScan()
        }
        if (BARCODE_SHORTCUT == intent.action) {
            swapToSearchByCode()
        }

        //Scheduling background image upload job
        scheduleProductUploadJob(this)
        scheduleSync()

        //Adds nutriscore and quantity values in old history for schema 5 update
        val mSharedPref = applicationContext.getSharedPreferences("prefs", 0)
        val isOldHistoryDataSynced = mSharedPref.getBoolean("is_old_history_data_synced", false)
        if (!isOldHistoryDataSynced && isNetworkConnected(this)) {
            val apiClient = OpenFoodAPIClient(this)
            apiClient.syncOldHistory()
        }
        selectNavigationItem(binding.bottomNavigationInclude.bottomNavigation, 0)
        install(this, binding.bottomNavigationInclude.bottomNavigation)
        handleIntent(intent)
    }

    private fun swapToHomeFragment() {
        val fragmentManager = supportFragmentManager
        fragmentManager.addOnBackStackChangedListener {}
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        binding.toolbarInclude.toolbar.title = BuildConfig.APP_NAME
    }

    private fun openScan() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.CAMERA)) {
                MaterialDialog.Builder(this@MainActivity)
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .show().setOnDismissListener {
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA),
                                    Utils.MY_PERMISSIONS_REQUEST_CAMERA)
                        }
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), Utils.MY_PERMISSIONS_REQUEST_CAMERA)
            }
        } else {
            val intent = Intent(this@MainActivity, ContinuousScanActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun updateProfileForCurrentUser() {
        headerResult!!.updateProfile(userProfile)
        if (isUserLoggedIn) {
            if (headerResult!!.profiles != null && headerResult!!.profiles!!.size < 2) {
                headerResult!!.addProfiles(getProfileSettingDrawerItem())
            }
        } else {
            headerResult!!.removeProfileByIdentifier(ITEM_MANAGE_ACCOUNT.toLong())
        }
    }

    private fun openMyContributions() {
        if (isUserLoggedIn) {
            openMyContributionsInBrowser()
        } else {
            MaterialDialog.Builder(this@MainActivity)
                    .title(R.string.contribute)
                    .content(R.string.contribution_without_account)
                    .positiveText(R.string.create_account_button)
                    .neutralText(R.string.login_button)
                    .onPositive { _, _ -> CustomTabActivityHelper.openCustomTab(this@MainActivity, customTabsIntent!!, Uri.parse(getString(R.string.website) + "cgi/user.pl"), WebViewFallback()) }
                    .onNeutral { _, _ -> loginThenContributionsLauncher.launch(null) }
                    .show()
        }
    }

    private fun openMyContributionsInBrowser() {
        val userLogin = userLogin
        userContributeUri = Uri.parse(getString(R.string.website_contributor) + userLogin)
        start(this, userLogin, SearchType.CONTRIBUTOR)
    }

    private fun getProfileSettingDrawerItem(): IProfile<ProfileSettingDrawerItem> {
        val userLogin = userLogin
        val userSession = userSession
        userAccountUri = Uri.parse("${getString(R.string.website)}cgi/user.pl?type=edit&userid=$userLogin&user_id=$userLogin&user_session=$userSession")
        customTabActivityHelper!!.mayLaunchUrl(userAccountUri, null, null)
        return ProfileSettingDrawerItem()
                .withName(getString(R.string.action_manage_account))
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withIdentifier(ITEM_MANAGE_ACCOUNT.toLong())
                .withSelectable(false)
    }

    /**
     * Replace logout menu item by the login menu item
     * Change current user profile (Anonymous)
     * Remove all Account Header items
     * Remove user login info
     */
    private fun logout() {
        getSharedPreferences(PreferencesFragment.LOGIN_PREF, MODE_PRIVATE).edit().clear().apply()
        updateConnectedState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //add the values which need to be saved from the drawer to the bundle
        var newState = outState
        drawerResult.let {
            newState = it.saveInstanceState(newState)
        }
        //add the values which need to be saved from the accountHeader to the bundle
        headerResult?.let {
            newState = it.saveInstanceState(newState)
        }
        super.onSaveInstanceState(newState)
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the
        // activity
        if (drawerResult.isDrawerOpen) {
            drawerResult.closeDrawer()
        } else {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack(supportFragmentManager.getBackStackEntryAt(0).id, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                //recreate the activity onBackPressed
                recreate()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(onCreate(newBase))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.action_search).also {
            val searchView = it.actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    binding.bottomNavigationInclude.bottomNavigation.visibility = View.GONE
                } else {
                    binding.bottomNavigationInclude.bottomNavigation.visibility = View.VISIBLE
                }
            }
            it.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                    binding.bottomNavigationInclude.bottomNavigation.visibility = View.GONE
                    return true
                }

                override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                    return true
                }
            })
            if (intent.getBooleanExtra(PRODUCT_SEARCH_KEY, false)) {
                it.expandActionView()
            }
        }
        return true
    }

    private val logoutDrawerItem
        get() = PrimaryDrawerItem()
                .withName(getString(R.string.logout_drawer))
                .withIcon(GoogleMaterial.Icon.gmd_settings_power)
                .withIdentifier(ITEM_LOGOUT.toLong())
                .withSelectable(false)

    private val loginDrawerItem: PrimaryDrawerItem
        get() = PrimaryDrawerItem()
                .withName(R.string.sign_in_drawer)
                .withIcon(GoogleMaterial.Icon.gmd_account_circle)
                .withIdentifier(ITEM_LOGIN.toLong())
                .withSelectable(false)

    private val userProfile: ProfileDrawerItem
        get() = ProfileDrawerItem()
                .withName(getSharedPreferences(PreferencesFragment.LOGIN_PREF, 0)
                        .getString("user", resources.getString(R.string.txt_anonymous)))
                .withIcon(R.drawable.img_home)
                .withIdentifier(ITEM_USER.toLong())

    override fun onStart() {
        super.onStart()
        customTabActivityHelper!!.bindCustomTabsService(this)
        prefManager = PrefManager(this)
        if (isFlavors(AppFlavors.OFF)
                && isUserLoggedIn
                && !prefManager!!.isFirstTimeLaunch
                && !prefManager!!.userAskedToRate) {
            val firstTimeLaunchTime = prefManager!!.firstTimeLaunchTime
            // Check if it has been a week since first launch
            if (Calendar.getInstance().timeInMillis - firstTimeLaunchTime >= WEEK_IN_MS) {
                showFeedbackDialog()
            }
        }
    }

    /**
     * show dialog to ask the user to rate the app/give feedback
     */
    private fun showFeedbackDialog() {
        //dialog for rating the app on play store
        val rateDialog = MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.user_ask_rate_app)
                .positiveText(R.string.rate_app)
                .negativeText(R.string.no_thx)
                .onPositive { dialog, _ ->
                    //open app page in play store
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                    dialog.dismiss()
                }
                .onNegative { dialog, _ -> dialog.dismiss() }

        //dialog for giving feedback
        val feedbackDialog = MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.user_ask_show_feedback_form)
                .positiveText(R.string.txtOk)
                .negativeText(R.string.txtNo)
                .onPositive { dialog, _ ->
                    //show feedback form
                    CustomTabActivityHelper.openCustomTab(this@MainActivity,
                            customTabsIntent!!, Uri.parse(getString(R.string.feedback_form_url)), WebViewFallback())
                    dialog.dismiss()
                }
                .onNegative { dialog, _ -> dialog.dismiss() }
        MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.user_enjoying_app)
                .positiveText(R.string.txtYes)
                .onPositive { dialog, _ ->
                    prefManager!!.userAskedToRate = true
                    rateDialog.show()
                    dialog.dismiss()
                }
                .negativeText(R.string.txtNo)
                .onNegative { dialog, _ ->
                    prefManager!!.userAskedToRate = true
                    feedbackDialog.show()
                    dialog.dismiss()
                }
                .show()
    }

    override fun onStop() {
        super.onStop()
        customTabActivityHelper!!.unbindCustomTabsService(this)
    }

    override fun onDestroy() {
        customTabActivityHelper!!.setConnectionCallback(null)
        disp.dispose()
        _binding = null
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val type = intent.type
        if (Intent.ACTION_SEARCH == intent.action) {
            Log.e("INTENT", "start activity")
            val query = intent.getStringExtra(SearchManager.QUERY)
            //Saves the most recent queries and adds it to the list of suggestions
            val suggestions = SearchRecentSuggestions(this,
                    SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE)
            suggestions.saveRecentQuery(query, null)
            start(this, query, SearchType.SEARCH)
            if (searchMenuItem != null) {
                searchMenuItem!!.collapseActionView()
            }
        } else if (type != null && type.startsWith("image/")) {
            if (Intent.ACTION_SEND == intent.action) {
                handleSendImage(intent) // Handle single image being sent
            } else if (Intent.ACTION_SEND_MULTIPLE == intent.action) {
                handleSendMultipleImages(intent) // Handle multiple images being sent
            }
        }
    }

    /**
     * This moves the main activity to the barcode entry fragment.
     */
    private fun swapToSearchByCode() {
        changeFragment(SearchByCodeFragment(), resources.getString(R.string.search_by_barcode_drawer), ITEM_SEARCH_BY_CODE.toLong())
    }

    override fun setItemSelected(@NavigationDrawerType type: Int) {
        drawerResult.setSelection(type.toLong(), false)
    }

    public override fun onResume() {
        super.onResume()
        selectNavigationItem(binding.bottomNavigationInclude.bottomNavigation, R.id.home_page)

        // change drawer menu item from "install" to "open" when navigating back from play store.
        if (isApplicationInstalled(this@MainActivity, BuildConfig.OFOTHERLINKAPP)) {
            drawerResult.updateName(ITEM_OBF.toLong(), StringHolder(getString(R.string.open_other_flavor_drawer)))
            drawerResult.adapter.notifyDataSetChanged()
        }
        updateConnectedState()
    }

    private fun updateConnectedState() {
        updateProfileForCurrentUser()
        drawerResult.removeItem(ITEM_LOGIN.toLong())
        drawerResult.removeItem(ITEM_LOGOUT.toLong())
        drawerResult.addItemAtPosition(if (super.isUserLoggedIn()) logoutDrawerItem else loginDrawerItem, drawerResult.getPosition(ITEM_MY_CONTRIBUTIONS.toLong()))
    }

    private fun handleSendImage(intent: Intent) {
        val selectedImagesArray = ArrayList<Uri>()
        val selectedImage = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (selectedImage != null) {
            selectedImagesArray.add(selectedImage)
            chooseDialog(selectedImagesArray)
        }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        val selectedImagesArray = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
        if (selectedImagesArray != null) {
            selectedImagesArray.removeAll(setOf<Any?>(null))
            chooseDialog(selectedImagesArray)
        }
    }

    private fun chooseDialog(selectedImagesArray: ArrayList<Uri>) {
        disp.add(detectBarcodeInImages(selectedImagesArray).observeOn(AndroidSchedulers.mainThread())
                .subscribe { isBarCodePresent: Boolean ->
                    if (isBarCodePresent) {
                        createAlertDialog(false, barcode!!, selectedImagesArray)
                    } else {
                        createAlertDialog(true, "", selectedImagesArray)
                    }
                })
    }

    /**
     * IO / Computing intensive operation
     *
     * @param selectedImages
     */
    private fun detectBarcodeInImages(selectedImages: List<Uri>): Single<Boolean> {
        return Observable.fromIterable(selectedImages)
                .map { uri: Uri ->
                    var bMap: Bitmap? = null
                    try {
                        contentResolver.openInputStream(uri).use { imageStream -> bMap = BitmapFactory.decodeStream(imageStream) }
                    } catch (e: FileNotFoundException) {
                        Log.e(MainActivity::class.java.simpleName, "Could not resolve file from Uri $uri", e)
                    } catch (e: IOException) {
                        Log.e(MainActivity::class.java.simpleName, "IO error during bitmap stream decoding: " + e.message, e)
                    }
                    //decoding bitmap
                    if (bMap != null) {
                        val intArray = IntArray(bMap!!.width * bMap!!.height)
                        bMap!!.getPixels(intArray, 0, bMap!!.width, 0, 0, bMap!!.width, bMap!!.height)
                        val source: LuminanceSource = RGBLuminanceSource(bMap!!.width, bMap!!.height, intArray)
                        val bitmap = BinaryBitmap(HybridBinarizer(source))
                        val reader: Reader = MultiFormatReader()
                        try {
                            val decodeHints = EnumMap<DecodeHintType, Any?>(DecodeHintType::class.java)
                            decodeHints[DecodeHintType.TRY_HARDER] = java.lang.Boolean.TRUE
                            decodeHints[DecodeHintType.PURE_BARCODE] = java.lang.Boolean.TRUE
                            val decodedResult = reader.decode(bitmap, decodeHints)
                            if (decodedResult != null) {
                                barcode = decodedResult.text
                            }
                            if (barcode != null) {
                                return@map true
                            }
                        } catch (e: FormatException) {
                            Toast.makeText(applicationContext, getString(R.string.format_error), Toast.LENGTH_SHORT).show()
                            Log.e(MainActivity::class.java.simpleName, "Error decoding bitmap into barcode: " + e.message)
                        } catch (e: Exception) {
                            Log.e(MainActivity::class.java.simpleName, "Error decoding bitmap into barcode: " + e.message)
                        }
                    }
                    false
                }
                .filter { it }
                .first(false)
                .subscribeOn(Schedulers.computation())
    }

    private fun createAlertDialog(hasEditText: Boolean, barcode: String, uri: ArrayList<Uri>) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.alert_barcode, null)
        alertDialogBuilder.setView(dialogView)
        val barcodeEditText = dialogView.findViewById<EditText>(R.id.barcode)
        val productImages: RecyclerView = dialogView.findViewById(R.id.product_image)
        val layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false)
        productImages.layoutManager = layoutManager
        productImages.adapter = PhotosAdapter(uri)
        if (hasEditText) {
            barcodeEditText.visibility = View.VISIBLE
            alertDialogBuilder.setTitle(getString(R.string.no_barcode))
            alertDialogBuilder.setMessage(getString(R.string.enter_barcode))
        } else {
            alertDialogBuilder.setTitle(getString(R.string.code_detected))
            alertDialogBuilder.setMessage("$barcode\n${getString(R.string.do_you_want_to)}")
        }

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.txtYes) { dialog, _ ->
                    for (selected in uri) {
                        val api = OpenFoodAPIClient(this@MainActivity)
                        var image: ProductImage
                        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                        val activeNetwork = cm.activeNetworkInfo
                        val tempBarcode = if (hasEditText) {
                            barcodeEditText.text.toString()
                        } else {
                            barcode
                        }
                        if (tempBarcode.isNotEmpty()) {
                            dialog.cancel()
                            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
                                val imageFile = File(RealPathUtil.getRealPath(this@MainActivity, selected))
                                image = ProductImage(tempBarcode, ProductImageField.OTHER, imageFile)
                                disp.add(api.postImg(image).subscribe())
                            } else {
                                val pd = Product()
                                pd.code = tempBarcode
                                val st = ProductState()
                                st.product = pd
                                ProductEditActivity.start(this, st)
                            }
                        } else {
                            Toast.makeText(this@MainActivity, getString(R.string.sorry_msg), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton(R.string.txtNo
                ) { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    /**
     * Used to navigate Fragments which are children of `MainActivity`.
     * Use this method when the `Fragment` APPEARS in the `Drawer`.
     *
     * @param fragment The fragment class to display.
     * @param title The title that should be displayed on the top toolbar.
     * @param drawerName The fragment as it appears in the drawer. See [NavigationDrawerListener] for the value.
     * @author ross-holloway94
     * @see [Related Stack Overflow article](https://stackoverflow.com/questions/45138446/calling-fragment-from-recyclerview-adapter)
     *
     * @since 06/16/18
     */
    private fun changeFragment(fragment: Fragment, title: String?, drawerName: Long) {
        changeFragment(fragment, title)
        drawerResult.setSelection(drawerName)
    }

    /**
     * Used to navigate Fragments which are children of `MainActivity`.
     * Use this method when the `Fragment` DOES NOT APPEAR in the `Drawer`.
     *
     * @param fragment The fragment class to display.
     * @param title The title that should be displayed on the top toolbar.
     * @author ross-holloway94
     * @see [Related Stack Overflow article](https://stackoverflow.com/questions/45138446/calling-fragment-from-recyclerview-adapter)
     *
     * @since 06/16/18
     */
    @JvmOverloads
    fun changeFragment(fragment: Fragment, title: String? = null) {
        val backStateName = fragment.javaClass.name
        val manager = supportFragmentManager
        val fragmentPopped = manager.popBackStackImmediate(backStateName, 0)
        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
            val ft = manager.beginTransaction()
            ft.replace(R.id.fragment_container, fragment, backStateName)
            ft.addToBackStack(backStateName)
            ft.commit()
        }
        if (title != null) {
            supportActionBar?.title = title
        }
    }

    companion object {
        private const val USER_ID: Long = 500
        private const val CONTRIBUTIONS_SHORTCUT = "CONTRIBUTIONS"
        private const val SCAN_SHORTCUT = "SCAN"
        private const val BARCODE_SHORTCUT = "BARCODE"
        private const val WEEK_IN_MS = 60 * 60 * 24 * 7 * 1000
        const val PRODUCT_SEARCH_KEY = "product_search"
        private val LOG_TAG = MainActivity::class.simpleName!!
    }
}