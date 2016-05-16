package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fastadapter.utils.RecyclerViewCacheUtil;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import butterknife.Bind;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.AlertUserFragment;
import openfoodfacts.github.scrachx.openfood.fragments.FindProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.HomeFragment;
import openfoodfacts.github.scrachx.openfood.fragments.OfflineEditFragment;
import openfoodfacts.github.scrachx.openfood.fragments.SearchProductsFragment;
import openfoodfacts.github.scrachx.openfood.fragments.UserFragment;

public class MainActivity extends BaseActivity {

    private AccountHeader headerResult = null;
    private Drawer result = null;
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences settings = getSharedPreferences("login", 0);
        String loginS = settings.getString("user", "");
        if(loginS.isEmpty()) loginS = getResources().getString(R.string.txt_anonymous);

        final IProfile profile = new ProfileDrawerItem().withName(loginS).withIcon(R.drawable.img_home).withIdentifier(100);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.home_drawer));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment()).commit();

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.background_header_drawer)
                .addProfiles(profile)
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.home_drawer).withIcon(FontAwesome.Icon.faw_home).withIdentifier(1),
                        new SectionDrawerItem().withName(R.string.search_drawer),
                        new SecondaryDrawerItem().withName(R.string.search_by_barcode_drawer).withIcon(FontAwesome.Icon.faw_barcode).withIdentifier(2),
                        new SecondaryDrawerItem().withName(R.string.search_by_name_drawer).withIcon(FontAwesome.Icon.faw_search).withIdentifier(3),
                        new SecondaryDrawerItem().withName(R.string.scan_search).withIcon(FontAwesome.Icon.faw_camera).withIdentifier(4),
                        new SectionDrawerItem().withName(R.string.user_drawer),
                        new SecondaryDrawerItem().withName(R.string.sign_in_drawer).withIcon(FontAwesome.Icon.faw_sign_in).withIdentifier(5),
                        new SecondaryDrawerItem().withName(R.string.alert_drawer).withIcon(FontAwesome.Icon.faw_info).withIdentifier(6),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(R.string.offline_edit_drawer).withIcon(FontAwesome.Icon.faw_anchor).withIdentifier(7)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem != null) {
                            Fragment fragment = null;
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1) {
                                fragment = new HomeFragment();
                                getSupportActionBar().setTitle(getResources().getString(R.string.home_drawer));
                            } else if (drawerItem.getIdentifier() == 2) {
                                fragment = new FindProductFragment();
                                getSupportActionBar().setTitle(getResources().getString(R.string.search_by_barcode_drawer));
                            } else if (drawerItem.getIdentifier() == 3) {
                                fragment = new SearchProductsFragment();
                                getSupportActionBar().setTitle(getResources().getString(R.string.search_by_name_drawer));
                            } else if (drawerItem.getIdentifier() == 4) {
                                intent = new Intent(MainActivity.this, ScannerFragmentActivity.class);
                                startActivity(intent);
                            } else if (drawerItem.getIdentifier() == 5) {
                                fragment = new UserFragment();
                                getSupportActionBar().setTitle(R.string.sign_in_drawer);
                            } else if (drawerItem.getIdentifier() == 6) {
                                fragment = new AlertUserFragment();
                                getSupportActionBar().setTitle(R.string.alert_drawer);
                            } else if (drawerItem.getIdentifier() == 7) {
                                fragment = new OfflineEditFragment();
                                getSupportActionBar().setTitle(getResources().getString(R.string.offline_edit_drawer));
                            }
                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }

                            if (fragment != null) {
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, fragment).commit();
                            } else {
                                // error in creating fragment
                                Log.e("MainActivity", "Error in creating fragment");
                            }
                        }

                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(false)
                .build();

        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

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
    }

    private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
            if (drawerItem instanceof Nameable) {
                Log.i("material-drawer", "DrawerItem: " + ((Nameable) drawerItem).getName() + " - toggleChecked: " + isChecked);
            } else {
                Log.i("material-drawer", "toggleChecked: " + isChecked);
            }
        }
    };

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_about:
                new MaterialDialog.Builder(this)
                .title(R.string.action_about)
                .content(R.string.txtAbout)
                .neutralText(R.string.txtOk)
                .show();
            return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
