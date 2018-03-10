package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

import java.util.Arrays;

import butterknife.OnClick;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;

public class BarCodeScannerFragment extends BaseFragment implements MessageDialogFragment.MessageDialogListener,
        ZXingScannerView.ResultHandler, CameraSelectorDialogFragment.CameraSelectorDialogListener {

    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String CAMERA_ID = "CAMERA_ID";
    private static final String RING_STATE = "RING_STATE";
    private ZXingScannerView mScannerView;
    private boolean mFlash;
    private boolean mRing;
    private boolean mAutoFocus;
    private int mCameraId = -1;
    private OpenFoodAPIClient api;
    private SharedPreferences settings;
    private EditText mBarcode;
    private Toast mToast;
    private Button cancelButton;
    private Button findButton;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {

        final SharedPreferences settings = getActivity().getSharedPreferences("camera", 0);

        mScannerView = new ZXingScannerView(getActivity());
        api = new OpenFoodAPIClient(getActivity());
        if (state != null) {
            mRing = state.getBoolean(RING_STATE, false);
            mFlash = state.getBoolean(FLASH_STATE, false);
            mAutoFocus = state.getBoolean(AUTO_FOCUS_STATE, true);
            mCameraId = state.getInt(CAMERA_ID, -1);
        } else {
            mRing = settings.getBoolean("ring", false);
            mFlash = settings.getBoolean("flash", false);
            mAutoFocus = settings.getBoolean("focus", true);
            mCameraId = -1;
        }


        setupFormats();

        return mScannerView;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem ringMenuItem = menu.add(Menu.NONE, R.id.menu_ring, 0, mRing ? R.string.ring_on : R.string.ring_off);
        MenuItemCompat.setShowAsAction(ringMenuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem flashMenuItem = menu.add(Menu.NONE, R.id.menu_flash, 0, mFlash ? R.string.flash_on : R.string.flash_off);
        MenuItemCompat.setShowAsAction(flashMenuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem focusMenuItem = menu.add(Menu.NONE, R.id.menu_auto_focus, 0, mAutoFocus ? R.string.auto_focus_on : R.string.auto_focus_off);
        MenuItemCompat.setShowAsAction(focusMenuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem cameraMenuItem = menu.add(Menu.NONE, R.id.menu_camera_selector, 0, R.string.select_camera);
        MenuItemCompat.setShowAsAction(cameraMenuItem, MenuItem.SHOW_AS_ACTION_NEVER);

        MenuItem aboutMenuItem = menu.add(Menu.NONE, R.id.menu_about, 0, R.string.action_about);
        MenuItemCompat.setShowAsAction(aboutMenuItem, MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items

        final SharedPreferences settings = getActivity().getSharedPreferences("camera", 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (item.getItemId()) {
            case R.id.menu_ring:
                mRing = !mRing;
                if (mRing) {
                    item.setTitle(R.string.ring_on);
                    editor.putBoolean("ring", true);
                } else {
                    item.setTitle(R.string.ring_off);
                    editor.putBoolean("ring", false);
                }
                editor.apply();
                return true;
            case R.id.menu_flash:
                mFlash = !mFlash;
                if (mFlash) {
                    item.setTitle(R.string.flash_on);
                    editor.putBoolean("flash", true);
                } else {
                    item.setTitle(R.string.flash_off);
                    editor.putBoolean("flash", false);
                }
                editor.apply();
                mScannerView.setFlash(mFlash);
                return true;
            case R.id.menu_auto_focus:
                mAutoFocus = !mAutoFocus;
                if (mAutoFocus) {
                    item.setTitle(R.string.auto_focus_on);
                    editor.putBoolean("focus", true);
                } else {
                    item.setTitle(R.string.auto_focus_off);
                    editor.putBoolean("focus", false);
                }
                editor.apply();
                mScannerView.setAutoFocus(mAutoFocus);
                return true;
            case R.id.menu_camera_selector:
                DialogFragment cFragment = CameraSelectorDialogFragment.newInstance(this, mCameraId);
                cFragment.show(getActivity().getSupportFragmentManager(), "camera_selector");
                return true;
            case R.id.menu_about:
                new MaterialDialog.Builder(this.getActivity())
                        .title(R.string.action_about)
                        .content(R.string.txtAbout)
                        .neutralText(R.string.txtOk)
                        .show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera(mCameraId);
        mScannerView.setFlash(mFlash);
        mScannerView.setAutoFocus(mAutoFocus);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
        outState.putBoolean(AUTO_FOCUS_STATE, mAutoFocus);
        outState.putBoolean(RING_STATE, mRing);
    }

    @Override
    public void handleResult(Result rawResult) {
        if (mRing) {
            try {
                ToneGenerator beep = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                beep.startTone(ToneGenerator.TONE_PROP_BEEP);
            } catch (Exception e) {
                Log.e("SCAN RING", e.getMessage(), e);
            }
        }

        if (rawResult.getText().isEmpty()) {
            return;
        }


        if (Utils.isNetworkConnected(getContext())) {
            if (settings.getBoolean("powerMode", false) && mScannerView != null) {
                api.getShortProduct(rawResult.getText(), getActivity(), mScannerView, this);
            } else {
                api.getProduct(rawResult.getText(), getActivity());
            }
        } else {
            Intent intent = new Intent(getActivity(), SaveProductOfflineActivity.class);
            intent.putExtra("barcode", rawResult.getText());
            getActivity().startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Resume the camera
        mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onCameraSelected(int cameraId) {
        if (mCameraId != cameraId) {
            mCameraId = cameraId;
            mScannerView.stopCamera();
            mScannerView.startCamera(mCameraId);
            mScannerView.setFlash(mFlash);
            mScannerView.setAutoFocus(mAutoFocus);
        }
    }

    public void setupFormats() {
        mScannerView.setFormats(Arrays.asList(BarcodeFormat.UPC_A, BarcodeFormat.UPC_E,
                BarcodeFormat.EAN_13, BarcodeFormat.EAN_8,
                BarcodeFormat.RSS_14, BarcodeFormat.CODE_39,
                BarcodeFormat.CODE_93, BarcodeFormat.CODE_128,
                BarcodeFormat.ITF));
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }
}