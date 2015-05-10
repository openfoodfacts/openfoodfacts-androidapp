package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openfoodfacts.github.scrachx.openfood.views.ProductActivity;

/**
 * Created by scotscriven on 04/05/15.
 */

public class BarCodeScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private List<BarcodeFormat> formats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mScannerView = new ZXingScannerView(getActivity());
        formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.UPC_A);
        formats.add(BarcodeFormat.UPC_E);
        formats.add(BarcodeFormat.EAN_13);
        formats.add(BarcodeFormat.EAN_8);
        formats.add(BarcodeFormat.RSS_14);
        formats.add(BarcodeFormat.CODE_39);
        formats.add(BarcodeFormat.CODE_93);
        formats.add(BarcodeFormat.CODE_128);
        formats.add(BarcodeFormat.ITF);
        mScannerView.setFormats(formats);
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setFormats(formats);
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        mScannerView.startCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {}
        goToProduct(rawResult, mScannerView);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    private void goToProduct(Result rawResult, ZXingScannerView scannerView){
        FoodAPIRestClientUsage api = new FoodAPIRestClientUsage();
        api.getProduct(rawResult.getText(), getActivity(), scannerView);
    }
}