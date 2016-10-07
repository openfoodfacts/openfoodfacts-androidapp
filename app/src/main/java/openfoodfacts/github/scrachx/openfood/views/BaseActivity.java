package openfoodfacts.github.scrachx.openfood.views;

import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        LocaleHelper.onCreate(this);
    }
}
