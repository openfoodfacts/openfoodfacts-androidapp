package openfoodfacts.github.scrachx.openfood.views;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.orm.query.Select;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.HistoryItem;
import openfoodfacts.github.scrachx.openfood.models.HistoryProduct;
import openfoodfacts.github.scrachx.openfood.views.adapters.HistoryListAdapter;

public class HistoryScanActivity extends BaseActivity {

    private ShareActionProvider mShareActionProvider;
    private List<HistoryItem> productItems;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.listHistoryScan)
    RecyclerView recyclerHistoryScanView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_scan);

        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productItems = new ArrayList<>();
        new HistoryScanActivity.FillAdapter().execute(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_edit_product:
                // TODO : export option
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*getMenuInflater().inflate(R.menu.menu_product, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String url = " " + Utils.getUriProductByCurrentLanguage() + mState.getProduct().getCode();
        if (mState.getProduct().getUrl() != null) {
            url = " " + mState.getProduct().getUrl();
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.msg_share) + url);
        shareIntent.setType("text/plain");
        setShareIntent(shareIntent);*/

        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public class FillAdapter extends AsyncTask<Context, Void, Context> {

        @Override
        protected void onPreExecute() {
            List<HistoryProduct> listHistoryProducts = HistoryProduct.listAll(HistoryProduct.class);
            if (listHistoryProducts.size() == 0) {
                Toast.makeText(getApplicationContext(), R.string.txtNoData, Toast.LENGTH_LONG).show();
                cancel(true);
            } else {
                Toast.makeText(getApplicationContext(), R.string.txtLoading, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Context doInBackground(Context... ctx) {
            List<HistoryProduct> listHistoryProducts = Select.from(HistoryProduct.class).orderBy("LAST_SEEN DESC").list();
            for (int i = 0; i < listHistoryProducts.size(); i++) {
                HistoryProduct hp = listHistoryProducts.get(i);
                Bitmap imgUrl = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_no), 200, 200, true);
                try {
                    URL url = new URL(hp.getUrl());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    imgUrl = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 200, 200, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                productItems.add(new HistoryItem(hp.getTitle(), hp.getBrands(), imgUrl, hp.getBarcode()));
            }

            return ctx[0];
        }

        @Override
        protected void onPostExecute(Context ctx) {
            HistoryListAdapter adapter = new HistoryListAdapter(productItems, getApplication());
            recyclerHistoryScanView.setAdapter(adapter);
            recyclerHistoryScanView.setLayoutManager(new LinearLayoutManager(ctx));
        }
    }

}
