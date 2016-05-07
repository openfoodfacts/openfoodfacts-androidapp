package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Allergen;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductPagerAdapter;

public class ProductActivity extends BaseActivity {

    private ShareActionProvider mShareActionProvider;
    private ProductPagerAdapter adapterResult;
    private List<Allergen> mAllergens;

    @Bind(R.id.pager) ViewPager viewPager;
    @Bind(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAllergens = Allergen.find(Allergen.class, "enable = ?", "true");
        adapterResult = new ProductPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapterResult);

        Intent intent = getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        List<String> all = (List<String>) state.getProduct().getAllergensHierarchy();
        List<String> traces = (List<String>) state.getProduct().getTracesTags();
        all.addAll(traces);
        List<String> matchAll = new ArrayList<String>();
        for (int a = 0; a < mAllergens.size(); a++) {
            for(int i = 0; i < all.size(); i++) {
                if (all.get(i).trim().equals(mAllergens.get(a).getIdAllergen().trim())) {
                    matchAll.add(mAllergens.get(a).getName());
                }
            }
        }

        if(matchAll.size() > 0) {
            new MaterialDialog.Builder(this)
                    .title(R.string.warning_allergens)
                    .items(matchAll)
                    .neutralText(R.string.txtOk)
                    .titleColorRes(R.color.red_500)
                    .dividerColorRes(R.color.indigo_900)
                    .icon(this.getResources().getDrawable(R.drawable.ic_warning_24dp))
                    .show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_share, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        Intent intent = getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String url = " " + Utils.getUriProductByCurrentLanguage() + state.getProduct().getCode();
        if (state.getProduct().getUrl() != null) {
            url = " " + state.getProduct().getUrl();
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.msg_share) + url);
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);

        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }
}
