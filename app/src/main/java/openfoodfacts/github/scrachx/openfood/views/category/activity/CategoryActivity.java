package openfoodfacts.github.scrachx.openfood.views.category.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_category);

        setSupportActionBar(findViewById(R.id.toolbar));
        setTitle(R.string.category_drawer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
