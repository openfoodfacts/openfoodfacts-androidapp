package org.openfoodfacts.scanner.views;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

import org.openfoodfacts.scanner.R;
import org.openfoodfacts.scanner.network.OpenFoodAPIClient;
import org.openfoodfacts.scanner.utils.Utils;

public class ScannerFragmentActivity extends BaseActivity {

    OpenFoodAPIClient api;
    BottomSheetBehavior bottomSheetBehavior;
    TextView mBarcode;
    Toast mToast;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        api = new OpenFoodAPIClient(ScannerFragmentActivity.this);
        setContentView(R.layout.fragment_scanner);

        View v = findViewById(R.id.design_bottom_sheet);
        mBarcode = v.findViewById(R.id.barcode);
        bottomSheetBehavior = BottomSheetBehavior.from(v);

        final Handler handler = new Handler();
        handler.postDelayed(() -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED), 15000);

        setupToolbar();
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.try_it_scan_now);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void expand(View view) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }


    public void displayToast(String message) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(ScannerFragmentActivity.this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void find(View view) {
        Utils.hideKeyboard(ScannerFragmentActivity.this);
        if (mBarcode.getText().toString().isEmpty()) {
            displayToast(getResources().getString(R.string.txtBarcodeNotValid));
        } else {
            if (EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(mBarcode.getText().toString()) && (!mBarcode.getText().toString().substring(0, 3).contains("977") || !mBarcode.getText().toString().substring(0, 3).contains("978") || !mBarcode.getText().toString().substring(0, 3).contains("979"))) {
                api.getProduct(mBarcode.getText().toString(), ScannerFragmentActivity.this);
            } else {
                displayToast(getResources().getString(R.string.txtBarcodeNotValid));
            }
        }
    }
}
