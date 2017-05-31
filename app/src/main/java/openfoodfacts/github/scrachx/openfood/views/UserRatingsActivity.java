package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.RatingItem;
import openfoodfacts.github.scrachx.openfood.models.RatingProduct;
import openfoodfacts.github.scrachx.openfood.models.RatingProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.RatingProductListAdapter;

public class UserRatingsActivity extends BaseActivity {
    private RatingProductDao mRatingDao;
    private OpenFoodAPIClient api;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.ratings_recycler_view)
    RecyclerView ratingsRecyclerView;

    private List<RatingItem> mRatings;
    private boolean emptyRatings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_ratings);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.user_ratings);

        api = new OpenFoodAPIClient(this);
        mRatingDao = Utils.getAppDaoSession(this).getRatingProductDao();
        mRatings = new ArrayList<>();

        new UserRatingsActivity.FillAdapter(this).execute(this);
    }

    public class FillAdapter extends AsyncTask<Context, Void, Context> {

        private Activity activity;

        public FillAdapter(Activity act) {
            activity = act;
        }

        @Override
        protected void onPreExecute() {
            List<RatingProduct> listHistoryProducts = mRatingDao.loadAll();
            if (listHistoryProducts.size() == 0) {
                Toast.makeText(getApplicationContext(), R.string.txtNoData, Toast.LENGTH_LONG).show();
                emptyRatings = true;
                invalidateOptionsMenu();
                cancel(true);
            } else {
                Toast.makeText(getApplicationContext(), R.string.txtLoading, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Context doInBackground(Context... ctx) {
            List<RatingProduct> listRatingProducts = mRatingDao.queryBuilder().orderDesc(RatingProductDao.Properties.Stars).list();
            final Bitmap defaultImgUrl = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_no), 200, 200, true);

            for (RatingProduct rating : listRatingProducts) {
                Bitmap imgUrl;
                HttpURLConnection connection = null;
                InputStream input = null;
                try {
                    URL url = new URL(rating.getImageUrl());
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    input = connection.getInputStream();
                    imgUrl = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 200, 200, true);
                } catch (IOException e) {
                    Log.i("UserRatingActivity", "Unable to get the rating product image", e);
                    imgUrl = defaultImgUrl;
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) { }
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                mRatings.add(new RatingItem(rating.getStars(), rating.getComment(), rating.getBarcode(), rating.getProductName(), imgUrl));
            }

            return ctx[0];
        }

        @Override
        protected void onPostExecute(Context ctx) {
            RatingProductListAdapter adapter = new RatingProductListAdapter(mRatings, activity);
            ratingsRecyclerView.setAdapter(adapter);
            ratingsRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        }
    }
}
