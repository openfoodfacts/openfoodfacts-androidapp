package openfoodfacts.github.scrachx.openfood.views;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import openfoodfacts.github.scrachx.openfood.R;

/**
 * Created by scotscriven on 04/05/15.
 */
public class ScannerFragmentActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.fragment_scanner);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
