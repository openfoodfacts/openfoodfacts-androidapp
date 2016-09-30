package openfoodfacts.github.scrachx.openfood.views;

import android.os.Bundle;
import openfoodfacts.github.scrachx.openfood.R;

public class ScannerFragmentActivity extends BaseScannerActivity {

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.fragment_scanner);
        setupToolbar();
    }

}
