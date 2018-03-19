package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fastadapter.commons.utils.RecyclerViewCacheUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.AllergensAlertFragment;
import openfoodfacts.github.scrachx.openfood.fragments.FindProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.HomeFragment;
import openfoodfacts.github.scrachx.openfood.fragments.OfflineEditFragment;
import openfoodfacts.github.scrachx.openfood.fragments.PreferencesFragment;
import openfoodfacts.github.scrachx.openfood.models.SendProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.category.activity.CategoryActivity;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class MainActivity extends BaseActivity implements CustomTabActivityHelper.ConnectionCallback, NavigationDrawerListener {

    private static final int LOGIN_REQUEST = 1;
    private static final long USER_ID = 500;
    private static final String CONTRIBUTIONS_SHORTCUT = "CONTRIBUTIONS";
    private static final String SCAN_SHORTCUT = "SCAN";
    private static final String BARCODE_SHORTCUT = "BARCODE";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private AccountHeader headerResult = null;
    private Drawer result = null;

    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;

    private Uri userAccountUri;
    private Uri contributeUri;
    private Uri discoverUri;
    private Uri userContributeUri;
    private SendProductDao mSendProductDao;
    private int numberOFSavedProducts;
    private SharedPreferences mSharedPref;
    PrimaryDrawerItem primaryDrawerItem;
    private int positionOfOfflineBadeItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_main);

        Utils.hideKeyboard(this);

        final IProfile<ProfileDrawerItem> profile = getUserProfile();
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Bundle extras = getIntent().getExtras();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mSendProductDao = Utils.getAppDaoSession(MainActivity.this).getSendProductDao();
        numberOFSavedProducts = mSendProductDao.loadAll().size();

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

            }
        });

        boolean isOpenOfflineEdit = extras != null && extras.getBoolean("openOfflineEdit");
        if (isOpenOfflineEdit) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, new
                    OfflineEditFragment()).commit();
            getSupportActionBar().setTitle(getResources().getString(R.string.offline_edit_drawer));
        } else {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, new HomeFragment
                    ()).commit();
            getSupportActionBar().setTitle(getResources().getString(R.string.home_drawer));
        }

        // chrome custom tab init
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);

        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(),
                customTabActivityHelper.getSession());

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(profile)
                .withSelectionListEnabledForSingleProfile(false)
                .withOnAccountHeaderListener((view, profile1, current) -> {
                    if (profile1 instanceof IDrawerItem) {
                        if (profile1.getIdentifier() == ITEM_MANAGE_ACCOUNT) {
                            CustomTabActivityHelper.openCustomTab(MainActivity.this,
                                    customTabsIntent, userAccountUri, new WebViewFallback());
                        }
                    }

                    //false if you have not consumed the event and it should close the drawer
                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .build();

        // Add Manage Account profile if the user is connected
        SharedPreferences preferences = getSharedPreferences("login", 0);
        String userLogin = preferences.getString("user", null);
        String userSession = preferences.getString("user_session", null);
        boolean isUserConnected = userLogin != null && userSession != null;
        if (isUserConnected) {
            userAccountUri = Uri.parse(getString(R.string.website) + "cgi/user.pl?type=edit&userid=" + userLogin + "&user_id=" + userLogin +
                    "&user_session=" + userSession);
            customTabActivityHelper.mayLaunchUrl(userAccountUri, null, null);

            headerResult.addProfiles(getProfileSettingDrawerItem());
        }
        primaryDrawerItem = createOfflineEditDrawerItem();
        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        Utils.hideKeyboard(MainActivity.this);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                    }

                    @Override

                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        Utils.hideKeyboard(MainActivity.this);
                    }
                })
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.home_drawer).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(ITEM_HOME),
                        new SectionDrawerItem().withName(R.string.search_drawer),
                        new PrimaryDrawerItem().withName(R.string.search_by_barcode_drawer).withIcon(GoogleMaterial.Icon.gmd_dialpad).withIdentifier(ITEM_SEARCH_BY_CODE),
                        new PrimaryDrawerItem().withName(R.string.search_by_category).withIcon(GoogleMaterial.Icon.gmd_filter_list).withIdentifier(ITEM_CATEGORIES).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.scan_search).withIcon(R.drawable.barcode_grey_24dp).withIdentifier(ITEM_SCAN).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.advanced_search_title).withIcon(GoogleMaterial.Icon.gmd_insert_chart).withIdentifier(ITEM_ADVANCED_SEARCH).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.scan_history_drawer).withIcon(GoogleMaterial.Icon.gmd_history).withIdentifier(ITEM_HISTORY).withSelectable(false),
                        new SectionDrawerItem().withName(R.string.user_drawer).withIdentifier(USER_ID),
                        new PrimaryDrawerItem().withName(getString(R.string.action_contributes)).withIcon(GoogleMaterial.Icon.gmd_rate_review).withIdentifier(ITEM_MY_CONTRIBUTIONS).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.alert_drawer).withIcon(GoogleMaterial.Icon.gmd_warning).withIdentifier(ITEM_ALERT),
                        new PrimaryDrawerItem().withName(R.string.action_preferences).withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(ITEM_PREFERENCES),
                        new DividerDrawerItem(),
                        primaryDrawerItem,
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.action_discover).withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(ITEM_ABOUT).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.contribute).withIcon(R.drawable.ic_group_grey_24dp).withIdentifier(ITEM_CONTRIBUTE).withSelectable(false),
                        new PrimaryDrawerItem().withName(R.string.open_beauty_drawer).withIcon(GoogleMaterial.Icon.gmd_shop).withIdentifier(ITEM_OBF).withSelectable(false)
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {

                    if (drawerItem == null) {
                        return false;
                    }

                    Fragment fragment = null;
                    switch ((int) drawerItem.getIdentifier()) {
                        case ITEM_HOME:
                            fragment = new HomeFragment();
                            break;
                        case ITEM_SEARCH_BY_CODE:
                            fragment = new FindProductFragment();
                            break;
                        case ITEM_CATEGORIES:
                            startActivity(CategoryActivity.getIntent(this));
                            break;
                        case ITEM_SCAN:
                            scan();
                            break;
                        case ITEM_HISTORY:
                            startActivity(new Intent(MainActivity.this, HistoryScanActivity.class));
                            break;
                        case ITEM_LOGIN:
                            startActivityForResult(new Intent(MainActivity.this, LoginActivity
                                    .class), LOGIN_REQUEST);
                            break;
                        case ITEM_ALERT:
                            fragment = new AllergensAlertFragment();
                            break;
                        case ITEM_PREFERENCES:
                            fragment = new PreferencesFragment();
                            break;
                        case ITEM_OFFLINE:
                            fragment = new OfflineEditFragment();
                            break;
                        case ITEM_ABOUT:
                            CustomTabActivityHelper.openCustomTab(MainActivity.this,
                                    customTabsIntent, discoverUri, new WebViewFallback());
                            break;
                        case ITEM_CONTRIBUTE:
                            CustomTabActivityHelper.openCustomTab(MainActivity.this,
                                    customTabsIntent, contributeUri, new WebViewFallback());
                            break;
                        case ITEM_OBF:
                            boolean otherOFAppInstalled = Utils.isApplicationInstalled
                                    (MainActivity.this, BuildConfig.OFOTHERLINKAPP);
                            if (otherOFAppInstalled) {
                                Intent LaunchIntent = getPackageManager()
                                        .getLaunchIntentForPackage(BuildConfig.OFOTHERLINKAPP);
                                startActivity(LaunchIntent);
                            } else {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                                            ("market://details?id=" + BuildConfig.OFOTHERLINKAPP)));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" +
                                            BuildConfig.OFOTHERLINKAPP)));

                                }
                            }
                            break;

                        case ITEM_ADVANCED_SEARCH:
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            CustomTabsIntent customTabsIntent = builder.build();
                            CustomTabActivityHelper.openCustomTab(this, customTabsIntent, Uri.parse(getString(R.string.advanced_search_url)), new
                                    WebViewFallback());
                            break;

                        case ITEM_MY_CONTRIBUTIONS:
                            myContributions();
                            break;
                        case ITEM_LOGOUT:
                            new MaterialDialog.Builder(MainActivity.this)
                                    .title(R.string.confirm_logout)
                                    .content(R.string.logout_dialog_content)
                                    .positiveText(R.string.txtOk)
                                    .negativeText(R.string.dialog_cancel)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(MaterialDialog dialog, DialogAction which) {
                                            logout();
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(MaterialDialog dialog, DialogAction which) {
                                            Toast.makeText(getApplicationContext(), "Cancelled",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }).show();
                            break;
                        default:
                            // nothing to do
                            break;
                    }

                    if (fragment != null) {
                             getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
                    } else {
                        // error in creating fragment
                        Log.e("MainActivity", "Error in creating fragment");
                    }

                    return false;
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(false)
                .build();

        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        // Add Drawer items for the connected user
        result.addItemsAtPosition(result.getPosition(ITEM_MY_CONTRIBUTIONS), isUserConnected ?
                getLogoutDrawerItem() : getLoginDrawerItem());
        if (BuildConfig.FLAVOR.equals("obf")) {
            result.removeItem(ITEM_ALERT);
            result.updateName(ITEM_OBF, new StringHolder(getString(R.string.open_food_drawer)));
        }

        if (BuildConfig.FLAVOR.equals("opff")) {
            result.removeItem(ITEM_ALERT);
            result.updateName(ITEM_OBF, new StringHolder(getString(R.string.open_food_drawer)));
        }

        // Remove scan item if the device does not have a camera, for example, Chromebooks or
        // Fire devices
        if (!Utils.isHardwareCameraInstalled(this)) {
            result.removeItem(ITEM_SCAN);
        }


        //if you have many different types of DrawerItems you can magically pre-cache those items
        // to get a better scroll performance
        //make sure to init the cache after the DrawerBuilder was created as this will first
        // clear the cache to make sure no old elements are in
        //RecyclerViewCacheUtil.getInstance().withCacheSize(2).init(result);
        new RecyclerViewCacheUtil<IDrawerItem>().withCacheSize(2).apply(result.getRecyclerView(),
                result.getDrawerItems());

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 1
            result.setSelection(ITEM_HOME, false);

            //set the active profile
            headerResult.setActiveProfile(profile);
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext
                ());
        if (settings.getBoolean("startScan", false)) {
            Intent cameraIntent = new Intent(MainActivity.this, ScannerFragmentActivity.class);
            startActivity(cameraIntent);
        }

        // prefetch uris
        contributeUri = Uri.parse(getString(R.string.website_contribute));
        discoverUri = Uri.parse(getString(R.string.website_discover));
        userContributeUri = Uri.parse(getString(R.string.website_contributor) + userLogin);

        customTabActivityHelper.mayLaunchUrl(contributeUri, null, null);
        customTabActivityHelper.mayLaunchUrl(discoverUri, null, null);
        customTabActivityHelper.mayLaunchUrl(userContributeUri, null, null);

        if (CONTRIBUTIONS_SHORTCUT.equals(getIntent().getAction())) {
            myContributions();
        }

        if (SCAN_SHORTCUT.equals(getIntent().getAction())) {
            scan();
        }

        if (BARCODE_SHORTCUT.equals(getIntent().getAction())) {
            moveToBarcodeEntry();
        }

        //Scheduling background image upload job
        Utils.scheduleProductUploadJob(this);

        //Adds nutriscore and quantity values in old history for schema 5 update
        mSharedPref = getApplicationContext().getSharedPreferences("prefs", 0);
        boolean isOldHistoryDataSynced = mSharedPref.getBoolean("is_old_history_data_synced", false);
        if (!isOldHistoryDataSynced && Utils.isNetworkConnected(this)) {
            OpenFoodAPIClient apiClient = new OpenFoodAPIClient(this);
            apiClient.syncOldHistory();
        }

    }

    private void scan() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest
                    .permission.CAMERA)) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.action_about)
                        .content(R.string.permission_camera)
                        .neutralText(R.string.txtOk)
                        .show().setOnDismissListener(dialogInterface -> ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        Utils.MY_PERMISSIONS_REQUEST_CAMERA));

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest
                        .permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            Intent intent = new Intent(MainActivity.this, ScannerFragmentActivity.class);
            startActivity(intent);
        }
    }

    private void myContributions() {
        SharedPreferences preferences1 = getSharedPreferences("login", 0);
        String userLogin1 = preferences1.getString("user", null);
        userContributeUri = Uri.parse(getString(R.string.website_contributor) + userLogin1);
        if (isNotEmpty(userLogin1)) {

            ProductBrowsingListActivity.startActivity(this, userLogin1, SearchType.CONTRIBUTOR);


        } else {
            new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.contribute)
                    .content(R.string.contribution_without_account)
                    .positiveText(R.string.create_account_button)
                    .neutralText(R.string.login_button)
                    .onPositive((dialog, which) -> CustomTabActivityHelper.openCustomTab(MainActivity.this, customTabsIntent, Uri.parse(getString(R
                            .string.website) + "cgi/user.pl"), new WebViewFallback()))
                    .onNeutral((dialog, which) -> startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOGIN_REQUEST))
                    .show();
        }
    }

    private IProfile<ProfileSettingDrawerItem> getProfileSettingDrawerItem() {
        SharedPreferences preferences = getSharedPreferences("login", 0);
        String userLogin = preferences.getString("user", null);
        String userSession = preferences.getString("user_session", null);
        userAccountUri = Uri.parse(getString(R.string.website) + "cgi/user.pl?type=edit&userid=" + userLogin + "&user_id=" + userLogin +
                "&user_session=" + userSession);
        customTabActivityHelper.mayLaunchUrl(userAccountUri, null, null);
        return new ProfileSettingDrawerItem()
                .withName(getString(R.string.action_manage_account))
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withIdentifier(ITEM_MANAGE_ACCOUNT)
                .withSelectable(false);
    }

    /**
     * Replace logout menu item by the login menu item
     * Change current user profile (Anonymous)
     * Remove all Account Header items
     * Remove user login info
     */
    private void logout() {
        getSharedPreferences("login", MODE_PRIVATE).edit().clear().commit();
        headerResult.removeProfileByIdentifier(ITEM_MANAGE_ACCOUNT);
        headerResult.updateProfile(getUserProfile());
        result.addItemAtPosition(getLoginDrawerItem(), result.getPosition(ITEM_MY_CONTRIBUTIONS));
        result.removeItem(ITEM_LOGOUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOGIN_REQUEST:
                if (resultCode == RESULT_OK) {
                    result.removeItem(ITEM_LOGIN);
                    result.addItemsAtPosition(result.getPosition(ITEM_MY_CONTRIBUTIONS),
                            getLogoutDrawerItem());
                    headerResult.updateProfile(getUserProfile());
                    headerResult.addProfiles(getProfileSettingDrawerItem());
                }
                break;
            default:
                // do nothing
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the
        // activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack(getSupportFragmentManager().getBackStackEntryAt(0).getId(), getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
            }
            else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat
                .OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment currentFragment = fragmentManager.findFragmentById(R.id
                        .fragment_container);


                return true;
            }
        });

        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utils.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager
                        .PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, ScannerFragmentActivity.class);
                    startActivity(intent);
                }
            }
            break;
        }
    }

    private IDrawerItem<PrimaryDrawerItem, com.mikepenz.materialdrawer.model
            .AbstractBadgeableDrawerItem.ViewHolder> getLogoutDrawerItem() {
        return new PrimaryDrawerItem()
                .withName(getString(R.string.logout_drawer))
                .withIcon(GoogleMaterial.Icon.gmd_settings_power)
                .withIdentifier(ITEM_LOGOUT)
                .withSelectable(false);
    }

    private IDrawerItem<PrimaryDrawerItem, com.mikepenz.materialdrawer.model
            .AbstractBadgeableDrawerItem.ViewHolder> getLoginDrawerItem() {
        return new PrimaryDrawerItem()
                .withName(R.string.sign_in_drawer)
                .withIcon(GoogleMaterial.Icon.gmd_account_circle)
                .withIdentifier(ITEM_LOGIN)
                .withSelectable(false);
    }

    private IProfile<ProfileDrawerItem> getUserProfile() {
        String userLogin = getSharedPreferences("login", 0)
                .getString("user", getResources().getString(R.string.txt_anonymous));

        return new ProfileDrawerItem()
                .withName(userLogin)
                .withIcon(R.drawable.img_home)
                .withIdentifier(ITEM_USER);
    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    protected void onDestroy() {
        customTabActivityHelper.setConnectionCallback(null);
        super.onDestroy();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.e("INTENT", "start activity");
            String query = intent.getStringExtra(SearchManager.QUERY);
            ProductBrowsingListActivity.startActivity(this, query, SearchType.SEARCH);
        }
    }

    /**
     * This moves the main activity to the barcode entry fragment.
     */
    public void moveToBarcodeEntry() {
        result.setSelection(ITEM_SEARCH_BY_CODE);
        Fragment fragment = new FindProductFragment();
        getSupportActionBar().setTitle(getResources().getString(R.string.search_by_barcode_drawer));

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    fragment).commit();
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    /**
     * This moves the main activity to the preferences fragment.
     */
    public void moveToPreferences() {
        result.setSelection(ITEM_PREFERENCES);
        Fragment fragment = new PreferencesFragment();
        getSupportActionBar().setTitle(R.string.action_preferences);

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        } else {
            Log.e(getClass().getSimpleName(), "Error in creating fragment");
        }
    }

    /**
     * Create the drawer item. This adds a badge if there are items in the offline edit, otherwise
     * there is no badge present.
     *
     * @return drawer item.
     */
    private PrimaryDrawerItem createOfflineEditDrawerItem() {
        if (numberOFSavedProducts > 0) {
            return new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(GoogleMaterial.Icon.gmd_local_airport).withIdentifier(9)
                    .withBadge(String.valueOf(numberOFSavedProducts)).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R
                            .color.md_red_700));
        } else {
            return new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(GoogleMaterial.Icon.gmd_local_airport).withIdentifier(ITEM_OFFLINE);
        }
    }

    /**
     * Updates the drawer item. This updates the badge if there are items left in offline edit, otherwise
     * there is no badge present.
     * This function is called from OfflineEditFragment only.
     */
    public void updateBadgeOfflineEditDrawerITem(int size) {
        positionOfOfflineBadeItem = result.getPosition(primaryDrawerItem);
        if (size > 0) {
            primaryDrawerItem = new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(GoogleMaterial.Icon.gmd_local_airport).withIdentifier(ITEM_OFFLINE).withBadge(String.valueOf(size)).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700));
        } else {
            primaryDrawerItem = new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(GoogleMaterial.Icon.gmd_local_airport).withIdentifier(ITEM_OFFLINE);
        }
        result.updateItemAtPosition(primaryDrawerItem, positionOfOfflineBadeItem);
    }


    @Override
    public void setItemSelected(@NavigationDrawerType Integer type) {
        result.setSelection(type, false);
    }
}
