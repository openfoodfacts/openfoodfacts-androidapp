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
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
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
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.ActivityMainBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.PhotosAdapter
import openfoodfacts.github.scrachx.openfood.features.additives.AdditiveListActivity
import openfoodfacts.github.scrachx.openfood.features.allergensalert.AllergensAlertFragment
import openfoodfacts.github.scrachx.openfood.features.categories.activity.CategoryActivity
import openfoodfacts.github.scrachx.openfood.features.changelog.ChangelogDialog
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity
import openfoodfacts.github.scrachx.openfood.features.home.HomeFragment
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity.Companion.LoginContract
import openfoodfacts.github.scrachx.openfood.features.preferences.PreferencesFragment
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.productlists.ProductListsActivity
import openfoodfacts.github.scrachx.openfood.features.scanhistory.ScanHistoryActivity
import openfoodfacts.github.scrachx.openfood.features.searchbycode.SearchByCodeFragment
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.jobs.ProductUploaderWorker.Companion.scheduleProductUpload
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.utils.*
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
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType
import openfoodfacts.github.scrachx.openfood.utils.Utils.scheduleProductUploadJob
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start as startSearch

@AndroidEntryPoint
class MainActivity : BaseActivity(), NavigationDrawerListener {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    @Inject
    lateinit var prefManager: PreferencesService

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


    private var searchMenuItem: MenuItem? = null
    private var userSettingsURI: Uri? = null

    private var historySyncJob: Job? = null


    private val loginThenUpdate = registerForActivityResult(LoginContract())
    { isLoggedIn -> if (isLoggedIn) updateConnectedState() }
    private val loginThenOpenContributions = registerForActivityResult(LoginContract())
    { isLoggedIn -> if (isLoggedIn) openMyContributionsInSearchActivity() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Are we sure we want to keep this?
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideKeyboard()
        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        swapToFragment(HomeFragment.newInstance())

        // chrome custom tab init
        customTabActivityHelper = CustomTabActivityHelper()
        customTabActivityHelper.connectionCallback = object : CustomTabActivityHelper.ConnectionCallback {
            override fun onCustomTabsConnected() = Unit
            override fun onCustomTabsDisconnected() = Unit
        }
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(this, customTabActivityHelper.session)

        // Create the AccountHeader
        val profile = getUserProfile()

        headerResult = buildAccountHeader {
            withActivity(this@MainActivity)
            withTranslucentStatusBar(true)
            withTextColorRes(R.color.white)
            addProfiles(profile)
            withOnAccountHeaderProfileImageListener(object : AccountHeader.OnAccountHeaderProfileImageListener {
                override fun onProfileImageClick(view: View, profile: IProfile<*>, current: Boolean): Boolean {
                    if (!isUserSet()) startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    return false
                }

                override fun onProfileImageLongClick(view: View, profile: IProfile<*>, current: Boolean) = false
            })
            withOnAccountHeaderSelectionViewClickListener(object : AccountHeader.OnAccountHeaderSelectionViewClickListener {
                override fun onClick(view: View, profile: IProfile<*>): Boolean {
                    if (!isUserSet()) startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    return false
                }
            })
            withSelectionListEnabledForSingleProfile(false)
            withOnAccountHeaderListener(object : AccountHeader.OnAccountHeaderListener {
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
            withSavedInstance(savedInstanceState)

            try {
                withHeaderBackground(R.drawable.header)
            } catch (e: OutOfMemoryError) {
                Log.w(LOG_TAG, "Device has too low memory, loading color drawer header...", e)
                withHeaderBackground(ColorDrawable(ContextCompat.getColor(this@MainActivity, R.color.primary_dark)))
            }
        }

        // Add Manage Account profile if the user is connected
        if (isUserSet() && getUserSession() != null) updateProfileForCurrentUser()

        // Create the drawer
        drawerResult = setupDrawer(savedInstanceState)
        drawerResult.actionBarDrawerToggle!!.isDrawerIndicatorEnabled = true

        // Add Drawer items for the connected user
        drawerResult.addItemsAtPosition(
            drawerResult.getPosition(ITEM_MY_CONTRIBUTIONS.toLong()),
            if (isUserSet()) getLogoutDrawerItem() else getLoginDrawerItem()
        )
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

        // FIXME: When set we cannot go to home fragment from bottom bar
        if (sharedPreferences.getBoolean("startScan", false)) startScanActivity()

        // prefetch uris
        customTabActivityHelper.mayLaunchUrl(contributeUri, null, null)

        customTabActivityHelper.mayLaunchUrl(discoverUri, null, null)

        customTabActivityHelper.mayLaunchUrl(getUserContributeUri(), null, null)

        when (intent.action) {
            CONTRIBUTIONS_SHORTCUT -> openMyContributions()
            SCAN_SHORTCUT -> checkThenStartScanActivity()
            BARCODE_SHORTCUT -> swapToSearchByCode()
        }

        //Scheduling background image upload job
        scheduleProductUploadJob(this)
        scheduleProductUpload(this, sharedPreferences)

        // Adds nutriscore and quantity values in old history for schema 5 update

        val isOldHistoryDataSynced = getAppPreferences().getBoolean("is_old_history_data_synced", false)
        if (!isOldHistoryDataSynced && isNetworkConnected()) {
            historySyncJob?.cancel()
            historySyncJob = viewModel.syncOldHistory()
        }

        binding.bottomNavigationInclude.bottomNavigation.selectNavigationItem(0)
        binding.bottomNavigationInclude.bottomNavigation.installBottomNavigation(this)

        handleIntent(intent)

        if (isFlavors(OFF)) {
            ChangelogDialog.newInstance(BuildConfig.DEBUG).presentAutomatically(this)
        }
    }


    private fun setupDrawer(savedInstanceState: Bundle?) = buildDrawer(this) {
        withToolbar(binding.toolbarInclude.toolbar)
        withHasStableIds(true)
        withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header

        withOnDrawerListener(object : Drawer.OnDrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) = hideKeyboard()
            override fun onDrawerOpened(drawerView: View) = hideKeyboard()
            override fun onDrawerClosed(drawerView: View) = Unit
        })

        addDrawerItems(

            primaryItem {
                withName(R.string.home_drawer)
                withIcon(GoogleMaterial.Icon.gmd_home)
                withIdentifier(ITEM_HOME.toLong())
            },

            sectionItem { withName(R.string.search_drawer) },
            primaryItem {
                withName(R.string.search_by_barcode_drawer)
                withIcon(GoogleMaterial.Icon.gmd_dialpad)
                withIdentifier(ITEM_SEARCH_BY_CODE.toLong())
            },
            primaryItem {
                withName(R.string.search_by_category)
                withIcon(GoogleMaterial.Icon.gmd_filter_list)
                withIdentifier(ITEM_CATEGORIES.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.additives)
                withIcon(R.drawable.ic_additives).withIdentifier(ITEM_ADDITIVES.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.scan_search)
                withIcon(R.drawable.barcode_grey_24dp)
                withIdentifier(ITEM_SCAN.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.compare_products)
                withIcon(GoogleMaterial.Icon.gmd_swap_horiz)
                withIdentifier(ITEM_COMPARE.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.advanced_search_title)
                withIcon(GoogleMaterial.Icon.gmd_insert_chart)
                withIdentifier(ITEM_ADVANCED_SEARCH.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.scan_history_drawer)
                withIcon(GoogleMaterial.Icon.gmd_history)
                withIdentifier(ITEM_HISTORY.toLong())
                withSelectable(false)
            },

            sectionItem { withName(R.string.user_drawer).withIdentifier(USER_ID) },
            primaryItem {
                withName(getString(R.string.action_contributes))
                withIcon(GoogleMaterial.Icon.gmd_rate_review)
                withIdentifier(ITEM_MY_CONTRIBUTIONS.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.your_lists)
                withIcon(GoogleMaterial.Icon.gmd_list)
                withIdentifier(ITEM_YOUR_LISTS.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.products_to_be_completed)
                withIcon(GoogleMaterial.Icon.gmd_edit)
                withIdentifier(ITEM_INCOMPLETE_PRODUCTS.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.alert_drawer)
                withIcon(GoogleMaterial.Icon.gmd_warning)
                withIdentifier(ITEM_ALERT.toLong())
            },
            primaryItem {
                withName(R.string.action_preferences)
                withIcon(GoogleMaterial.Icon.gmd_settings)
                withIdentifier(ITEM_PREFERENCES.toLong())
            },

            dividerItem(),

            primaryItem {
                withName(R.string.action_discover)
                withIcon(GoogleMaterial.Icon.gmd_info)
                withIdentifier(ITEM_ABOUT.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.contribute)
                withIcon(GoogleMaterial.Icon.gmd_group)
                withIdentifier(ITEM_CONTRIBUTE.toLong())
                withSelectable(false)
            },
            primaryItem {
                withName(R.string.open_other_flavor_drawer)
                withIcon(GoogleMaterial.Icon.gmd_shop)
                withIdentifier(ITEM_OBF.toLong())
                withSelectable(false)
            }
        )
        withOnDrawerItemClickListener(object : Drawer.OnDrawerItemClickListener {
            override fun onItemClick(view: View?, position: Int, drawerItem: IDrawerItem<*>): Boolean {
                var newFragment: Fragment? = null
                when (drawerItem.identifier.toInt()) {
                    ITEM_HOME -> newFragment = HomeFragment.newInstance()
                    ITEM_SEARCH_BY_CODE -> {
                        newFragment = SearchByCodeFragment.newInstance()
                        binding.bottomNavigationInclude.bottomNavigation.selectNavigationItem(0)
                    }
                    ITEM_CATEGORIES -> CategoryActivity.start(this@MainActivity)
                    ITEM_ADDITIVES -> AdditiveListActivity.start(this@MainActivity)
                    ITEM_COMPARE -> ProductCompareActivity.start(this@MainActivity)
                    ITEM_HISTORY -> ScanHistoryActivity.start(this@MainActivity)
                    ITEM_SCAN -> checkThenStartScanActivity()
                    ITEM_LOGIN -> loginThenUpdate.launch(Unit)
                    ITEM_ALERT -> newFragment = AllergensAlertFragment.newInstance()
                    ITEM_PREFERENCES -> newFragment = PreferencesFragment.newInstance()
                    ITEM_ABOUT -> CustomTabActivityHelper.openCustomTab(this@MainActivity, customTabsIntent, discoverUri, WebViewFallback())
                    ITEM_CONTRIBUTE -> CustomTabActivityHelper.openCustomTab(this@MainActivity, customTabsIntent, contributeUri, WebViewFallback())
                    ITEM_INCOMPLETE_PRODUCTS -> startSearch(
                        this@MainActivity,
                        SearchType.INCOMPLETE_PRODUCT,
                        ""
                    ) // Search and display the products to be completed by moving to ProductBrowsingListActivity
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
                                    data = Uri.fromParts(
                                        "package",
                                        BuildConfig.OFOTHERLINKAPP,
                                        null
                                    )
                                })
                            }
                        } else {
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=${BuildConfig.OFOTHERLINKAPP}".toUri()))
                            } catch (anfe: ActivityNotFoundException) {
                                startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        "https://play.google.com/store/apps/details?id=${BuildConfig.OFOTHERLINKAPP}".toUri()
                                    )
                                )
                            }
                        }
                    }
                    ITEM_ADVANCED_SEARCH -> {
                        CustomTabActivityHelper.openCustomTab(
                            this@MainActivity,
                            CustomTabsIntent.Builder().build(),
                            getString(R.string.advanced_search_url).toUri(),
                            WebViewFallback()
                        )
                    }
                    ITEM_MY_CONTRIBUTIONS -> openMyContributions()
                    ITEM_YOUR_LISTS -> ProductListsActivity.start(this@MainActivity)
                    ITEM_LOGOUT -> MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle(R.string.confirm_logout)
                        .setMessage(R.string.logout_dialog_content)
                        .setPositiveButton(android.R.string.ok) { _, _ -> logout() }
                        .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
                        .show()
                }
                newFragment?.let(::swapToFragment)
                return false
            }
        })


        withSavedInstance(savedInstanceState)
        withShowDrawerOnFirstLaunch(false)
    }


    private fun swapToFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.fragments.lastOrNull()
        if (currentFragment == null || currentFragment::class.java != fragment::class.java) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container, fragment)
                addToBackStack(null)
            }
        }
        binding.toolbarInclude.toolbar.title = BuildConfig.APP_NAME
    }

    private fun checkThenStartScanActivity() {
        when {
            checkSelfPermission(this, Manifest.permission.CAMERA) == PERMISSION_GRANTED -> {
                startScanActivity()
            }
            shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.action_about)
                    .setMessage(R.string.permission_camera)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        requestCameraThenOpenScan.launch(Manifest.permission.CAMERA)
                    }
                    .show()
            }
            else -> {
                requestCameraThenOpenScan.launch(Manifest.permission.CAMERA)
            }
        }

    }

    private fun updateProfileForCurrentUser() {
        headerResult.updateProfile(getUserProfile())
        if (!isUserSet()) {
            headerResult.removeProfileByIdentifier(ITEM_MANAGE_ACCOUNT.toLong())
        } else if (headerResult.profiles != null && headerResult.profiles!!.size < 2) {
            headerResult.addProfiles(getProfileSettingDrawerItem())
        }
    }

    private fun openMyContributions() {
        if (isUserSet()) {
            openMyContributionsInSearchActivity()
        } else {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle(R.string.contribute)
                .setMessage(R.string.contribution_without_account)
                .setPositiveButton(R.string.create_account_button) { _, _ ->
                    CustomTabActivityHelper.openCustomTab(
                        this,
                        customTabsIntent,
                        "${getString(R.string.website)}cgi/user.pl".toUri(),
                        WebViewFallback()
                    )
                }
                .setNeutralButton(R.string.login_button) { _, _ -> loginThenOpenContributions.launch(Unit) }
                .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
                .show()
        }
    }

    private fun openMyContributionsInSearchActivity() =
        startSearch(this, SearchType.CONTRIBUTOR, getUserLogin()!!)

    private fun getProfileSettingDrawerItem(): IProfile<ProfileSettingDrawerItem> {
        val userLogin = getUserLogin()
        val userSession = getUserSession()
        userSettingsURI = "${getString(R.string.website)}cgi/user.pl?type=edit&userid=$userLogin&user_id=$userLogin&user_session=$userSession".toUri()
        customTabActivityHelper.mayLaunchUrl(userSettingsURI, null, null)

        return profileSettingItem {
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
        getLoginPreferences().edit { clear() }
        updateConnectedState()
        matomoAnalytics.trackEvent(AnalyticsEvent.UserLogout)
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
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate(
                supportFragmentManager.getBackStackEntryAt(0).id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            // Close the app if no Fragment is visible anymore
            if (supportFragmentManager.backStackEntryCount == 0) {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)



        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.action_search).also { menuItem ->
            val searchView = menuItem.actionView as SearchView
            val bottomNavigation = binding.bottomNavigationInclude.bottomNavigation

            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    bottomNavigation.visibility = View.GONE
                } else {
                    bottomNavigation.visibility = View.VISIBLE
                }
            }

            searchView.setOnSearchClickListener {
                listenToKeyboardVisibilityChanges(object : OnKeyboardVisibilityChanged {
                    override fun onKeyboardVisible() {
                        bottomNavigation.visibility = View.GONE
                    }

                    override fun onKeyboardDismissed() {
                        bottomNavigation.visibility = View.VISIBLE
                    }
                })

                bottomNavigation.visibility = View.GONE
            }

            searchView.setOnCloseListener {
                stopListeningToKeyboardVisibilityChanges()
                false
            }

            if (intent.getBooleanExtra(PRODUCT_SEARCH_KEY, false)) {
                menuItem.expandActionView()
            }
        }
        return true
    }

    private fun getLogoutDrawerItem() = primaryItem {
        withName(getString(R.string.logout_drawer))
        withIcon(GoogleMaterial.Icon.gmd_settings_power)
        withIdentifier(ITEM_LOGOUT.toLong())
        withSelectable(false)
    }

    private fun getLoginDrawerItem() = primaryItem {
        withName(R.string.sign_in_drawer)
        withIcon(GoogleMaterial.Icon.gmd_account_circle)
        withIdentifier(ITEM_LOGIN.toLong())
        withSelectable(false)
    }

    private fun getUserProfile(): ProfileDrawerItem = profileItem {
        withName(getLoginPreferences().getString("user", resources.getString(R.string.txt_anonymous)))
        withIcon(R.drawable.img_home)
        withIdentifier(ITEM_USER.toLong())
    }

    override fun onStart() {
        super.onStart()
        customTabActivityHelper.bindCustomTabsService(this)
        if (isFlavors(OFF)
            && isUserSet()
            && !prefManager.isFirstTimeLaunch
            && !prefManager.userAskedToRate
        ) {
            val firstTimeLaunchTime = prefManager.firstTimeLaunchTime

            // Check if it has been a week since first launch
            if (firstTimeLaunchTime + System.currentTimeMillis() >= 7 * 24 * 60 * 60 * 1000)
                showFeedbackDialog()
        }
    }

    /**
     * show dialog to ask the user to rate the app/give feedback
     */
    private fun showFeedbackDialog() {
        //dialog for rating the app on play store
        val rateDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.app_name)
            .setMessage(R.string.user_ask_rate_app)
            .setPositiveButton(R.string.rate_app) { dialog, _ ->
                //open app page in play store
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no_thx) { dialog, _ -> dialog.dismiss() }

        //dialog for giving feedback
        val feedbackDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.app_name)
            .setMessage(R.string.user_ask_show_feedback_form)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                //show feedback form
                CustomTabActivityHelper.openCustomTab(
                    this@MainActivity,
                    customTabsIntent,
                    getString(R.string.feedback_form_url).toUri(),
                    WebViewFallback(),
                )
                dialog.dismiss()
            }
            .setNegativeButton(R.string.txtNo) { dialog, _ -> dialog.dismiss() }


        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.app_name)
            .setMessage(R.string.user_enjoying_app)
            .setPositiveButton(R.string.txtYes) { dialog, _ ->
                prefManager.userAskedToRate = true
                rateDialog.show()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.txtNo) { dialog, _ ->
                prefManager.userAskedToRate = true
                feedbackDialog.show()
                dialog.dismiss()
            }
            .show()
    }

    override fun onStop() {
        super.onStop()
        customTabActivityHelper.unbindCustomTabsService(this)
    }

    override fun onDestroy() {
        customTabActivityHelper.connectionCallback = null
        stopListeningToKeyboardVisibilityChanges()
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
                startSearch(this, SearchType.SEARCH, query)

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
    private fun swapToSearchByCode() {
        swapToFragment(SearchByCodeFragment.newInstance())
        drawerResult.setSelection(ITEM_SEARCH_BY_CODE.toLong())
        supportActionBar?.title = resources.getString(R.string.search_by_barcode_drawer)
    }

    override fun setItemSelected(@NavigationDrawerType type: Int) = drawerResult.setSelection(type.toLong(), false)

    public override fun onResume() {
        super.onResume()
        binding.bottomNavigationInclude.bottomNavigation.selectNavigationItem(R.id.home_page)
        matomoAnalytics.showAnalyticsBottomSheetIfNeeded(supportFragmentManager)

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
        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { chooseImageDialog(listOf(it)) }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { chooseImageDialog(it.filterNotNull()) }
    }

    private fun chooseImageDialog(selectedImages: List<Uri>) {
        lifecycleScope.launch(Dispatchers.Main) {
            val barcodes = detectBarcodeInImages(selectedImages)
            if (barcodes.isNotEmpty()) createAlertDialog(false, barcodes.first(), selectedImages)
            else createAlertDialog(true, "", selectedImages)
        }
    }

    /**
     * IO / Computing intensive operation
     *
     * @param selectedImages
     */
    private suspend fun detectBarcodeInImages(selectedImages: List<Uri>) =
        withContext(Dispatchers.Default) {
            selectedImages
                .asSequence()
                .mapNotNull { uri ->
                    val bMap = try {
                        contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
                    } catch (e: FileNotFoundException) {
                        Log.e(MainActivity::class.simpleName, "Could not resolve file from Uri $uri", e)
                        null
                    } catch (e: IOException) {
                        Log.e(MainActivity::class.simpleName, "IO error during bitmap stream decoding: ${e.message}", e)
                        null
                    } ?: return@mapNotNull null

                    // Decoding bitmap
                    val intArray = IntArray(bMap.width * bMap.height)
                    bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
                    val source = RGBLuminanceSource(bMap.width, bMap.height, intArray)
                    val bitmap = BinaryBitmap(HybridBinarizer(source))
                    val reader = MultiFormatReader()

                    try {
                        val decodeHints = mapOf(
                            DecodeHintType.TRY_HARDER to true,
                            DecodeHintType.PURE_BARCODE to true
                        )
                        reader.decode(bitmap, decodeHints)?.text
                    } catch (e: FormatException) {
                        Toast.makeText(this@MainActivity, getString(R.string.format_error), Toast.LENGTH_SHORT).show()
                        Log.e(MainActivity::class.simpleName, "Error decoding bitmap into barcode: ${e.message}")
                        null
                    } catch (e: Exception) {
                        Log.e(MainActivity::class.simpleName, "Error decoding bitmap into barcode: ${e.message}")
                        null
                    }

                }.toList()
        }

    private fun createAlertDialog(hasEditText: Boolean, barcode: String, uris: List<Uri>) {
        val alertDialogBuilder = AlertDialog.Builder(this)

        val dialogView = layoutInflater.inflate(R.layout.alert_barcode, null)
        alertDialogBuilder.setView(dialogView)

        dialogView.findViewById<RecyclerView>(R.id.product_image).run {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = PhotosAdapter(uris)
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
                for (imgUri in uris) {
                    val tempBarcode = if (hasEditText) barcodeEditText.text.toString() else barcode

                    if (tempBarcode.isEmpty()) {
                        Toast.makeText(this@MainActivity, getString(R.string.sorry_msg), Toast.LENGTH_LONG).show()
                    } else {
                        dialog.dismiss()
                        if (!this@MainActivity.isNetworkConnected()) {
                            ProductEditActivity.start(this@MainActivity, Product().apply { code = tempBarcode })
                        } else {
                            contentResolver.openInputStream(imgUri)!!.use {
                                val image = ProductImage(
                                    tempBarcode,
                                    ProductImageField.OTHER,
                                    it.readBytes(),
                                    localeManager.getLanguage()
                                )
                                // Post image
                                viewModel.postImage(image)
                            }
                        }
                    }
                }
            }
            setNegativeButton(R.string.txtNo) { d, _ -> d.cancel() }
            show()
        }

    }

    companion object {
        private const val USER_ID = 500L
        private const val CONTRIBUTIONS_SHORTCUT = "CONTRIBUTIONS"
        private const val SCAN_SHORTCUT = "SCAN"
        private const val BARCODE_SHORTCUT = "BARCODE"
        const val PRODUCT_SEARCH_KEY = "product_search"
        private val LOG_TAG = MainActivity::class.simpleName!!

        fun start(context: Context) = context.startActivity(Intent(context, MainActivity::class.java))
    }
}
