package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.IngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.NutritionInfoProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.NutritionProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.SummaryProductFragment;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class ProductActivity extends BaseActivity {

    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.buttonScan)
    FloatingActionButton mButtonScan;
    private ShareActionProvider mShareActionProvider;
    private State mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_product);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);
        mState = (State) getIntent().getExtras().getSerializable("state");
        if (!Utils.isHardwareCameraInstalled(this)) {
            mButtonScan.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.buttonScan)
    protected void OnScan() {
        if (Utils.isHardwareCameraInstalled(this)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    new MaterialDialog.Builder(this)
                            .title(R.string.action_about)
                            .content(R.string.permission_camera)
                            .neutralText(R.string.txtOk)
                            .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                            .show();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                Intent intent = new Intent(this, ScannerFragmentActivity.class);
                startActivity(intent);
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        String[] menuTitles = getResources().getStringArray(R.array.nav_drawer_items_product);

        ProductFragmentPagerAdapter adapterResult = new ProductFragmentPagerAdapter(getSupportFragmentManager());
        adapterResult.addFragment(new SummaryProductFragment(), menuTitles[0]);
        adapterResult.addFragment(new IngredientsProductFragment(), menuTitles[1]);
        if (BuildConfig.FLAVOR.equals("off")) {
            adapterResult.addFragment(new NutritionProductFragment(), menuTitles[2]);
            adapterResult.addFragment(new NutritionInfoProductFragment(), menuTitles[3]);
        }
        if (BuildConfig.FLAVOR.equals("opff")) {
            adapterResult.addFragment(new NutritionProductFragment(), menuTitles[2]);
            adapterResult.addFragment(new NutritionInfoProductFragment(), menuTitles[3]);
        }
        viewPager.setAdapter(adapterResult);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;

            case R.id.menu_item_share:
                String shareUrl = " " + getString(R.string.website_product) + mState.getProduct().getCode();
                Intent sharingIntent = new Intent();
                sharingIntent.setAction(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = getResources().getString(R.string.msg_share) + shareUrl;
                String shareSub = "\n\n";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share using"));
                return true;

            case R.id.action_edit_product:
                String url = getString(R.string.website) + "cgi/product.pl?type=edit&code=" + mState.getProduct().getCode();
                if (mState.getProduct().getUrl() != null) {
                    url = " " + mState.getProduct().getUrl();
                }

                CustomTabsIntent customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getBaseContext(), null);

                CustomTabActivityHelper.openCustomTab(ProductActivity.this, customTabsIntent, Uri.parse(url), new WebViewFallback());
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product, menu);
//        MenuItem item = menu.findItem(R.id.menu_item_share);
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
//
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        String url = " " + getString(R.string.website_product) + mState.getProduct().getUniqueAllergenID();
//        if (mState.getProduct().getUrl() != null) {
//            url = " " + mState.getProduct().getUrl();
//        }
//        shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.msg_share) + url);
//        shareIntent.setType("text/plain");
//        setShareIntent(shareIntent);

        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
            case Utils.MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length <= 0 || grantResults[0] != PERMISSION_GRANTED) {
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
            }
        }
    }
}
