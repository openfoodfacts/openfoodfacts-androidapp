package openfoodfacts.github.scrachx.openfood.views.category.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;

public class CategoryActivity extends BaseActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, CategoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkNetworkAndShowScreen();
    }

    private void checkNetworkAndShowScreen(){
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected) {

            setContentView(R.layout.activity_category);

            setSupportActionBar(findViewById(R.id.toolbar));
            setTitle(R.string.category_drawer);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        else{
            setContentView(R.layout.offline_cloud_layout);
            findViewById(R.id.buttonToRefresh).setOnClickListener(view -> checkNetworkAndShowScreen());

        }
    }
}
