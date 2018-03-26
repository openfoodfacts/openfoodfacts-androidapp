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
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.BuildConfig;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.IngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.NutritionInfoProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.NutritionProductFragment;
import openfoodfacts.github.scrachx.openfood.fragments.SummaryProductFragment;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.SearchType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductFragmentPagerAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductsRecyclerViewAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabsHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public class ProductActivity extends BaseActivity implements CustomTabActivityHelper.ConnectionCallback {

    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.buttonScan)
    FloatingActionButton mButtonScan;
    private ShareActionProvider mShareActionProvider;
    private BottomSheetBehavior bottomSheetBehavior;
    TextView bottomSheetDesciption;
    TextView bottomSheetTitle;
    Button buttonToBrowseProducts;
    Button wikipediaButton;
    RecyclerView productBrowsingRecyclerView;
    ProductsRecyclerViewAdapter productsRecyclerViewAdapter;
    private CustomTabActivityHelper customTabActivityHelper;
    private CustomTabsIntent customTabsIntent;
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

        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(getApplicationContext(), customTabActivityHelper.getSession());

        View v = findViewById(R.id.design_bottom_sheet_product_activity);
        bottomSheetTitle = v.findViewById(R.id.titleBottomSheet);
        bottomSheetDesciption = v.findViewById(R.id.description);
        buttonToBrowseProducts = v.findViewById(R.id.buttonToBrowseProducts);
        wikipediaButton = v.findViewById(R.id.wikipediaButton);

        bottomSheetBehavior = BottomSheetBehavior.from(v);

        mState = (State) getIntent().getExtras().getSerializable("state");
        if (!Utils.isHardwareCameraInstalled(this)) {
            mButtonScan.setVisibility(View.GONE);
        }
    }

    public void expand() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        mButtonScan.setVisibility(View.INVISIBLE);
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

    public void showBottomScreen(JSONObject result, String code, int type, String title) {
        try {
            result = result.getJSONObject("entities").getJSONObject(code);
            JSONObject description = result.getJSONObject("descriptions");
            JSONObject sitelinks = result.getJSONObject("sitelinks");
            String descriptionString = getDescription(description);
            String wikiLink = getWikiLink(sitelinks);
            bottomSheetTitle.setText(title);
            bottomSheetDesciption.setText(descriptionString);
            buttonToBrowseProducts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(ProductActivity.this, ProductBrowsingListActivity.class);
                    switch (type) {
                        case 1: {
                            intent.putExtra("search_type", SearchType.CATEGORY);
                            break;
                        }
                        case 2: {
                            intent.putExtra("search_type", SearchType.LABEL);
                            break;
                        }
                        case 3: {
                            intent.putExtra("search_type", SearchType.ADDITIVE);
                            break;
                        }
                    }
                    intent.putExtra("search_query", title);
                    startActivity(intent);
                }
            });
            wikipediaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openInCustomTab(wikiLink);
                }
            });
            expand();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getWikiLink(JSONObject sitelinks) {
        String link = "";
        String languageCode = Locale.getDefault().getLanguage();
        languageCode = languageCode + "wiki";
        if (sitelinks.has(languageCode)) {
            try {
                sitelinks = sitelinks.getJSONObject(languageCode);
                link = sitelinks.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (sitelinks.has("enwiki")) {
            try {
                sitelinks = sitelinks.getJSONObject("enwiki");
                link = sitelinks.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("ProductActivity", "Result for wikilink is not found in native or english language.");
        }
        return link;
    }

    private String getDescription(JSONObject description) {
        String descriptionString = "";
        String languageCode = Locale.getDefault().getLanguage();
        if (description.has(languageCode)) {
            try {
                description = description.getJSONObject(languageCode);
                descriptionString = description.getString("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (description.has("en")) {
            try {
                description = description.getJSONObject("en");
                descriptionString = description.getString("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("ProductActivity", "Result for description is not found in native or english language.");
        }
        return descriptionString;
    }

    private void openInCustomTab(String url) {
        Uri wikipediaUri = Uri.parse(url);
        CustomTabActivityHelper.openCustomTab(ProductActivity.this, customTabsIntent, wikipediaUri, new WebViewFallback());

    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }
}
