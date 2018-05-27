package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fastadapter.commons.utils.RecyclerViewCacheUtil;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
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
import openfoodfacts.github.scrachx.openfood.fragments.AlertUserFragment;
import openfoodfacts.github.scrachx.openfood.fragments.FindProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.HomeFragment;
import openfoodfacts.github.scrachx.openfood.fragments.OfflineEditFragment;
import openfoodfacts.github.scrachx.openfood.fragments.PreferencesFragment;
import openfoodfacts.github.scrachx.openfood.fragments.SearchProductsResultsFragment;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class MainActivity extends BaseActivity implements CustomTabActivityHelper.ConnectionCallback {

    private static final int LOGIN_REQUEST = 1;
    public static final int USER_PROFILE = 100;
    private static final long PROFILE_SETTING = 200;
    private static final int CONTRIBUTOR = 300;
    private static final int LOGOUT = 400;
    public static final int LOGIN_ID = 6;
    private static final long USER_ID = 500;
    private static final int ABOUT = 600;
    private static final int CONTRIBUTE = 700;

    @BindView(R.id.toolbar) Toolbar toolbar;
    private AccountHeader headerResult = null;
    private Drawer result = null;

    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;

    private Uri userAccountUri;
    private Uri contributeUri;
    private Uri discoverUri;
    private Uri userContributeUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.hideKeyboard(this);

        final IProfile profile = getUserProfile();
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Bundle extras = getIntent().getExtras();
        FragmentManager fragmentManager = getSupportFragmentManager();

        boolean isOpenOfflineEdit = extras != null && extras.getBoolean("openOfflineEdit");
        if (isOpenOfflineEdit) {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, new OfflineEditFragment()).commit();
            getSupportActionBar().setTitle(getResources().getString(R.string.offline_edit_drawer));
        } else {
            fragmentManager.beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            getSupportActionBar().setTitle(getResources().getString(R.string.home_drawer));
        }

        // chrome custom tab init
        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);

        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(), customTabActivityHelper.getSession());

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(profile)
                .withOnAccountHeaderListener((view, profile1, current) -> {
                    if (profile1 instanceof IDrawerItem) {
                        if (profile1.getIdentifier() == PROFILE_SETTING) {
                            CustomTabActivityHelper.openCustomTab(MainActivity.this, customTabsIntent, userAccountUri, new WebViewFallback());
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
            userAccountUri = Uri.parse(getString(R.string.website) + "cgi/user.pl?type=edit&userid=" + userLogin + "&user_id=" + userLogin + "&user_session=" + userSession);
            customTabActivityHelper.mayLaunchUrl(userAccountUri, null, null);

            headerResult.addProfiles(getProfileSettingDrawerItem());
        }

        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                    new PrimaryDrawerItem().withName(R.string.home_drawer).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(1),
                    new SectionDrawerItem().withName(R.string.search_drawer),
                    new PrimaryDrawerItem().withName(R.string.search_by_barcode_drawer).withIcon(GoogleMaterial.Icon.gmd_dialpad).withIdentifier(2),
                    new PrimaryDrawerItem().withName(R.string.scan_search).withIcon(R.drawable.barcode_grey_24dp).withIdentifier(4),
                    new PrimaryDrawerItem().withName(R.string.scan_history_drawer).withIcon(GoogleMaterial.Icon.gmd_history).withIdentifier(5),
                    new SectionDrawerItem().withName(R.string.user_drawer).withIdentifier(USER_ID),
                    new PrimaryDrawerItem().withName(getString(R.string.action_contributes)).withIcon(GoogleMaterial.Icon.gmd_rate_review).withIdentifier(CONTRIBUTOR),
                    new PrimaryDrawerItem().withName(R.string.alert_drawer).withIcon(GoogleMaterial.Icon.gmd_warning).withIdentifier(7),
					new PrimaryDrawerItem().withName(R.string.user_ratings).withIcon(GoogleMaterial.Icon.gmd_stars).withIdentifier(12),
                    new PrimaryDrawerItem().withName(R.string.action_preferences).withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(8),
                    new DividerDrawerItem(),
                    new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(GoogleMaterial.Icon.gmd_local_airport).withIdentifier(9),
                    new DividerDrawerItem(),
                    new PrimaryDrawerItem().withName(R.string.action_discover).withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(ABOUT),
                    new PrimaryDrawerItem().withName(R.string.contribute).withIcon(R.drawable.ic_group_grey_24dp).withIdentifier(CONTRIBUTE),
                    new PrimaryDrawerItem().withName(R.string.open_beauty_drawer).withIcon(GoogleMaterial.Icon.gmd_shop).withIdentifier(11)
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {

                    if (drawerItem == null) {
                        return false;
                    }

                    Fragment fragment = null;
                    switch ((int) drawerItem.getIdentifier()) {
                        case 1:
                            fragment = new HomeFragment();
                            getSupportActionBar().setTitle(getResources().getString(R.string.home_drawer));
                            break;
                        case 2:
                            fragment = new FindProductFragment();
                            getSupportActionBar().setTitle(getResources().getString(R.string.search_by_barcode_drawer));
                            break;
                        case 4:
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .title(R.string.action_about)
                                            .content(R.string.permission_camera)
                                            .neutralText(R.string.txtOk)
                                            .show();
                                } else {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
                                }
                            } else {
                                Intent intent = new Intent(MainActivity.this, ScannerFragmentActivity.class);
                                startActivity(intent);
                            }
                            break;
                        case 5:
                            startActivity(new Intent(MainActivity.this, HistoryScanActivity.class));
                            break;
                        case LOGIN_ID:
                            startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), LOGIN_REQUEST);
                            break;
                        case 7:
                            fragment = new AlertUserFragment();
                            getSupportActionBar().setTitle(R.string.alert_drawer);
                            break;
                        case 8:
                            fragment = new PreferencesFragment();
                            getSupportActionBar().setTitle(R.string.action_preferences);
                            break;
                        case 9:
                            fragment = new OfflineEditFragment();
                            getSupportActionBar().setTitle(getResources().getString(R.string.offline_edit_drawer));
                            break;
                        case ABOUT:
                            CustomTabActivityHelper.openCustomTab(MainActivity.this, customTabsIntent, discoverUri, new WebViewFallback());
                            break;
                        case CONTRIBUTE:
                            CustomTabActivityHelper.openCustomTab(MainActivity.this, customTabsIntent, contributeUri, new WebViewFallback());
                            break;
                        case 11:
                            boolean otherOFAppInstalled = Utils.isApplicationInstalled(MainActivity.this, BuildConfig.OFOTHERLINKAPP);
                            if (otherOFAppInstalled) {
                                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(BuildConfig.OFOTHERLINKAPP);
                                startActivity(LaunchIntent);
                            } else {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + BuildConfig.OFOTHERLINKAPP)));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.OFOTHERLINKAPP)));
                                }
                            }
                            break;
                        case 12:
                            startActivity(new Intent(MainActivity.this, UserRatingsActivity.class));
                            break;
                        case CONTRIBUTOR:
                            SharedPreferences preferences1 = getSharedPreferences("login", 0);
                            String userLogin1 = preferences1.getString("user", null);
                            if (isNotEmpty(userLogin1)) {
                                CustomTabActivityHelper.openCustomTab(MainActivity.this, customTabsIntent, userContributeUri, new WebViewFallback());
                            } else {
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title(R.string.contribute)
                                        .content(R.string.contribution_without_account)
                                        .positiveText(R.string.txtOk)
                                        .negativeText(R.string.cancel_button)
                                        .onPositive((dialog, which) -> CustomTabActivityHelper.openCustomTab(MainActivity.this, customTabsIntent, Uri.parse(getString(R.string.website) + "cgi/user.pl"), new WebViewFallback()))
                                        .show();
                            }
                            break;
                        case LOGOUT:
                            logout();
                            break;
                        default:
                            // nothing to do
                            break;
                    }

                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
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
        result.addItemsAtPosition(result.getPosition(CONTRIBUTOR), isUserConnected ? getLogoutDrawerItem() : getLoginDrawerItem());
        if(BuildConfig.FLAVOR.equals("obf")) {
            result.removeItem(7);
            result.updateName(11, new StringHolder(getString(R.string.open_food_drawer)));
        }
        if(BuildConfig.FLAVOR.equals("opff")) {
            result.removeItem(7);
            result.updateName(11, new StringHolder(getString(R.string.open_food_drawer)));
        }

        //if you have many different types of DrawerItems you can magically pre-cache those items to get a better scroll performance
        //make sure to init the cache after the DrawerBuilder was created as this will first clear the cache to make sure no old elements are in
        //RecyclerViewCacheUtil.getInstance().withCacheSize(2).init(result);
        new RecyclerViewCacheUtil<IDrawerItem>().withCacheSize(2).apply(result.getRecyclerView(), result.getDrawerItems());

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 1
            result.setSelection(1, false);

            //set the active profile
            headerResult.setActiveProfile(profile);
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
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
    }

    private IProfile getProfileSettingDrawerItem() {
        return new ProfileSettingDrawerItem()
                .withName(getString(R.string.action_manage_account))
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withIdentifier(PROFILE_SETTING);
    }

    /**
     * Replace logout menu item by the login menu item
     * Change current user profile (Anonymous)
     * Remove all Account Header items
     * Remove user login info
     */
    private void logout() {
        getSharedPreferences("login", 0).edit().clear().apply();

        headerResult.removeProfileByIdentifier(PROFILE_SETTING);
        headerResult.setActiveProfile(getUserProfile());

        result.addItemAtPosition(getLoginDrawerItem(), result.getPosition(CONTRIBUTOR));
        result.removeItem(LOGOUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOGIN_REQUEST:
                if (resultCode == RESULT_OK) {
                    result.removeItem(LOGIN_ID);
                    result.addItemsAtPosition(result.getPosition(CONTRIBUTOR), getLogoutDrawerItem());
                    headerResult.setActiveProfile(getUserProfile());
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
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

                // Not replace if no search has been done (no switch of fragment)
                if (currentFragment instanceof SearchProductsResultsFragment) {
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                }

                return true;
            }
        });

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Utils.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MainActivity.this, ScannerFragmentActivity.class);
                    startActivity(intent);
                } else {
                    new MaterialDialog.Builder(this)
                            .title(R.string.permission_title)
                            .content(R.string.permission_denied)
                            .negativeText(R.string.txtNo)
                            .positiveText(R.string.txtYes)
                            .onPositive((dialog, which) -> {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .show();
                }
                break;
            }
        }
    }

    private IDrawerItem getLogoutDrawerItem() {
        return new PrimaryDrawerItem()
                .withName(getString(R.string.logout_drawer))
                .withIcon(GoogleMaterial.Icon.gmd_settings_power)
                .withIdentifier(LOGOUT);
    }

    private IDrawerItem getLoginDrawerItem() {
        return new PrimaryDrawerItem()
                .withName(R.string.sign_in_drawer)
                .withIcon(GoogleMaterial.Icon.gmd_account_circle)
                .withIdentifier(LOGIN_ID);
    }

    private IProfile getUserProfile() {
        String userLogin = getSharedPreferences("login", 0)
                .getString("user", getResources().getString(R.string.txt_anonymous));

        return new ProfileDrawerItem()
                .withName(userLogin)
                .withIcon(R.drawable.img_home)
                .withIdentifier(USER_PROFILE);
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
        super.onDestroy();
        customTabActivityHelper.setConnectionCallback(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment newFragment = new SearchProductsResultsFragment();
            Bundle args = new Bundle();
            args.putString("query", query);
            newFragment.setArguments(args);

            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

}
