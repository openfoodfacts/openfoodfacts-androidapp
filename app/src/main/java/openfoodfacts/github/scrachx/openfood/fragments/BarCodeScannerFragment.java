package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import java.util.ArrayList;
import java.util.List;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;

public class BarCodeScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private List<BarcodeFormat> mFormats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mScannerView = new ZXingScannerView(getActivity());
        mScannerView.setFocusableInTouchMode(true);
        mScannerView.requestFocus();
        mScannerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                } else {
                    return false;
                }
            }
        });
        mFormats = new ArrayList<BarcodeFormat>();
        mFormats.add(BarcodeFormat.UPC_A);
        mFormats.add(BarcodeFormat.UPC_E);
        mFormats.add(BarcodeFormat.EAN_13);
        mFormats.add(BarcodeFormat.EAN_8);
        mFormats.add(BarcodeFormat.RSS_14);
        mFormats.add(BarcodeFormat.CODE_39);
        mFormats.add(BarcodeFormat.CODE_93);
        mFormats.add(BarcodeFormat.CODE_128);
        mFormats.add(BarcodeFormat.ITF);
        mScannerView.setFormats(mFormats);
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        return mScannerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setFormats(mFormats);
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
        if(rawResult.getText().isEmpty()){

        }else{
            goToProduct(rawResult, mScannerView);
        }
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