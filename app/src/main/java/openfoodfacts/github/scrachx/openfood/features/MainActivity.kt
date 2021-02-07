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
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
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
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.ActivityMainBinding
import openfoodfacts.github.scrachx.openfood.features.LoginActivity.Companion.LoginContract
import openfoodfacts.github.scrachx.openfood.features.adapters.PhotosAdapter
import openfoodfacts.github.scrachx.openfood.features.additives.AdditiveListActivity
import openfoodfacts.github.scrachx.openfood.features.allergensalert.AllergensAlertFragment
import openfoodfacts.github.scrachx.openfood.features.categories.activity.CategoryActivity
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
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
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.onCreate
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.setLanguageInPrefs
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.*
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_ABOUT
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_ADDITIVES
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_ADVANCED_SEARCH
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_ALERT
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_CATEGORIES
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_COMPARE
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_CONTRIBUTE
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_HISTORY
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_HOME
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_INCOMPLETE_PRODUCTS
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_LOGIN
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_LOGOUT
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_MANAGE_ACCOUNT
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_MY_CONTRIBUTIONS
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_OBF
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_PREFERENCES
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_SCAN
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_SEARCH_BY_CODE
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_USER
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.Companion.ITEM_YOUR_LISTS
import openfoodfacts.github.scrachx.openfood.utils.Utils.hideKeyboard
import openfoodfacts.github.scrachx.openfood.utils.Utils.isApplicationInstalled
import openfoodfacts.github.scrachx.openfood.utils.Utils.isNetworkConnected
import openfoodfacts.github.scrachx.openfood.utils.Utils.scheduleProductUploadJob
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

class MainActivity : BaseActivity(), NavigationDrawerListener {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val disp = CompositeDisposable()

    private val contributeUri: Uri by lazy { Uri.parse(getString(R.string.website_contribute)) }
    private val discoverUri: Uri by lazy { Uri.parse(getString(R.string.website_discover)) }
    private fun getUserContributeUri(): Uri = Uri.parse(getString(R.string.website_contributor) + getUserLogin())

    /**
     * Used to re-create the fragment after activity recreation
     */
    private lateinit var customTabsIntent: CustomTabsIntent
    private lateinit var customTabActivityHelper: CustomTabActivityHelper
    private lateinit var drawerResult: Drawer
    private lateinit var headerResult: AccountHeader
    private lateinit var prefManager: PrefManager

    private var searchMenuItem: MenuItem? = null
    private var userSettingsURI: Uri? = null

    private val loginThenUpdate = registerForActivityResult(LoginContract())
    { isLoggedIn -> if (isLoggedIn) updateConnectedState() }
    private val loginThenOpenContributions = registerForActivityResult(LoginContract())
    { isLoggedIn -> if (isLoggedIn) openMyContributionsInSearchActivity() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideKeyboard(this)
        setLanguageInPrefs(this, getLanguage(this))
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        swapToHomeFragment()

        // chrome custom tab init
        customTabActivityHelper = CustomTabActivityHelper()
        customTabActivityHelper.connectionCallback = object : CustomTabActivityHelper.ConnectionCallback {
            override fun onCustomTabsConnected() = Unit
            override fun onCustomTabsDisconnected() = Unit
        }
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(this, customTabActivityHelper.session)

        // Create the AccountHeader
        val profile = getUserProfile()
        var accountHeaderBuilder = AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withTextColorRes(R.color.white)
                .addProfiles(profile)
                .withOnAccountHeaderProfileImageListener(object : AccountHeader.OnAccountHeaderProfileImageListener {
                    override fun onProfileImageClick(view: View, profile: IProfile<*>, current: Boolean): Boolean {
                        if (!isUserSet()) startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        return false
                    }

                    override fun onProfileImageLongClick(view: View, profile: IProfile<*>, current: Boolean) = false
                })
                .withOnAccountHeaderSelectionViewClickListener(object : AccountHeader.OnAccountHeaderSelectionViewClickListener {
                    override fun onClick(view: View, profile: IProfile<*>): Boolean {
                        if (!isUserSet()) startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        return false
                    }
                })
                .withSelectionListEnabledForSingleProfile(selectionListEnabledForSingleProfile = false)
                .withOnAccountHeaderListener(object : AccountHeader.OnAccountHeaderListener {
                    override fun onProfileChanged(view: View?, profile: IProfile<*>, current: Boolean): Boolean {
                        if (profile is IDrawerItem<*> && profile.identifier == ITEM_MANAGE_ACCOUNT.toLong()) {
                            CustomTabActivityHelper.openCustomTab(
                                    this@MainActivity,
                                    customTabsIntent,
                                    userSettingsURI!!,
                                    WebViewFallback()
                            )
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
        if (isUserSet() && getUserSession() != null) updateProfileForCurrentUser()

        //Create the drawer
        drawerResult = DrawerBuilder()
                .withActivity(this)
                .withToolbar(binding.toolbarInclude.toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .withOnDrawerListener(object : Drawer.OnDrawerListener {
                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) = hideKeyboard(this@MainActivity)
                    override fun onDrawerOpened(drawerView: View) = hideKeyboard(this@MainActivity)
                    override fun onDrawerClosed(drawerView: View) = Unit
                })
                .addDrawerItems(
                        PrimaryDrawerItem().withName(R.string.home_drawer).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(ITEM_HOME.toLong()),

                        SectionDrawerItem().withName(R.string.search_drawer),

                        PrimaryDrawerItem().withName(R.string.search_by_barcode_drawer).withIcon(GoogleMaterial.Icon.gmd_dialpad).withIdentifier(ITEM_SEARCH_BY_CODE.toLong()),
                        PrimaryDrawerItem().withName(R.string.search_by_category).withIcon(GoogleMaterial.Icon.gmd_filter_list).withIdentifier(ITEM_CATEGORIES.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.additives).withIcon(R.drawable.ic_additives).withIdentifier(ITEM_ADDITIVES.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.scan_search).withIcon(R.drawable.barcode_grey_24dp).withIdentifier(ITEM_SCAN.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.compare_products).withIcon(GoogleMaterial.Icon.gmd_swap_horiz).withIdentifier(ITEM_COMPARE.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.advanced_search_title).withIcon(GoogleMaterial.Icon.gmd_insert_chart).withIdentifier(ITEM_ADVANCED_SEARCH.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.scan_history_drawer).withIcon(GoogleMaterial.Icon.gmd_history).withIdentifier(ITEM_HISTORY.toLong()).withSelectable(false),

                        SectionDrawerItem().withName(R.string.user_drawer).withIdentifier(USER_ID),

                        PrimaryDrawerItem().withName(getString(R.string.action_contributes)).withIcon(GoogleMaterial.Icon.gmd_rate_review).withIdentifier(ITEM_MY_CONTRIBUTIONS.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.your_lists).withIcon(GoogleMaterial.Icon.gmd_list).withIdentifier(ITEM_YOUR_LISTS.toLong()).withSelectable(false),
                        PrimaryDrawerItem().withName(R.string.products_to_be_completed).withIcon(GoogleMaterial.Icon.gmd_edit).withIdentifier(ITEM_INCOMPLETE_PRODUCTS.toLong()).withSelectable(false),
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
                                binding.bottomNavigationInclude.bottomNavigation.selectNavigationItem(0)
                            }
                            ITEM_CATEGORIES -> CategoryActivity.start(this@MainActivity)
                            ITEM_ADDITIVES -> AdditiveListActivity.start(this@MainActivity)
                            ITEM_COMPARE -> ProductCompareActivity.start(this@MainActivity)
                            ITEM_HISTORY -> ScanHistoryActivity.start(this@MainActivity)
                            ITEM_SCAN -> openScan()
                            ITEM_LOGIN -> loginThenUpdate.launch(Unit)
                            ITEM_ALERT -> newFragment = AllergensAlertFragment.newInstance()
                            ITEM_PREFERENCES -> newFragment = PreferencesFragment.newInstance()
                            ITEM_ABOUT -> CustomTabActivityHelper.openCustomTab(this@MainActivity, customTabsIntent, discoverUri, WebViewFallback())
                            ITEM_CONTRIBUTE -> CustomTabActivityHelper.openCustomTab(this@MainActivity, customTabsIntent, contributeUri, WebViewFallback())
                            ITEM_INCOMPLETE_PRODUCTS -> start(this@MainActivity, SearchType.INCOMPLETE_PRODUCT, "") // Search and display the products to be completed by moving to ProductBrowsingListActivity
                            ITEM_OBF -> {
                                val otherOFAppInstalled = isApplicationInstalled(this@MainActivity, BuildConfig.OFOTHERLINKAPP)
                                if (otherOFAppInstalled) {
                                    val launchIntent = packageManager.getLaunchIntentForPackage(BuildConfig.OFOTHERLINKAPP)
                                    if (launchIntent != null) {
                                        startActivity(launchIntent)
                                    } else {
                                        Toast.makeText(this@MainActivity, R.string.app_disabled_text, Toast.LENGTH_SHORT).show()
                                        startActivity(Intent().apply {
                                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                            data = Uri.fromParts("package", BuildConfig.OFOTHERLINKAPP, null)

                                        })
                                    }
                                } else {
                                    try {
                                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.OFOTHERLINKAPP)))
                                    } catch (anfe: ActivityNotFoundException) {
                                        startActivity(Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.OFOTHERLINKAPP}")
                                        ))
                                    }
                                }
                            }
                            ITEM_ADVANCED_SEARCH -> {
                                CustomTabActivityHelper.openCustomTab(
                                        this@MainActivity,
                                        CustomTabsIntent.Builder().build(),
                                        Uri.parse(getString(R.string.advanced_search_url)),
                                        WebViewFallback()
                                )
                            }
                            ITEM_MY_CONTRIBUTIONS -> openMyContributions()
                            ITEM_YOUR_LISTS -> ProductListsActivity.start(this@MainActivity)
                            ITEM_LOGOUT -> MaterialDialog.Builder(this@MainActivity).run {
                                title(R.string.confirm_logout)
                                content(R.string.logout_dialog_content)
                                positiveText(R.string.txtOk)
                                negativeText(R.string.dialog_cancel)
                                onPositive { _, _ -> logout() }
                                onNegative { dialog, _ ->
                                    Snackbar.make(binding.root, "Cancelled", BaseTransientBottomBar.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                                show()
                            }

                        }
                        newFragment?.let {
                            supportFragmentManager.commit {
                                replace(R.id.fragment_container, it)
                                addToBackStack(null)
                            }
                        }
                        return false
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(false)
                .build()
        drawerResult.actionBarDrawerToggle!!.isDrawerIndicatorEnabled = true

        // Add Drawer items for the connected user
        drawerResult.addItemsAtPosition(drawerResult.getPosition(ITEM_MY_CONTRIBUTIONS.toLong()), if (isUserSet()) getLogoutDrawerItem() else getLoginDrawerItem())
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
            drawerResult.updateName(ITEM_OBF.toLong(), StringHolder("${getString(R.string.install)} ${getString(R.string.open_other_flavor_drawer)}"))
        } else {
            drawerResult.updateName(ITEM_OBF.toLong(), StringHolder(getString(R.string.open_other_flavor_drawer)))
        }

        // Remove scan item if the device does not have a camera, for example, Chromebooks or Fire devices
        if (!isHardwareCameraInstalled(this)) drawerResult.removeItem(ITEM_SCAN.toLong())

//        //if you have many different types of DrawerItems you can magically pre-cache those items
//        // to get a better scroll performance
//        //make sure to init the cache after the DrawerBuilder was created as this will first
//        // clear the cache to make sure no old elements are in
//        new RecyclerViewCacheUtil<IDrawerItem>().withCacheSize(2).apply(result.getRecyclerView(),
//            result.getDrawerItems());

        // Only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 1
            drawerResult.setSelection(ITEM_HOME.toLong(), false)

            //set the active profile
            headerResult.activeProfile = profile
        }
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        if (settings.getBoolean("startScan", false)) {
            startActivity(Intent(this@MainActivity, ContinuousScanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }

        // prefetch uris
        customTabActivityHelper.mayLaunchUrl(contributeUri, null, null)

        customTabActivityHelper.mayLaunchUrl(discoverUri, null, null)

        customTabActivityHelper.mayLaunchUrl(getUserContributeUri(), null, null)

        when (intent.action) {
            CONTRIBUTIONS_SHORTCUT -> openMyContributions()
            SCAN_SHORTCUT -> openScan()
            BARCODE_SHORTCUT -> swapToSearchByCode()
        }

        //Scheduling background image upload job
        scheduleProductUploadJob(this)
        scheduleSync()

        //Adds nutriscore and quantity values in old history for schema 5 update
        val mSharedPref = applicationContext.getSharedPreferences("prefs", 0)
        val isOldHistoryDataSynced = mSharedPref.getBoolean("is_old_history_data_synced", false)
        if (!isOldHistoryDataSynced && isNetworkConnected(this)) {
            OpenFoodAPIClient(this).syncOldHistory()
        }
        binding.bottomNavigationInclude.bottomNavigation.selectNavigationItem(0)
        binding.bottomNavigationInclude.bottomNavigation.installBottomNavigation(this)
        handleIntent(intent)
    }

    private fun swapToHomeFragment() {
        supportFragmentManager.addOnBackStackChangedListener {}
        supportFragmentManager.commit { replace(R.id.fragment_container, HomeFragment()) }
        binding.toolbarInclude.toolbar.title = BuildConfig.APP_NAME
    }

    private fun openScan() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.CAMERA)) {
                MaterialDialog.Builder(this@MainActivity).run {
                    title(R.string.action_about)
                    content(R.string.permission_camera)
                    neutralText(R.string.txtOk)
                    show().setOnDismissListener {
                        ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.CAMERA),
                                MY_PERMISSIONS_REQUEST_CAMERA
                        )
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            }
        } else {
            startActivity(Intent(this@MainActivity, ContinuousScanActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }
    }

    private fun updateProfileForCurrentUser() {
        headerResult.updateProfile(getUserProfile())
        if (isUserSet()) {
            if (headerResult.profiles != null && headerResult.profiles!!.size < 2) {
                headerResult.addProfiles(getProfileSettingDrawerItem())
            }
        } else {
            headerResult.removeProfileByIdentifier(ITEM_MANAGE_ACCOUNT.toLong())
        }
    }

    private fun openMyContributions() {
        if (isUserSet()) {
            openMyContributionsInSearchActivity()
        } else {
            MaterialDialog.Builder(this@MainActivity).run {
                title(R.string.contribute)
                content(R.string.contribution_without_account)
                positiveText(R.string.create_account_button)
                neutralText(R.string.login_button)
                onPositive { _, _ -> CustomTabActivityHelper.openCustomTab(this@MainActivity, customTabsIntent, Uri.parse(getString(R.string.website) + "cgi/user.pl"), WebViewFallback()) }
                onNeutral { _, _ -> loginThenOpenContributions.launch(Unit) }
                show()
            }

        }
    }

    private fun openMyContributionsInSearchActivity() {
        start(this, SearchType.CONTRIBUTOR, getUserLogin()!!)
    }

    private fun getProfileSettingDrawerItem(): IProfile<ProfileSettingDrawerItem> {
        val userLogin = getUserLogin()
        val userSession = getUserSession()
        userSettingsURI = Uri.parse("${getString(R.string.website)}cgi/user.pl?type=edit&userid=$userLogin&user_id=$userLogin&user_session=$userSession")
        customTabActivityHelper.mayLaunchUrl(userSettingsURI, null, null)
        return ProfileSettingDrawerItem().apply {
            withName(getString(R.string.action_manage_account))
            withIcon(GoogleMaterial.Icon.gmd_settings)
            withIdentifier(ITEM_MANAGE_ACCOUNT.toLong())
            withSelectable(false)
        }
    }

    /**
     * Replace logout menu item by the login menu item
     * Change current user profile (Anonymous)
     * Remove all Account Header items
     * Remove user login info
     */
    private fun logout() {
        getSharedPreferences(PreferencesFragment.LOGIN_PREF, MODE_PRIVATE).edit { clear() }
        updateConnectedState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //add the values which need to be saved from the drawer to the bundle
        var newState = outState
        drawerResult.let { newState = it.saveInstanceState(newState) }
        //add the values which need to be saved from the accountHeader to the bundle
        headerResult.let { newState = it.saveInstanceState(newState) }
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
                // Recreate the activity onBackPressed
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

                override fun onMenuItemActionCollapse(menuItem: MenuItem) = true
            })
            if (intent.getBooleanExtra(PRODUCT_SEARCH_KEY, false)) {
                it.expandActionView()
            }
        }
        return true
    }

    private fun getLogoutDrawerItem() = PrimaryDrawerItem()
            .withName(getString(R.string.logout_drawer))
            .withIcon(GoogleMaterial.Icon.gmd_settings_power)
            .withIdentifier(ITEM_LOGOUT.toLong())
            .withSelectable(false)

    private fun getLoginDrawerItem() = PrimaryDrawerItem()
            .withName(R.string.sign_in_drawer)
            .withIcon(GoogleMaterial.Icon.gmd_account_circle)
            .withIdentifier(ITEM_LOGIN.toLong())
            .withSelectable(false)

    private fun getUserProfile() = ProfileDrawerItem()
            .withName(getLoginPreferences().getString("user", resources.getString(R.string.txt_anonymous)))
            .withIcon(R.drawable.img_home)
            .withIdentifier(ITEM_USER.toLong())

    override fun onStart() {
        super.onStart()
        customTabActivityHelper.bindCustomTabsService(this)
        prefManager = PrefManager(this)
        if (isFlavors(AppFlavors.OFF)
                && isUserSet()
                && !prefManager.isFirstTimeLaunch
                && !prefManager.userAskedToRate) {
            val firstTimeLaunchTime = prefManager.firstTimeLaunchTime
            // Check if it has been a week since first launch
            if (System.currentTimeMillis() - firstTimeLaunchTime >= WEEK_IN_MS) {
                showFeedbackDialog()
            }
        }
    }

    /**
     * show dialog to ask the user to rate the app/give feedback
     */
    private fun showFeedbackDialog() {
        //dialog for rating the app on play store
        val rateDialog = MaterialDialog.Builder(this).apply {
            title(R.string.app_name)
            content(R.string.user_ask_rate_app)
            positiveText(R.string.rate_app)
            negativeText(R.string.no_thx)
            onPositive { dialog, _ ->
                //open app page in play store
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                dialog.dismiss()
            }
            onNegative { dialog, _ -> dialog.dismiss() }
        }

        //dialog for giving feedback
        val feedbackDialog = MaterialDialog.Builder(this).apply {
            title(R.string.app_name)
            content(R.string.user_ask_show_feedback_form)
            positiveText(R.string.txtOk)
            negativeText(R.string.txtNo)
            onPositive { dialog, _ ->
                //show feedback form
                CustomTabActivityHelper.openCustomTab(
                        this@MainActivity,
                        customTabsIntent,
                        Uri.parse(getString(R.string.feedback_form_url)),
                        WebViewFallback(),
                )
                dialog.dismiss()
            }
            onNegative { dialog, _ -> dialog.dismiss() }
        }


        MaterialDialog.Builder(this).run {
            title(R.string.app_name)
            content(R.string.user_enjoying_app)
            positiveText(R.string.txtYes)
            onPositive { dialog, _ ->
                prefManager.userAskedToRate = true
                rateDialog.show()
                dialog.dismiss()
            }
            negativeText(R.string.txtNo)
            onNegative { dialog, _ ->
                prefManager.userAskedToRate = true
                feedbackDialog.show()
                dialog.dismiss()
            }
            show()
        }

    }

    override fun onStop() {
        super.onStop()
        customTabActivityHelper.unbindCustomTabsService(this)
    }

    override fun onDestroy() {
        customTabActivityHelper.connectionCallback = null
        disp.dispose()
        _binding = null
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when {
            intent.action == Intent.ACTION_SEARCH -> {
                Log.e("INTENT", "start activity")
                val query = intent.getStringExtra(SearchManager.QUERY) as String
                // Saves the most recent queries and adds it to the list of suggestions
                val suggestions = SearchRecentSuggestions(
                        this,
                        SearchSuggestionProvider.AUTHORITY,
                        SearchSuggestionProvider.MODE
                )
                suggestions.saveRecentQuery(query, null)
                start(this, SearchType.SEARCH, query)

                searchMenuItem?.collapseActionView()
            }
            intent.type?.startsWith("image/") == true -> {
                when (intent.action) {
                    Intent.ACTION_SEND -> handleSendImage(intent) // Handle single image being sent
                    Intent.ACTION_SEND_MULTIPLE -> handleSendMultipleImages(intent) // Handle multiple images being sent
                }
            }
        }
    }

    /**
     * This moves the main activity to the barcode entry fragment.
     */
    private fun swapToSearchByCode() =
            changeFragment(SearchByCodeFragment(), ITEM_SEARCH_BY_CODE.toLong(), resources.getString(R.string.search_by_barcode_drawer))

    override fun setItemSelected(@NavigationDrawerType type: Int) = drawerResult.setSelection(type.toLong(), false)

    public override fun onResume() {
        super.onResume()
        binding.bottomNavigationInclude.bottomNavigation.selectNavigationItem(R.id.home_page)

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
        drawerResult.addItemAtPosition(
                if (this@MainActivity.isUserSet()) getLogoutDrawerItem()
                else getLoginDrawerItem(), drawerResult.getPosition(ITEM_MY_CONTRIBUTIONS.toLong())
        )
    }

    private fun handleSendImage(intent: Intent) {
        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { chooseDialog(listOf(it)) }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { chooseDialog(it.filterNotNull()) }
    }

    private fun chooseDialog(selectedImagesArray: List<Uri>) {
        detectBarcodeInImages(selectedImagesArray).observeOn(AndroidSchedulers.mainThread()).subscribe { barcodes ->
            if (barcodes.isNotEmpty()) {
                createAlertDialog(false, barcodes.first(), selectedImagesArray)
            } else {
                createAlertDialog(true, "", selectedImagesArray)
            }
        }.addTo(disp)
    }

    /**
     * IO / Computing intensive operation
     *
     * @param selectedImages
     */
    private fun detectBarcodeInImages(selectedImages: List<Uri>): Single<MutableList<String>> {
        return Observable.fromIterable(selectedImages).flatMapMaybe { uri ->
            var bMap: Bitmap? = null
            try {
                contentResolver.openInputStream(uri).use { bMap = BitmapFactory.decodeStream(it) }
            } catch (e: FileNotFoundException) {
                Log.e(MainActivity::class.java.simpleName, "Could not resolve file from Uri $uri", e)
            } catch (e: IOException) {
                Log.e(MainActivity::class.java.simpleName, "IO error during bitmap stream decoding: " + e.message, e)
            }
            // Decoding bitmap
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
                        return@flatMapMaybe Maybe.just(decodedResult.text)
                    }
                } catch (e: FormatException) {
                    Toast.makeText(this@MainActivity, getString(R.string.format_error), Toast.LENGTH_SHORT).show()
                    Log.e(MainActivity::class.simpleName, "Error decoding bitmap into barcode: ${e.message}")
                } catch (e: Exception) {
                    Log.e(MainActivity::class.simpleName, "Error decoding bitmap into barcode: ${e.message}")
                }
            }
            return@flatMapMaybe Maybe.empty()
        }.toList().subscribeOn(Schedulers.computation())
    }

    private fun createAlertDialog(hasEditText: Boolean, barcode: String, imgUris: List<Uri>) {
        val alertDialogBuilder = AlertDialog.Builder(this)

        val dialogView = layoutInflater.inflate(R.layout.alert_barcode, null)
        alertDialogBuilder.setView(dialogView)

        dialogView.findViewById<RecyclerView>(R.id.product_image).run {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = PhotosAdapter(imgUris)
        }

        val barcodeEditText = dialogView.findViewById<EditText>(R.id.barcode)
        if (hasEditText) {
            barcodeEditText.visibility = View.VISIBLE
            alertDialogBuilder.setTitle(getString(R.string.no_barcode))
            alertDialogBuilder.setMessage(getString(R.string.enter_barcode))
        } else {
            alertDialogBuilder.setTitle(getString(R.string.code_detected))
            alertDialogBuilder.setMessage("$barcode\n${getString(R.string.do_you_want_to)}")
        }

        // set dialog message
        alertDialogBuilder.run {
            setCancelable(false)
            setPositiveButton(R.string.txtYes) { dialog, _ ->
                val api = OpenFoodAPIClient(this@MainActivity)
                imgUris.forEach { selected ->
                    val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                    val activeNetwork = cm.activeNetworkInfo
                    val tempBarcode = if (hasEditText) barcodeEditText.text.toString() else barcode
                    if (tempBarcode.isNotEmpty()) {
                        dialog.dismiss()
                        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
                            val imageFile = File(RealPathUtil.getRealPath(this@MainActivity, selected))
                            val image = ProductImage(tempBarcode, ProductImageField.OTHER, imageFile)
                            api.postImg(image).subscribe().addTo(disp)
                        } else {
                            val product = Product().apply { code = tempBarcode }
                            ProductEditActivity.start(this@MainActivity, product)
                        }
                    } else {
                        Toast.makeText(this@MainActivity, getString(R.string.sorry_msg), Toast.LENGTH_LONG).show()
                    }
                }
            }
            setNegativeButton(R.string.txtNo) { dialog, _ -> dialog.cancel() }
            create()
            show()
        }

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
    private fun changeFragment(fragment: Fragment, drawerName: Long, title: String? = null) {
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
        val backStateName = fragment::class.simpleName
        val fragmentPopped = supportFragmentManager.popBackStackImmediate(backStateName, 0)
        if (!fragmentPopped && supportFragmentManager.findFragmentByTag(backStateName) == null) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, fragment, backStateName)
                addToBackStack(backStateName)
            }
        }
        title?.let { supportActionBar?.title = it }
    }

    companion object {
        private const val USER_ID: Long = 500
        private const val CONTRIBUTIONS_SHORTCUT = "CONTRIBUTIONS"
        private const val SCAN_SHORTCUT = "SCAN"
        private const val BARCODE_SHORTCUT = "BARCODE"
        private const val WEEK_IN_MS = 1000 * 60 * 60 * 24 * 7 // MS in S * S in M * M in H * H in D * D in W
        const val PRODUCT_SEARCH_KEY = "product_search"
        private val LOG_TAG = MainActivity::class.simpleName!!
    }
}