package openfoodfacts.github.scrachx.openfood.views.category.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;

import static openfoodfacts.github.scrachx.openfood.utils.CustomTextView.CustomOfflineSnackbar;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.isNetworkConnected;

public class CategoryActivity extends BaseActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, CategoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        if (!isNetworkConnected(getApplicationContext()))
            CustomOfflineSnackbar(findViewById(android.R.id.content));
        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle(R.string.category_drawer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
